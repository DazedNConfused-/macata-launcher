package com.dazednconfused.catalauncher.database;

import com.dazednconfused.catalauncher.helper.Constants;
import com.dazednconfused.catalauncher.helper.result.Result;

import io.vavr.control.Try;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class H2Database {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2Database.class);

    private static final String JDBC_URL_TEMPLATE = "jdbc:h2:%s/db/%s;AUTO_SERVER=TRUE";

    private static final String USER = null;
    private static final String PASSWORD = null;
    public static final String DATABASE_NAME_BLANK_ERROR = "Database name cannot be blank! Aborting operation";

    /**
     * Opens a connection to {@link #getDatabaseName()}.
     *
     * @apiNote Callers of this method are in charge of properly handling and disposing of this connection upon use.
     * */
    public Connection getConnection() {
        return H2Database.openConnection(getDatabaseName()).toEither()
            .getOrElseThrow(() -> new RuntimeException("Could not establish database connection to [" + getDatabaseName() + "]"))
            .getResult()
            .orElseThrow(() -> new RuntimeException("Expected database connection to [" + getDatabaseName() + "], but object was empty! Aborting operation"));
    }

    /**
     * Establishes a connection to the provided {@code database}.
     * */
    protected static Result<Throwable, Connection> openConnection(String database) {
        if (StringUtils.isBlank(database)) {
            return Result.failure(new Throwable(DATABASE_NAME_BLANK_ERROR));
        }

        LOGGER.trace("Opening connection for database [{}]...", database);

        return Try.of(() -> DriverManager.getConnection(
            String.format(JDBC_URL_TEMPLATE, Constants.LAUNCHER_FILES, database),
            USER, PASSWORD
        )).onFailure(
            t -> LOGGER.error("There was an error while opening database file [{}]", database, t)
        ).map(Result::success).recover(Result::failure).get();
    }


    /**
     * Completely wipes this database of any and all data.
     *
     * @apiNote Callers of this method are responsible for making sure any and all open {@link Connection}s are properly
     *          disposed of before triggering the wipe.
     *
     * @implNote Equivalent to calling {@link #wipe(String)} with {@link #getDatabaseName()} as argument.
     * */
    public Result<Throwable, Object> wipe() {
        return H2Database.wipe(getDatabaseName());
    }

    /**
     * Completely wipes the given {@code database} of any and all data.
     * */
    protected static Result<Throwable, Object> wipe(String database) {
        if (StringUtils.isBlank(database)) {
            return Result.failure(new Throwable(DATABASE_NAME_BLANK_ERROR));
        }

        LOGGER.trace("Wiping database [{}]...", database);

        return Try.of(() -> DriverManager.getConnection(
            String.format(JDBC_URL_TEMPLATE, Constants.LAUNCHER_FILES, database),
            USER, PASSWORD
        )).onFailure(
            t -> LOGGER.error("There was an error while wiping database file [{}]", database, t)
        ).andThenTry(connection -> {
            Statement stmt = connection.createStatement();
            stmt.execute("DROP ALL OBJECTS");
        }).map(connection -> Result.success()).recover(Result::failure).get();
    }

    /**
     * Completely destroys this database file.
     *
     * @apiNote Callers of this method are responsible for making sure any and all open {@link Connection}s are properly
     *          disposed of before triggering the wipe.
     *
     * @implNote Equivalent to calling {@link #destroy(String)} with {@link #getDatabaseName()} as argument.
     * */
    public Result<Throwable, Object> destroy() {
        return H2Database.destroy(getDatabaseName());
    }

    /**
     * Completely destroys the {@code database} file.
     * */
    protected static Result<Throwable, Object> destroy(String database) {
        if (StringUtils.isBlank(database)) {
            return Result.failure(new Throwable(DATABASE_NAME_BLANK_ERROR));
        }

        LOGGER.trace("Destroying database [{}]...", database);

        return Try.of(() -> {
            String dbFilePath = Constants.LAUNCHER_FILES + "/db/" + database + ".mv.db";

            File dbFile = new File(dbFilePath);

            if (dbFile.exists() && dbFile.delete()) {
                LOGGER.trace("Database file for [{}] deleted successfully", database);
                return Result.success();
            } else {
                LOGGER.trace("Failed to delete database file for [{}]", database);
                return Result.failure(new Throwable("Failed to delete database file for [" + database + "]"));
            }
        }).onFailure(
            t -> LOGGER.error("There was an error while deleting database file [{}]", database, t)
        ).recover(Result::failure).get();
    }

    /**
     * The underlying database's name.
     * */
    public abstract String getDatabaseName();
}
package com.dazednconfused.catalauncher.database.h2;

import com.dazednconfused.catalauncher.database.base.DAOException;
import com.dazednconfused.catalauncher.database.base.DisposableDatabase;
import com.dazednconfused.catalauncher.database.h2.migration.MigrateableH2Database;
import com.dazednconfused.catalauncher.helper.Constants;
import com.dazednconfused.catalauncher.helper.result.Result;

import io.vavr.control.Try;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class H2Database implements DisposableDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2Database.class);

    private static final String JDBC_URL_TEMPLATE = "jdbc:h2:%s/db/%s;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=TRUE";

    private static final String USER = null;
    private static final String PASSWORD = null;
    public static final String DATABASE_NAME_BLANK_ERROR = "Database name cannot be blank! Aborting operation";

    /**
     * Opens a connection to {@link #getDatabaseName()}.
     *
     * @apiNote Callers of this method are in charge of properly handling and disposing of this connection upon use.
     * */
    @Override
    public Connection getConnection() {
        return H2Database.openConnection(getDatabaseName()).toEither()
            .getOrElseThrow(() -> new RuntimeException("Could not establish database connection to [" + getDatabaseName() + "]"))
            .getResult()
            .orElseThrow(() -> new RuntimeException("Expected database connection to [" + getDatabaseName() + "], but object was empty! Aborting operation"));
    }

    /**
     * Returns whether the given {@code tableName} exists within the {@link #getDatabaseName()}.
     * */
    public boolean doesTableExist(String tableName) {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";

        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tableName.toUpperCase());
            try (var rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("There was an error while checking if table [{}] exists in database [{}]", tableName, this.getDatabaseName(), e);
        }

        return false;
    }

    /**
     * Returns whether the given {@code columnName} exists inside the {@code tableName} within the {@link #getDatabaseName()}.
     * */
    public boolean doesColumnExist(String tableName, String columnName) {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?";

        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tableName.toUpperCase());
            pstmt.setString(2, columnName.toUpperCase());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("There was an error while checking if column [{}] exists within table [{}] in database [{}]", columnName, tableName, this.getDatabaseName(), e);

        }
        return false;
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
    @Override
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
            connection.close();
        }).map(connection -> Result.success()).recover(Result::failure).get();
    }

    /**
     * Completely cleans the given {@code database} of any and all data, but leaves the underlying schema intact.
     *
     * @apiNote Callers of this method are responsible for making sure any and all open {@link Connection}s are properly
     *          disposed of before triggering the wipe.
     *
     * @implNote Equivalent to calling {@link #reset(String)} with {@link #getDatabaseName()} as argument.
     * */
    @Override
    public Result<Throwable, Object> reset() {
        return H2Database.reset(getDatabaseName());
    }

    /**
     * Completely cleans the given {@code database} of any and all data, but leaves the underlying schema intact.
     *
     * @apiNote If wiping the schema is also desired, use {@link #wipe(String)} instead.
     * */
    protected static Result<Throwable, Object> reset(String database) {
        if (StringUtils.isBlank(database)) {
            return Result.failure(new Throwable(DATABASE_NAME_BLANK_ERROR));
        }

        LOGGER.trace("Resetting database [{}]...", database);

        return Try.of(() -> DriverManager.getConnection(
            String.format(JDBC_URL_TEMPLATE, Constants.LAUNCHER_FILES, database),
            USER, PASSWORD
        )).onFailure(
            t -> LOGGER.error("There was an error while cleaning database file [{}]", database, t)
        ).andThenTry(connection -> {
            Statement stmt = connection.createStatement();

            // first, disable constraint checks for the duration of the operation ---
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");

            // truncate all tables, restarting their auto-increment IDs ---
            String getTablesQuery = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = SCHEMA()";

            ResultSet getTablesQueryRs = stmt.executeQuery(getTablesQuery);
            while (getTablesQueryRs.next()) {
                String tableName = getTablesQueryRs.getString("TABLE_NAME");

                if (tableName.equalsIgnoreCase(MigrateableH2Database.MIGRATION_TABLE_NAME)) {
                    LOGGER.trace("Skipping migration table [{}] from reset process...", tableName);
                    continue;
                }

                Statement truncateStmt = connection.createStatement();
                truncateStmt.executeUpdate("TRUNCATE TABLE \"" + tableName + "\" RESTART IDENTITY");
                truncateStmt.close();
                LOGGER.trace("Truncated table with identity restart [{}]", tableName);
            }

            // restart all sequences ---
            String sequenceRestartQuery = "SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES";

            ResultSet sequenceRestartQueryRs = stmt.executeQuery(sequenceRestartQuery);
            while (sequenceRestartQueryRs.next()) {
                String sequenceName = sequenceRestartQueryRs.getString(1);

                Statement alterSequenceStmt = connection.createStatement();
                alterSequenceStmt.executeUpdate("ALTER SEQUENCE " + sequenceName + " RESTART WITH 1");
                alterSequenceStmt.close();
                LOGGER.trace("Restarted sequence [{}]", sequenceName);
            }

            // re-enable constraint checks now that database has been properly reset ---
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE ");

            // resources cleanup ---
            getTablesQueryRs.close();
            sequenceRestartQueryRs.close();
            stmt.close();
            connection.close();

            LOGGER.trace("Database [{}] has been reset.", database);
        }).map(connection -> Result.success()).recover(Result::failure).get();
    }

    /**
     * Safely shuts down the database.
     *
     * @apiNote Callers of this method are responsible for making sure any and all open {@link Connection}s are properly
     *          disposed of before triggering the shutdown process.
     *
     * @implNote Equivalent to calling {@link #shutdown(String)} with {@link #getDatabaseName()} as argument.
     * */
    @Override
    public Result<Throwable, Object> shutdown() {
        return H2Database.shutdown(getDatabaseName());
    }

    /**
     * Safely shuts down the given {@code database}.
     * */
    protected static Result<Throwable, Object> shutdown(String database) {
        if (StringUtils.isBlank(database)) {
            return Result.failure(new Throwable(DATABASE_NAME_BLANK_ERROR));
        }

        LOGGER.trace("Shutting down database [{}]...", database);

        return Try.of(() -> DriverManager.getConnection(
            String.format(JDBC_URL_TEMPLATE, Constants.LAUNCHER_FILES, database),
            USER, PASSWORD
        )).onFailure(
            t -> LOGGER.error("There was an error while shutting down database [{}]", database, t)
        ).andThenTry(connection -> {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("SHUTDOWN");
            connection.close();
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
    @Override
    public Result<Throwable, Object> destroy() {
        this.shutdown().toEither().getOrElseThrow(() ->
            new DAOException("Could not shutdown database. It's unsafe to destroy the database. Aborting operation")
        );
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
            String dbLockFilePath = Constants.LAUNCHER_FILES + "/db/" + database + ".lock.db";
            File dbLockFile = new File(dbLockFilePath);

            if (dbLockFile.exists()) {
                LOGGER.trace("Lockfile detected. Deleting first...");
                if (dbLockFile.delete()) {
                    LOGGER.trace("Lockfile deleted successfully.");
                } else {
                    LOGGER.trace("Failed to delete database lockfile for [{}].", database);
                    return Result.failure(new Throwable("Failed to delete database lockfile for [" + database + "]"));
                }
            }

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
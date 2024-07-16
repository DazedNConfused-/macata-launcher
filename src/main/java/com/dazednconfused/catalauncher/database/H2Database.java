package com.dazednconfused.catalauncher.database;

import com.dazednconfused.catalauncher.helper.Constants;
import com.dazednconfused.catalauncher.helper.result.Result;

import io.vavr.control.Try;

import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class H2Database {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2Database.class);

    private static final String USER = "macata";
    private static final String PASSWORD = "macata";

    /**
     * Establishes a connection to the provided {@code database}.
     * */
    protected static Result<Throwable, Connection> openConnection(String database) {
        if (StringUtils.isBlank(database)) {
            LOGGER.error("Database name cannot be blank! Aborting operation");
            return Result.failure(new Throwable("Database name cannot be blank! Aborting operation"));
        }

        final String JDBC_URL_TEMPLATE = "jdbc:h2:%s/db/%s;AUTO_SERVER=TRUE";

        return Try.of(() -> DriverManager.getConnection(
            String.format(JDBC_URL_TEMPLATE, Constants.LAUNCHER_FILES, database),
            USER, PASSWORD
        )).onFailure(
            t -> LOGGER.error("There was an error while opening database file [{}]", database, t)
        ).map(Result::success).recover(Result::failure).get();
    }

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
     * The underlying database's name.
     * */
    public abstract String getDatabaseName();
}
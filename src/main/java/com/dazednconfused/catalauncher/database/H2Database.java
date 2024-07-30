package com.dazednconfused.catalauncher.database;

import com.dazednconfused.catalauncher.helper.Constants;
import com.dazednconfused.catalauncher.helper.result.Result;

import io.vavr.control.Try;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class H2Database {

    protected static final String DATABASE_MIGRATIONS_RESOURCE_PATH = "db/migrations/";

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
     * Safely shuts down the database.
     *
     * @apiNote Callers of this method are responsible for making sure any and all open {@link Connection}s are properly
     *          disposed of before triggering the shutdown process.
     *
     * @implNote Equivalent to calling {@link #shutdown(String)} with {@link #getDatabaseName()} as argument.
     * */
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
     * Executes the SQL script residing in the given {@code resourcePath}.
     *
     * @param resourcePath the path to the SQL script resource.
     *
     * @return The SQL's {@link PreparedStatement#execute(String)}'s response, wrapped inside a {@link Result#success(Object)}.
     *         {@link Result#failure(Throwable)} if an error occurred during the operation.
     */
    protected Result<Throwable, Boolean> executeSqlResource(String resourcePath) {
        try (
            var c = getConnection();
            var in = H2Database.getResourceAsStream(resourcePath);
            var isr = new InputStreamReader(in);
            var br = new BufferedReader(isr)
        ) {
            LOGGER.debug("Executing SQL resource [{}]...", resourcePath);

            var sql = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sql.append(line);
                sql.append("\n");
            }

            LOGGER.trace("SQL resource [{}] to be executed: \n ********** \n {} \n **********", resourcePath, sql);

            boolean result = c.prepareStatement(sql.toString()).execute();

            LOGGER.debug("Executed SQL resource [{}]", resourcePath);

            return Result.success(result);
        } catch (SQLException | IOException e) {
            LOGGER.error("There was an error while executing SQL resource [{}]", resourcePath, e);
            return Result.failure(e);
        }
    }

    /**
     * Returns all {@code .sql} migrations files that are dated after the provided {@code from} {@link Date}.
     */
    protected static List<File> getDatabaseMigrationFilesDatedAfter(Date from) {
        LOGGER.debug("Retrieving all database migration files dated after [{}]...", from);

        return Optional
            .of(getDatabaseMigrationFiles())
            .map(result -> result.toEither().get().getResult().orElse(Collections.emptyList()))
            .get().stream()
            .filter(filename -> FilenameUtils.getExtension(filename).equalsIgnoreCase("sql")) // retrieve only .sql script files
            .filter(filename -> {
                Optional<Date> migrationDateStamp = getDateFromFilename(filename);
                if (migrationDateStamp.isEmpty()) {
                    // without a valid migration Date stamp, we don't know whether we have to apply the migration or not. Better to skip it entirely....
                    LOGGER.debug("Skipping file [{}] for not having a valid migration Date stamp...", filename);
                    return false;
                }

                return migrationDateStamp.get().after(from);
            }) // retrieve only those that were created after the supplied date
            .sorted((fn1, fn2) -> {
                Date d1 = getDateFromFilename(fn1).orElseThrow();
                Date d2 = getDateFromFilename(fn2).orElseThrow();

                return d1.compareTo(d2);
            }) // sort the migrations according to its natural order (aka by their timestamps)
            .map(filename -> new File(DATABASE_MIGRATIONS_RESOURCE_PATH + filename))
            .collect(Collectors.toList());
    }

    /**
     * List all filenames living inside the classpath's {@link H2Database#DATABASE_MIGRATIONS_RESOURCE_PATH}'s {@code resources} folder.
     */
    protected static Result<Throwable, List<String>> getDatabaseMigrationFiles() {
        List<String> filenames = new ArrayList<>();

        try (InputStream in = H2Database.getResourceAsStream(DATABASE_MIGRATIONS_RESOURCE_PATH); BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;

            while ((resource = br.readLine()) != null) {
                filenames.add(resource);
            }
        } catch (IOException e) {
            LOGGER.error("There was an error while reading resource files from path [{}]", DATABASE_MIGRATIONS_RESOURCE_PATH);
            return Result.failure(e);
        }

        return Result.success(filenames);
    }

    /**
     * Reads a specific {@code resource} from the classpath.
     *
     * @implNote It's up to this method's callers to properly close the resulting {@link InputStream}.
     */
    private static InputStream getResourceAsStream(String resource) {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        return in == null ? H2Database.class.getResourceAsStream(resource) : in;
    }

    /**
     * Parses the provided {@code date} in {@code yyyyMMdd} format.
     */
    private static Optional<Date> parseYyyyMmDdDate(String date) {
        try {
            return Optional.of(new SimpleDateFormat("yyyyMMdd").parse(date));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Returns the {@code yyyyMMdd} {@link Date} stamp present in the given {@code filename}, if present.
     */
    private static Optional<Date> getDateFromFilename(String filename) {
        Matcher matcher = Pattern.compile("(.*([-_])?)(\\d{8})(.*)").matcher(filename);

        if (!matcher.matches()) {
            // if pattern is invalid, return immediately...
            return Optional.empty();
        } else {
            // yyyyMMdd stamp is always captured in RegExp's Group 3
            return parseYyyyMmDdDate(matcher.group(3));
        }
    }

    /**
     * The underlying database's name.
     * */
    public abstract String getDatabaseName();
}
package com.dazednconfused.catalauncher.database;

import static com.dazednconfused.catalauncher.database.H2Database.DATABASE_MIGRATIONS_RESOURCE_PATH;
import static org.assertj.core.api.Assertions.assertThat;

import com.dazednconfused.catalauncher.helper.Constants;
import com.dazednconfused.catalauncher.helper.result.Result;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class H2DatabaseTest {

    private static final String DB_BASE_MIGRATION_FILENAME_YEAR = "2024";
    private static final String DB_BASE_MIGRATION_FILENAME_MONTH = "07";
    private static final String DB_BASE_MIGRATION_FILENAME_DAY = "30";

    private static final String DB_BASE_MIGRATION_FILENAME = String.format(
        "%s%s%s_base.sql",
        DB_BASE_MIGRATION_FILENAME_YEAR,
        DB_BASE_MIGRATION_FILENAME_MONTH,
        DB_BASE_MIGRATION_FILENAME_DAY
    );

    @AfterAll
    public static void cleanup() {
        H2Database.destroy(TestDatabase.MOCK_DATABASE_NAME);
    }

    @Test
    void get_connection_success() {
        // prepare mock data ---
        TestDatabase db = new TestDatabase();

        // execute test ---
        try (Connection result = db.getConnection()){
            // verify assertions ---
            assertThat(result).isNotNull();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void wipe_success() {

        // prepare mock data ---
        TestDatabase db = new TestDatabase();
        final String MOCKED_TABLE = "testTable";

        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + MOCKED_TABLE;
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // pre-test assertions ---
        String tableExistsSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";

        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(tableExistsSql)) {
            pstmt.setString(1, MOCKED_TABLE.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertThat(rs.getInt(1)).isPositive();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // execute test ---
        Result<Throwable, ?> result = H2Database.wipe(db.getDatabaseName());

        // verify assertions ---
        assertThat(result).isNotNull();

        assertThat(result.toEither().isRight()).isTrue();

        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(tableExistsSql)) {
            pstmt.setString(1, MOCKED_TABLE.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertThat(rs.getInt(1)).isZero();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void self_wipe_success() {

        // prepare mock data ---
        TestDatabase db = new TestDatabase();
        final String MOCKED_TABLE = "testTable";

        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + MOCKED_TABLE;
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // pre-test assertions ---
        String tableExistsSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";

        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(tableExistsSql)) {
            pstmt.setString(1, MOCKED_TABLE.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertThat(rs.getInt(1)).isPositive();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // execute test ---
        Result<Throwable, ?> result = db.wipe();

        // verify assertions ---
        assertThat(result).isNotNull();

        assertThat(result.toEither().isRight()).isTrue();

        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(tableExistsSql)) {
            pstmt.setString(1, MOCKED_TABLE.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertThat(rs.getInt(1)).isZero();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void delete_success() {

        // prepare mock data ---
        TestDatabase db = new TestDatabase();

        // pre-test assertions ---
        try (Connection result = db.getConnection()){
            // verify assertions ---
            assertThat(result).isNotNull();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // execute test ---
        Result<Throwable, ?> result = H2Database.destroy(db.getDatabaseName());

        // verify assertions ---
        assertThat(result).isNotNull();

        assertThat(result.toEither().isRight()).isTrue();

        String dbFilePath = Constants.LAUNCHER_FILES + "/db/" + db.getDatabaseName() + ".mv.db";
        File dbFile = new File(dbFilePath);

        assertThat(dbFile).doesNotExist();
    }

    @Test
    void self_delete_success() {

        // prepare mock data ---
        TestDatabase db = new TestDatabase();

        // pre-test assertions ---
        try (Connection result = db.getConnection()){
            // verify assertions ---
            assertThat(result).isNotNull();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // execute test ---
        Result<Throwable, ?> result = db.destroy();

        // verify assertions ---
        assertThat(result).isNotNull();

        assertThat(result.toEither().isRight()).isTrue();

        String dbFilePath = Constants.LAUNCHER_FILES + "/db/" + db.getDatabaseName() + ".mv.db";
        File dbFile = new File(dbFilePath);

        assertThat(dbFile).doesNotExist();
    }

    @Test
    void does_table_exist_success() {

        // prepare mock data ---
        TestDatabase db = new TestDatabase();
        final String MOCKED_TABLE = "testTable";

        // pre-test assertions ---
        String tableExistsSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";

        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(tableExistsSql)) {
            pstmt.setString(1, MOCKED_TABLE.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertThat(rs.getInt(1)).isZero();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // execute test ---
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + MOCKED_TABLE;
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        boolean result = db.doesTableExist(MOCKED_TABLE);

        // verify assertions ---
        assertThat(result).isTrue();

        // cleanup ---
        db.wipe(); // this was individually tested in another unit test
    }

    @Test
    void does_column_exist_success() {

        // prepare mock data ---
        TestDatabase db = new TestDatabase();
        final String MOCKED_TABLE = "testTable";
        final String MOCKED_COLUMN = "validColumn";

        // pre-test assertions ---
        String tableExistsSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";

        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(tableExistsSql)) {
            pstmt.setString(1, MOCKED_TABLE.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertThat(rs.getInt(1)).isZero();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // execute test & verify assertions ---
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + MOCKED_TABLE + "(" +
                "id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY," +
                MOCKED_COLUMN + " VARCHAR(1) NULL " +
                ")";
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        assertThat(db.doesColumnExist(MOCKED_TABLE, MOCKED_COLUMN)).isTrue();
        assertThat(db.doesColumnExist(MOCKED_TABLE, "invalidColumn")).isFalse();

        // cleanup ---
        db.wipe(); // this was individually tested in another unit test
    }

    @Test
    void get_database_migration_files_success() {

        // execute test ---
        Result<Throwable, List<String>> result = H2Database.getDatabaseMigrationFiles();

        // verify assertions ---
        assertThat(result).isNotNull(); // assert non-null result

        assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success

        List<String> migrations = result.toEither().get().getResult().orElseThrow();

        assertThat(migrations).isNotEmpty(); // assert that Result's Success is not empty

        assertThat(migrations).containsOnlyOnce(DB_BASE_MIGRATION_FILENAME); // assert that Result's Success contains, at the very least, the base migration
    }

    @Test
    void get_database_migration_files_dated_after_success() throws ParseException {

        // prepare mock data ---
        Date MOCKED_DATE = new SimpleDateFormat("yyyyMMdd").parse(
            DB_BASE_MIGRATION_FILENAME_YEAR + DB_BASE_MIGRATION_FILENAME_MONTH + DB_BASE_MIGRATION_FILENAME_DAY
        );

        // execute test ---
        List<File> result = H2Database.getDatabaseMigrationFilesDatedAfter(MOCKED_DATE);

        // verify assertions ---
        assertThat(result).isNotNull();

        assertThat(result).hasSize(5);

        String VALID_MIGRATION_FILE_1 = "20990101_valid_migration_1.sql";
        String VALID_MIGRATION_FILE_2 = "20990101_valid_migration_2.sql";
        String VALID_MIGRATION_FILE_3 = "20990102_valid_migration_3.sql";
        String VALID_MIGRATION_FILE_4 = "20990201_valid_migration_4.sql";
        String VALID_MIGRATION_FILE_5 = "20990202_valid_migration_5.sql";

        assertThat(result).containsAll(Stream.of(
                VALID_MIGRATION_FILE_1,
                VALID_MIGRATION_FILE_2,
                VALID_MIGRATION_FILE_3,
                VALID_MIGRATION_FILE_4,
                VALID_MIGRATION_FILE_5)
            .map(f -> DATABASE_MIGRATIONS_RESOURCE_PATH + f)
            .map(File::new)
            .collect(Collectors.toList())
        );
    }

    @Test
    void execute_sql_resource_success() {

        // prepare mock data ---
        TestDatabase db = new TestDatabase();

        // pre-test assertions ---
        assertThat(db.doesTableExist("sample")).isFalse();

        // execute test ---
        Result<Throwable, Boolean> result = db.executeSqlResource("db/scripts/sample.sql");

        // verify assertions ---
        assertThat(result).isNotNull(); // assert non-null result

        assertThat(result.toEither().isRight()).isTrue(); // assert that Result is Success

        assertThat(result.toEither().get().getResult().isEmpty()).isFalse(); // assert that Result's Success is not empty
        assertThat(result.toEither().get().getResult().get()).isFalse();

        assertThat(db.doesTableExist("sample")).isTrue();
    }

    /**
     * Mock database used to test basic functionality of the {@link H2Database} interface.
     * */
    private static class TestDatabase extends H2Database {

        public static String MOCK_DATABASE_NAME = "test";

        @Override
        public String getDatabaseName() {
            return MOCK_DATABASE_NAME;
        }
    }
}
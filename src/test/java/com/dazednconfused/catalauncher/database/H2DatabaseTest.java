package com.dazednconfused.catalauncher.database;

import static org.assertj.core.api.Assertions.assertThat;

import com.dazednconfused.catalauncher.helper.Constants;
import com.dazednconfused.catalauncher.helper.result.Result;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class H2DatabaseTest {

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
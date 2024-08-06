package com.dazednconfused.catalauncher.database.h2;

import static org.assertj.core.api.Assertions.assertThat;

import com.dazednconfused.catalauncher.helper.Paths;
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
    void destroy_success() {

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

        String dbFilePath = Paths.getLauncherFiles() + "/db/" + db.getDatabaseName() + ".mv.db";
        File dbFile = new File(dbFilePath);

        assertThat(dbFile).doesNotExist();
    }

    @Test
    void self_destroy_success() {

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

        String dbFilePath = Paths.getLauncherFiles() + "/db/" + db.getDatabaseName() + ".mv.db";
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
    void reset_success() {

        // prepare mock data ---
        TestDatabase db = new TestDatabase();
        final String MOCKED_TABLE_1 = "testTable1";
        final String MOCKED_TABLE_2 = "testTable2";

        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            String table1Sql = "CREATE TABLE IF NOT EXISTS " + MOCKED_TABLE_1 + " (" +
                "id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                "test TEXT NOT NULL " +
                ")";

            String table2Sql = "CREATE TABLE IF NOT EXISTS " + MOCKED_TABLE_2 + " (" +
                "id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                "child_id INT NOT NULL, " +
                "FOREIGN KEY (child_id) REFERENCES " + MOCKED_TABLE_1 + "(id)" +
                ")";

            stmt.execute(table1Sql);
            stmt.execute(table2Sql);

            String data1Sql = "INSERT INTO " + MOCKED_TABLE_1 + " (test) VALUES ('testValue')";
            stmt.execute(data1Sql);

            String data2Sql = "INSERT INTO " + MOCKED_TABLE_2 + " (child_id) VALUES (1)";
            stmt.execute(data2Sql);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // pre-test assertions ---
        String tableExistsSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
        String tableHasDataSql = "SELECT COUNT(*) AS rowcount FROM ";

        // MOCKED_TABLE_1 exists -
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(tableExistsSql)) {
            pstmt.setString(1, MOCKED_TABLE_1.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertThat(rs.getInt(1)).isPositive();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // MOCKED_TABLE_2 exists -
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(tableExistsSql)) {
            pstmt.setString(1, MOCKED_TABLE_2.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertThat(rs.getInt(1)).isPositive();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // MOCKED_TABLE_1 has data -
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(tableHasDataSql + MOCKED_TABLE_1)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertThat(rs.getInt("rowcount")).isPositive();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // MOCKED_TABLE_2 has data -
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(tableHasDataSql + MOCKED_TABLE_2)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertThat(rs.getInt("rowcount")).isPositive();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // execute test ---
        Result<Throwable, ?> result = H2Database.reset(db.getDatabaseName());

        // verify assertions ---
        assertThat(result).isNotNull();

        assertThat(result.toEither().isRight()).isTrue();

        // MOCKED_TABLE_1 exists -
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(tableExistsSql)) {
            pstmt.setString(1, MOCKED_TABLE_1.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertThat(rs.getInt(1)).isPositive();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // MOCKED_TABLE_2 exists -
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(tableExistsSql)) {
            pstmt.setString(1, MOCKED_TABLE_2.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertThat(rs.getInt(1)).isPositive();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // MOCKED_TABLE_1 has no data -
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(tableHasDataSql + MOCKED_TABLE_1)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertThat(rs.getInt("rowcount")).isZero();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // MOCKED_TABLE_2 has data -
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(tableHasDataSql + MOCKED_TABLE_2)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertThat(rs.getInt("rowcount")).isZero();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // cleanup ---
        db.wipe(); // this was individually tested in another unit test
    }

    @Test
    void self_reset_success() {

        // prepare mock data ---
        TestDatabase db = new TestDatabase();
        final String MOCKED_TABLE_1 = "testTable1";
        final String MOCKED_TABLE_2 = "testTable2";

        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            String table1Sql = "CREATE TABLE IF NOT EXISTS " + MOCKED_TABLE_1 + " (" +
                "id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                "test TEXT NOT NULL " +
                ")";

            String table2Sql = "CREATE TABLE IF NOT EXISTS " + MOCKED_TABLE_2 + " (" +
                "id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                "child_id INT NOT NULL, " +
                "FOREIGN KEY (child_id) REFERENCES " + MOCKED_TABLE_1 + "(id)" +
                ")";

            stmt.execute(table1Sql);
            stmt.execute(table2Sql);

            String data1Sql = "INSERT INTO " + MOCKED_TABLE_1 + " (test) VALUES ('testValue')";
            stmt.execute(data1Sql);

            String data2Sql = "INSERT INTO " + MOCKED_TABLE_2 + " (child_id) VALUES (1)";
            stmt.execute(data2Sql);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // pre-test assertions ---
        String tableExistsSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
        String tableHasDataSql = "SELECT COUNT(*) AS rowcount FROM ";

        // MOCKED_TABLE_1 exists -
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(tableExistsSql)) {
            pstmt.setString(1, MOCKED_TABLE_1.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertThat(rs.getInt(1)).isPositive();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // MOCKED_TABLE_2 exists -
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(tableExistsSql)) {
            pstmt.setString(1, MOCKED_TABLE_2.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertThat(rs.getInt(1)).isPositive();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // MOCKED_TABLE_1 has data -
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(tableHasDataSql + MOCKED_TABLE_1)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertThat(rs.getInt("rowcount")).isPositive();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // MOCKED_TABLE_2 has data -
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(tableHasDataSql + MOCKED_TABLE_2)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertThat(rs.getInt("rowcount")).isPositive();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // execute test ---
        Result<Throwable, ?> result = db.reset();

        // verify assertions ---
        assertThat(result).isNotNull();

        assertThat(result.toEither().isRight()).isTrue();

        // MOCKED_TABLE_1 exists -
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(tableExistsSql)) {
            pstmt.setString(1, MOCKED_TABLE_1.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertThat(rs.getInt(1)).isPositive();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // MOCKED_TABLE_2 exists -
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(tableExistsSql)) {
            pstmt.setString(1, MOCKED_TABLE_2.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertThat(rs.getInt(1)).isPositive();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // MOCKED_TABLE_1 has no data -
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(tableHasDataSql + MOCKED_TABLE_1)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertThat(rs.getInt("rowcount")).isZero();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // MOCKED_TABLE_2 has data -
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(tableHasDataSql + MOCKED_TABLE_2)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    assertThat(rs.getInt("rowcount")).isZero();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // cleanup ---
        db.wipe(); // this was individually tested in another unit test
    }

    /**
     * Mock database used to test basic functionality of the {@link H2Database} interface.
     * */
    private static class TestDatabase extends H2Database {

        public static String MOCK_DATABASE_NAME = "h2TestDatabase";

        @Override
        public String getDatabaseName() {
            return MOCK_DATABASE_NAME;
        }
    }
}
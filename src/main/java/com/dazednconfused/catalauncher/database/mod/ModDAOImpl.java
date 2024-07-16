package com.dazednconfused.catalauncher.database.mod;

import com.dazednconfused.catalauncher.database.H2Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModDAOImpl extends H2Database implements ModDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModDAOImpl.class);

    private static final String DATABASE_FILE = "mods";
    private static final String MODS_TABLE_NAME = "mod";
    private static final String MOD_FILES_TABLE_NAME = "modfile";

    @Override
    public String getDatabaseName() {
        return DATABASE_FILE;
    }

    @Override
    public void initializeTables() {
        LOGGER.info("Initializing tables...");

        this.createModsTable();
        this.createModfilesTable();

        LOGGER.info("Table initialization completed");
    }

    /**
     * Initializes the {@link #MODS_TABLE_NAME} table.
     * */
    private void createModsTable() {
        LOGGER.trace("Initializing Mods table...");

        String sql = "CREATE TABLE IF NOT EXISTS " + MODS_TABLE_NAME + " (" +
            "id INT PRIMARY KEY, " +
            "name VARCHAR(255)," +
            "created_date DATETIME," +
            "updated_date DATETIME" +
            ")";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.trace("Mods table initialized successfully");
        } catch (SQLException e) {
            LOGGER.error("An error occurred while creating table [{}]", MODS_TABLE_NAME, e);
        }
    }

    /**
     * Initializes the {@link #MOD_FILES_TABLE_NAME} table.
     * */
    private void createModfilesTable() {
        LOGGER.trace("Initializing Modfiles table...");

        String sql = "CREATE TABLE IF NOT EXISTS " + MOD_FILES_TABLE_NAME + " (" +
            "id INT PRIMARY KEY, " +
            "path TEXT," +
            "hash TEXT," +
            "created_date DATETIME," +
            "updated_date DATETIME" +
            ")";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOGGER.trace("Modfiles table initialized successfully");
        } catch (SQLException e) {
            LOGGER.error("An error occurred while creating table [{}]", MOD_FILES_TABLE_NAME, e);
        }
    }

//
//    @Override
//    public void insertUser(User user) {
//        String sql = "INSERT INTO users (id, name, email) VALUES (?, ?, ?)";
//        try (Connection conn = H2DatabaseUtil.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setInt(1, user.getId());
//            pstmt.setString(2, user.getName());
//            pstmt.setString(3, user.getEmail());
//            pstmt.executeUpdate();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public User getUserById(int id) {
//        String sql = "SELECT * FROM users WHERE id = ?";
//        try (Connection conn = H2DatabaseUtil.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setInt(1, id);
//            ResultSet rs = pstmt.executeQuery();
//            if (rs.next()) {
//                return new User(rs.getInt("id"), rs.getString("name"), rs.getString("email"));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    @Override
//    public void updateUser(User user) {
//        String sql = "UPDATE users SET name = ?, email = ? WHERE id = ?";
//        try (Connection conn = H2DatabaseUtil.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setString(1, user.getName());
//            pstmt.setString(2, user.getEmail());
//            pstmt.setInt(3, user.getId());
//            pstmt.executeUpdate();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void deleteUser(int id) {
//        String sql = "DELETE FROM users WHERE id = ?";
//        try (Connection conn = H2DatabaseUtil.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setInt(1, id);
//            pstmt.executeUpdate();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
}
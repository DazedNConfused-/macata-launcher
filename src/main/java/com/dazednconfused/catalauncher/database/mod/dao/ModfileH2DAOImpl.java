package com.dazednconfused.catalauncher.database.mod.dao;

import com.dazednconfused.catalauncher.database.DAOException;
import com.dazednconfused.catalauncher.database.migration.MigrateableH2Database;
import com.dazednconfused.catalauncher.database.mod.entity.ModfileEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModfileH2DAOImpl extends MigrateableH2Database implements ModfileDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModfileH2DAOImpl.class);

    /**
     * Constructor.
     * */
    public ModfileH2DAOImpl() {
    }

    @Override
    public String getDatabaseName() {
        return DATABASE_FILE;
    }

    @Override
    public ModfileEntity insert(ModfileEntity entity) throws DAOException {
        LOGGER.debug("Inserting ModfileEntity [{}]...", entity);

        String sql = "INSERT INTO " + TABLE_NAME + "" +
            "(mod_id, path, hash, created_date, updated_date) " +
            "VALUES " +
            "(?, ?, ?, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP())";

        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, entity.getModId());
            pstmt.setString(2, entity.getPath());
            pstmt.setString(3, entity.getHash());

            pstmt.executeUpdate();

            return this.getLatestGeneratedId(pstmt).map(this::findById).orElseThrow(DAOException::new).orElseThrow(DAOException::new);
        } catch (SQLException e) {
            LOGGER.error("An error occurred while inserting entity [{}]", entity, e);
            throw new DAOException(e);
        }
    }

    @Override
    public ModfileEntity update(ModfileEntity entity) throws DAOException {
        Optional<ModfileEntity> originalEntity = this.findById(entity.getId());

        if (originalEntity.isEmpty()) {
            throw new DAOException("No entity with id [" + entity.getId() + "] found");
        }

        LOGGER.debug("Updating ModfileEntity from [{}] to [{}]...", originalEntity.get(), entity);

        String sql = "UPDATE " + TABLE_NAME + " SET " +
            "path = ?, " +
            "hash = ?, " +
            "updated_date = CURRENT_TIMESTAMP() " +
            "WHERE id = ?";

        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entity.getPath());
            pstmt.setString(2, entity.getHash());
            pstmt.setLong(3, entity.getId());

            pstmt.executeUpdate();

            return this.findById(entity.getId()).orElseThrow(DAOException::new);
        } catch (SQLException e) {
            LOGGER.error("An error occurred while updating entity [{}]", entity, e);
            throw new DAOException(e);
        }
    }

    @Override
    public ModfileEntity buildFromResultSet(ResultSet rs) throws DAOException {
        LOGGER.trace("Building ModfileEntity from ResultSet [{}]...", rs);

        try {
            return ModfileEntity.builder()
                .id(rs.getLong("id"))
                .modId(rs.getLong("mod_id"))
                .path(rs.getString("path"))
                .hash(rs.getString("hash"))
                .createdDate(rs.getTimestamp("created_date"))
                .updatedDate(rs.getTimestamp("updated_date"))
                .build();
        } catch (SQLException e) {
            LOGGER.error("An error occurred while building entity from ResultSet [{}]", rs, e);
            throw new DAOException(e);
        }
    }

    @Override
    public String getDatabaseMigrationsResourcePath() {
        return DATABASE_MIGRATIONS_DEFAULT_RESOURCE_ROOT_PATH + "mod/";
    }
}
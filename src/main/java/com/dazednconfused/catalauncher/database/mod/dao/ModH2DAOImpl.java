package com.dazednconfused.catalauncher.database.mod.dao;

import com.dazednconfused.catalauncher.database.base.DAOException;
import com.dazednconfused.catalauncher.database.h2.migration.MigrateableH2Database;
import com.dazednconfused.catalauncher.database.mod.entity.ModEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModH2DAOImpl extends MigrateableH2Database implements ModDAO {

    public static final String MODS_TABLE_NAME = "mod";

    private static final Logger LOGGER = LoggerFactory.getLogger(ModH2DAOImpl.class);

    /**
     * Constructor.
     */
    public ModH2DAOImpl() {
        super(false);
    }

    /**
     * Constructor.
     */
    public ModH2DAOImpl(boolean applyMigrations) {
        super(applyMigrations);
    }

    @Override
    public String getDatabaseName() {
        return DATABASE_FILE;
    }

    @Override
    public ModEntity insert(ModEntity entity) throws DAOException {
        LOGGER.debug("Inserting ModEntity [{}]...", entity);

        String sql = "INSERT INTO " + MODS_TABLE_NAME + " " +
            "(name, modinfo, created_date, updated_date) " +
            "VALUES " +
            "(?, ?, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP())";

        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, entity.getName());
            pstmt.setString(2, entity.getModinfo());

            pstmt.executeUpdate();

            return this.getLatestGeneratedId(pstmt).map(this::findById).orElseThrow(DAOException::new).orElseThrow(DAOException::new);
        } catch (SQLException e) {
            LOGGER.error("An error occurred while inserting entity [{}]", entity, e);
            throw new DAOException(e);
        }
    }

    @Override
    public int bulkInsert(Collection<ModEntity> entities) throws DAOException {

        if (entities == null || entities.isEmpty()) {
            LOGGER.warn("No entities provided for bulk insert. No operation shall be performed.");
            return 0;
        }

        LOGGER.debug("Bulk inserting [{}] ModEntity(ies)...", entities.size());

        String sql = "INSERT INTO " + MODS_TABLE_NAME + " " +
            "(name, modinfo, created_date, updated_date) " +
            "VALUES " +
            "(?, ?, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP())";

        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (ModEntity entity : entities) {
                pstmt.setString(1, entity.getName());
                pstmt.setString(2, entity.getModinfo());
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();

            int insertedCount = Arrays.stream(results).filter(result -> result > 0).sum();
            LOGGER.debug("Bulk inserted [{}] ModEntity(ies)", insertedCount);
            return insertedCount;
        } catch (SQLException e) {
            LOGGER.error("An error occurred during bulk insert of ModEntity(ies)", e);
            throw new DAOException(e);
        }
    }

    @Override
    public ModEntity update(ModEntity entity) throws DAOException {
        Optional<ModEntity> originalEntity = this.findById(entity.getId());

        if (originalEntity.isEmpty()) {
            throw new DAOException("No entity with id [" + entity.getId() + "] found");
        }

        LOGGER.debug("Updating ModEntity from [{}] to [{}]...", originalEntity.get(), entity);

        String sql = "UPDATE " + MODS_TABLE_NAME + " SET " +
            "name = ?, " +
            "modinfo = ?, " +
            "updated_date = CURRENT_TIMESTAMP() " +
            "WHERE id = ?";

        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entity.getName());
            pstmt.setString(2, entity.getModinfo());
            pstmt.setLong(3, entity.getId());

            pstmt.executeUpdate();

            return this.findById(entity.getId()).orElseThrow(DAOException::new);
        } catch (SQLException e) {
            LOGGER.error("An error occurred while updating entity [{}]", entity, e);
            throw new DAOException(e);
        }
    }

    @Override
    public ModEntity buildFromResultSet(ResultSet rs) throws DAOException {
        LOGGER.trace("Building ModEntity from ResultSet [{}]...", rs);

        try {
            return ModEntity.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .modinfo(rs.getString("modinfo"))
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
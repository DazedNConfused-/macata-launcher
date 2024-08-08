package com.dazednconfused.catalauncher.database.mod.dao;

import com.dazednconfused.catalauncher.database.base.BaseDAO;
import com.dazednconfused.catalauncher.database.base.BaseEntity;
import com.dazednconfused.catalauncher.database.base.DAOException;
import com.dazednconfused.catalauncher.database.mod.entity.ModfileEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface ModfileDAO extends BaseDAO<ModfileEntity> {

    String TABLE_NAME = "modfile";
    String DATABASE_FILE = "mods";

    @Override
    default String getTableName() {
        return TABLE_NAME;
    }

    /**
     * Finds the given {@link BaseEntity} by ID.
     * */
    default List<ModfileEntity> findAllByModId(long modId) throws DAOException {
        LOGGER.debug("Finding ModfileEntity(s) associated to modId [{}]...", modId);

        String sql = "SELECT * FROM " + getTableName() + " WHERE mod_id = ?";
        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, modId);
            ResultSet rs = pstmt.executeQuery();

            List<ModfileEntity> result = new ArrayList<>();
            while (rs.next()) {
                result.add(this.buildFromResultSet(rs));
            }

            return result;
        } catch (SQLException e) {
            LOGGER.error("An error occurred while retrieving ModfileEntity(ies) associated to modID [{}]", modId, e);
            throw new DAOException(e);
        }
    }

    /**
     * Deletes all the {@link ModfileEntity}(ies) associated with the given {@code modId}.
     * */
    default int deleteAllByModId(long modId) throws DAOException {
        LOGGER.debug("Deleting ModfileEntity(s) associated to modId [{}]...", modId);

        String sql = "DELETE FROM " + getTableName() + " WHERE mod_id = ? ";

        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, modId);

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("An error occurred while deleting ModfileEntity(s) associated to modId [{}]", modId, e);
            throw new DAOException(e);
        }
    }
}

package com.dazednconfused.catalauncher.database.mod.repository;

import com.dazednconfused.catalauncher.database.BaseDAO;
import com.dazednconfused.catalauncher.database.DAOException;
import com.dazednconfused.catalauncher.database.mod.entity.ModEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public interface ModRepository extends BaseDAO<ModEntity> {

    Logger LOGGER = LoggerFactory.getLogger(ModRepository.class);


    @Override
    default ModEntity buildFromResultSet(ResultSet rs) throws DAOException {
        throw new RuntimeException("Method not implemented at Repository level");
    }

    @Override
    default String getTableName() {
        throw new RuntimeException("Method not implemented at Repository level");
    }

    @Override
    default Map.Entry<Connection, PreparedStatement> getTableCreationStatement() throws DAOException {
        throw new RuntimeException("Method not implemented at Repository level");
    }
}

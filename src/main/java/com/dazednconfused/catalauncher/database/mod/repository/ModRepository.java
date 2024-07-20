package com.dazednconfused.catalauncher.database.mod.repository;

import com.dazednconfused.catalauncher.database.DAOException;
import com.dazednconfused.catalauncher.database.mod.dao.ModDAO;
import com.dazednconfused.catalauncher.database.mod.entity.ModEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ModRepository extends ModDAO {

    Logger LOGGER = LoggerFactory.getLogger(ModRepository.class);

    @Override
    default ModEntity buildFromResultSet(ResultSet rs) throws DAOException {
        throw new RuntimeException("Method not implemented at Repository level");
    }

    @Override
    default Map.Entry<Connection, PreparedStatement> getTableCreationStatement() throws DAOException {
        throw new RuntimeException("Method not implemented at Repository level");
    }
}

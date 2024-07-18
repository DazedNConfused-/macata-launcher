package com.dazednconfused.catalauncher.database.mod.repository;

import com.dazednconfused.catalauncher.database.DAOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ModRepository {

    Logger LOGGER = LoggerFactory.getLogger(ModRepository.class);

    /**
     * Initializes this Repository's table(s), if it didn't exist already.
     * */
    void initializeTables() throws DAOException;

}

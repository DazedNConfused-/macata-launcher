package com.dazednconfused.catalauncher.database.mod.repository;

import com.dazednconfused.catalauncher.database.DAOException;
import com.dazednconfused.catalauncher.database.H2Database;

import com.dazednconfused.catalauncher.database.mod.dao.ModDAO;
import com.dazednconfused.catalauncher.database.mod.dao.ModH2DAOImpl;
import com.dazednconfused.catalauncher.database.mod.dao.ModfileDAO;
import com.dazednconfused.catalauncher.database.mod.dao.ModfileH2DAOImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModH2RepositoryImpl extends H2Database implements ModRepository {

    public static final String MODS_TABLE_NAME = "mod";

    private static final Logger LOGGER = LoggerFactory.getLogger(ModH2RepositoryImpl.class);

    private static final String DATABASE_FILE = "mods";

    private final ModDAO modDAO;
    private final ModfileDAO modfileDAO;

    public ModH2RepositoryImpl() {
        this.modDAO = new ModH2DAOImpl();
        this.modfileDAO = new ModfileH2DAOImpl();
    }

    @Override
    public void initializeTables() throws DAOException {
        this.modDAO.initializeTable();
        this.modfileDAO.initializeTable();
    }

    @Override
    public String getDatabaseName() {
        return DATABASE_FILE;
    }
}
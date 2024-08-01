package com.dazednconfused.catalauncher.database.migration;

import com.dazednconfused.catalauncher.database.DAOException;

/**
 * Generic interface for DAOs that allow migrations to be applied on them.
 * */
public interface Migrateable {

    /**
     * Searches for and applies all pending migrations to this DAO's database.
     * */
    void applyAllPendingMigrations() throws DAOException;

    /**
     * Applies an individual {@code migration} to this DAO's database.
     * */
    void applyMigration(String migration) throws DAOException;
}

package com.dazednconfused.catalauncher.database.base;

/**
 * Generic interface for DAOs that allow migrations to be applied on them.
 * */
public interface MigrateableDatabase extends BaseDatabase {

    /**
     * Searches for and applies all pending migrations to this DAO's database.
     * */
    void applyAllPendingMigrations() throws DAOException;

    /**
     * Applies an individual {@code migration} to this DAO's database.
     * */
    void applyMigration(String migration) throws DAOException;
}

package com.dazednconfused.catalauncher.database.base;

import java.sql.Connection;

/**
 * Generic interface for basic database implementations.
 * */
public interface BaseDatabase {

    /**
     * Opens a connection to this database.
     *
     * @apiNote Callers of this method are in charge of properly handling and disposing of this connection upon use.
     * */
    Connection getConnection();

}

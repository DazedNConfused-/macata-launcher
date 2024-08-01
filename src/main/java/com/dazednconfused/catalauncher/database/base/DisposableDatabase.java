package com.dazednconfused.catalauncher.database.base;

import com.dazednconfused.catalauncher.helper.result.Result;

import java.sql.Connection;

/**
 * Generic interface for self-cleaning database implementations.
 * */
public interface DisposableDatabase extends BaseDatabase {

    /**
     * Completely cleans the given {@code database} of any and all data, but leaves the underlying schema intact.
     *
     * @apiNote Callers of this method are responsible for making sure any and all open {@link Connection}s are properly
     *          disposed of before triggering the wipe.
     * */
    Result<Throwable, Object> reset();

    /**
     * Completely wipes this database of any and all data.
     *
     * @apiNote Callers of this method are responsible for making sure any and all open {@link Connection}s are properly
     *          disposed of before triggering the wipe.
     * */
    Result<Throwable, Object> wipe();

    /**
     * Safely shuts down the database.
     *
     * @apiNote Callers of this method are responsible for making sure any and all open {@link Connection}s are properly
     *          disposed of before triggering the shutdown process.
     * */
    Result<Throwable, Object> shutdown();

    /**
     * Completely destroys this database.
     *
     * @apiNote Callers of this method are responsible for making sure any and all open {@link Connection}s are properly
     *          disposed of before triggering the wipe.
     * */
    Result<Throwable, Object> destroy();
}

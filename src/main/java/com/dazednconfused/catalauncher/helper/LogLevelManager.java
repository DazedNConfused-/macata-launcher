package com.dazednconfused.catalauncher.helper;

import org.apache.log4j.Level;

public class LogLevelManager {

    /**
     * Dynamically changes the global logging level to the given {@link Level}.
     * */
    public static void changeGlobalLogLevelTo(Level level) {
        org.apache.log4j.Logger logger4j = org.apache.log4j.Logger.getRootLogger();
        logger4j.setLevel(org.apache.log4j.Level.toLevel(level.toString()));
    }
}

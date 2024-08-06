package com.dazednconfused.catalauncher.helper;

import com.dazednconfused.catalauncher.Application;

/**
 * A set of common filesystem {@link Paths} used throughout the application.
 * */
public class Paths {

    /**
     * Retrieves the {@link Application}'s root folder - that is, the filesystem path where the binary is currently executing.
     * Most defined {@link Paths} depend on this one.
     * */
    public static String getLauncherRootFolder() {
        return Application.getRootFolder();
    }

    /**
     * Retrieves the {@link Application}'s main folder.
     *
     * <pre> {@code
     *  getLauncherRootFolder() + "/.macatalauncher"
     * }</pre>
     * */
    public static String getLauncherFiles() {
        return getLauncherRootFolder() + "/.macatalauncher";
    }

    /**
     * Retrieves the {@link Application}'s main logfile.
     *
     * <pre> {@code
     *  getLauncherRootFolder() + "/.macatalauncher/logs/main.log"
     * }</pre>
     * */
    public static String getLogFilePath() {
        return getLauncherFiles() + "/logs/main.log";
    }

    /**
     * Retrieves the {@link Application}'s custom savefile path.
     *
     * <pre> {@code
     *  getLauncherRootFolder() + "/saves/"
     * }</pre>
     * */
    public static String getCustomSavePath() {
        return getLauncherRootFolder() + "/saves/";
    }

    /**
     * Retrieves the {@link Application}'s custom path for trashed savefiles.
     *
     * <pre> {@code
     *  getLauncherRootFolder() + "/trashed/saves/"
     * }</pre>
     * */
    public static String getCustomTrashedSavePath() {
        return getLauncherRootFolder() + "/trashed/saves/";
    }

    /**
     * Retrieves the {@link Application}'s custom path for backup-ed savefiles.
     *
     * <pre> {@code
     *  getLauncherRootFolder() + "/backups"
     * }</pre>
     * */
    public static String getSaveBackupPath() {
        return getLauncherRootFolder() + "/backups";
    }

    /**
     * Retrieves the {@link Application}'s custom {@code userdir/} directory.
     *
     * <pre> {@code
     *  getLauncherRootFolder() + "/userdir/"
     * }</pre>
     * */
    public static String getCustomUserDir() {
        return getLauncherRootFolder() + "/userdir/";
    }

    /**
     * Retrieves the {@link Application}'s custom {@code userdir/sound} directory.
     *
     * <pre> {@code
     *  getLauncherRootFolder() + "/userdir/sound/"
     * }</pre>
     * */
    public static String getCustomSoundpacksDir() {
        return getCustomUserDir() + "sound/";
    }

    /**
     * Retrieves the {@link Application}'s custom {@code userdir/mods} directory.
     *
     * <pre> {@code
     *  getLauncherRootFolder() + "/userdir/mods/"
     * }</pre>
     * */
    public static String getCustomModsDir() {
        return getCustomUserDir() + "mods/";
    }

    /**
     * Retrieves the {@link Application}'s database(s) directory.
     *
     * <pre> {@code
     *  getLauncherRootFolder() + "/.macatalauncher/db"
     * }</pre>
     * */
    public static String getDatabaseDirectory() {
        return getLauncherFiles() + "/db";
    }

    // java resources
    public static final String RESOURCE_ICONS_PATH = "icon/svg";
}

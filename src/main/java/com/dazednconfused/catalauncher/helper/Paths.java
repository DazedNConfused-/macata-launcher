package com.dazednconfused.catalauncher.helper;

import com.dazednconfused.catalauncher.Application;

import java.nio.file.Path;

/**
 * A set of common filesystem {@link Paths} used throughout the application.
 * */
public class Paths {

    /**
     * Retrieves the {@link Application}'s root folder - that is, the filesystem path where the binary is currently executing.
     * Most defined {@link Paths} depend on this one.
     * */
    public static Path getLauncherRootFolder() {
        return Path.of(Application.getRootFolder());
    }

    /**
     * Retrieves the {@link Application}'s main folder.
     *
     * <pre> {@code
     *  getLauncherRootFolder() + "/.macatalauncher"
     * }</pre>
     * */
    public static Path getLauncherFiles() {
        return getLauncherRootFolder().resolve(".macatalauncher");
    }

    /**
     * Retrieves the {@link Application}'s main logfile.
     *
     * <pre> {@code
     *  getLauncherRootFolder() + "/.macatalauncher/logs/main.log"
     * }</pre>
     * */
    public static Path getLogFilePath() {
        return getLauncherFiles().resolve("logs/main.log");
    }

    /**
     * Retrieves the {@link Application}'s custom savefile path.
     *
     * <pre> {@code
     *  getLauncherRootFolder() + "/saves/"
     * }</pre>
     * */
    public static Path getCustomSavePath() {
        return getLauncherRootFolder().resolve("saves");
    }

    /**
     * Retrieves the {@link Application}'s custom path for trashed files.
     *
     * <pre> {@code
     *  getLauncherRootFolder() + "/trashed"
     * }</pre>
     * */
    public static Path getCustomTrashedPath() {
        return getLauncherRootFolder().resolve("trashed");
    }

    /**
     * Retrieves the {@link Application}'s custom path for trashed savefiles.
     *
     * <pre> {@code
     *  getLauncherRootFolder() + "/trashed/saves/"
     * }</pre>
     * */
    public static Path getCustomTrashedSavePath() {
        return getCustomTrashedPath().resolve("saves");
    }

    /**
     * Retrieves the {@link Application}'s custom path for backup-ed savefiles.
     *
     * <pre> {@code
     *  getLauncherRootFolder() + "/backups"
     * }</pre>
     * */
    public static Path getSaveBackupPath() {
        return getLauncherRootFolder().resolve("backups");
    }

    /**
     * Retrieves the {@link Application}'s custom {@code userdir/} directory.
     *
     * <pre> {@code
     *  getLauncherRootFolder() + "/userdir/"
     * }</pre>
     * */
    public static Path getCustomUserDir() {
        return getLauncherRootFolder().resolve("userdir");
    }

    /**
     * Retrieves the {@link Application}'s custom {@code userdir/sound} directory.
     *
     * <pre> {@code
     *  getLauncherRootFolder() + "/userdir/sound/"
     * }</pre>
     * */
    public static Path getCustomSoundpacksDir() {
        return getCustomUserDir().resolve("sound");
    }

    /**
     * Retrieves the {@link Application}'s custom {@code userdir/mods} directory.
     *
     * <pre> {@code
     *  getLauncherRootFolder() + "/userdir/mods/"
     * }</pre>
     * */
    public static Path getCustomModsDir() {
        return getCustomUserDir().resolve("mods");
    }

    /**
     * Retrieves the {@link Application}'s custom path for trashed mods.
     *
     * <pre> {@code
     *  getLauncherRootFolder() + "/trashed/mods/"
     * }</pre>
     * */
    public static Path getCustomTrashedModsPath() {
        return getCustomTrashedPath().resolve("mods");
    }

    /**
     * Retrieves the {@link Application}'s database(s) directory.
     *
     * <pre> {@code
     *  getLauncherRootFolder() + "/.macatalauncher/db"
     * }</pre>
     * */
    public static Path getDatabaseDirectory() {
        return getLauncherFiles().resolve("db");
    }

    // java resources
    public static final String RESOURCE_ICONS_PATH = "icon/svg";
}

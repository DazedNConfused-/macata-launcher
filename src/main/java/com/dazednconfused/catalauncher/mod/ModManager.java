package com.dazednconfused.catalauncher.mod;


import static com.dazednconfused.catalauncher.helper.Constants.CUSTOM_MODS_DIR;

import io.vavr.control.Try;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModManager.class);

    /**
     * Returns all mods currently found in {@link com.dazednconfused.catalauncher.helper.Constants#CUSTOM_MODS_DIR}.
     * */
    public static List<File> listAllMods() {
        LOGGER.debug("Listing all mods...");
        return Arrays.stream(Objects.requireNonNull(getModsFolder().listFiles()))
                .filter(file -> !file.getName().equals(".DS_Store"))
                .collect(Collectors.toList());
    }

    /**
     * Deletes given {@code toBeDeleted} mod.
     * */
    public static void deleteMod(File toBeDeleted) {
        LOGGER.info("Deleting mod [{}]...", toBeDeleted);
        Try.run(() -> FileUtils.deleteDirectory(toBeDeleted)).onFailure(t -> LOGGER.error("There was an error deleting mod [{}]", toBeDeleted, t));
    }

    /**
     * Installs given {@code toBeInstalled} mod inside {@link com.dazednconfused.catalauncher.helper.Constants#CUSTOM_MODS_DIR}.
     * */
    public static void installMod(File toBeInstalled, Consumer<Path> onDoneCallback) {
        LOGGER.info("Installing mod [{}]...", toBeInstalled);
        File installInto = new File(getModsFolder().getPath() + "/" + toBeInstalled.getName());

        Try.run(() -> {
            LOGGER.debug("Copying [{}] into [{}]...", toBeInstalled, installInto);
            FileUtils.copyDirectory(toBeInstalled, installInto);
        }).onFailure(t -> LOGGER.error("There was an error installing mod [{}]", toBeInstalled, t)).andThen(() -> onDoneCallback.accept(installInto.toPath()));
    }

    /**
     * Retrieves the {@link com.dazednconfused.catalauncher.helper.Constants#CUSTOM_MODS_DIR} as a {@link File}.
     * */
    private static File getModsFolder() {
        File modsPath = new File(CUSTOM_MODS_DIR);
        if (!modsPath.exists()) {
            LOGGER.debug("Mods folder [{}] not found. Creating...", modsPath);
            Try.of(modsPath::mkdirs).onFailure(t -> LOGGER.error("Could not create mods destination folder [{}]", modsPath, t));
        }

        return modsPath;
    }
}

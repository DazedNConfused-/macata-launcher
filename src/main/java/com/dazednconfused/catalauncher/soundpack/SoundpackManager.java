package com.dazednconfused.catalauncher.soundpack;


import static com.dazednconfused.catalauncher.helper.Constants.CUSTOM_SOUNDPACKS_DIR;

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

public class SoundpackManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoundpackManager.class);

    /**
     * Returns all soundpacks currently found in {@link com.dazednconfused.catalauncher.helper.Constants#CUSTOM_SOUNDPACKS_DIR}.
     * */
    public static List<File> listAllSoundpacks() {
        LOGGER.debug("Listing all soundpacks...");
        return Arrays.stream(Objects.requireNonNull(getSoundpacksFolder().listFiles()))
                .filter(file -> !file.getName().equals(".DS_Store"))
                .collect(Collectors.toList());
    }

    /**
     * Deletes given {@code toBeDeleted} soundpack.
     * */
    public static void deleteSoundpack(File toBeDeleted) {
        LOGGER.info("Deleting soundpack [{}]...", toBeDeleted);
        Try.run(() -> FileUtils.deleteDirectory(toBeDeleted)).onFailure(t -> LOGGER.error("There was an error deleting soundpack [{}]", toBeDeleted, t));
    }

    /**
     * Installs given {@code toBeInstalled} soundpack inside {@link com.dazednconfused.catalauncher.helper.Constants#CUSTOM_SOUNDPACKS_DIR}.
     * */
    public static void installSoundpack(File toBeInstalled, Consumer<Path> onDoneCallback) {
        LOGGER.info("Installing soundpack [{}]...", toBeInstalled);
        File installInto = new File(getSoundpacksFolder().getPath() + "/" + toBeInstalled.getName());

        Try.run(() -> {
            LOGGER.debug("Copying [{}] into [{}]...", toBeInstalled, installInto);
            FileUtils.copyDirectory(toBeInstalled, installInto);
        }).onFailure(t -> LOGGER.error("There was an error installing soundpack [{}]", toBeInstalled, t)).andThen(() -> onDoneCallback.accept(installInto.toPath()));
    }

    /**
     * Retrieves the {@link com.dazednconfused.catalauncher.helper.Constants#CUSTOM_SOUNDPACKS_DIR} as a {@link File}.
     * */
    private static File getSoundpacksFolder() {
        File soundpacksPath = new File(CUSTOM_SOUNDPACKS_DIR);
        if (!soundpacksPath.exists()) {
            LOGGER.debug("Soundpacks folder [{}] not found. Creating...", soundpacksPath);
            Try.of(soundpacksPath::mkdirs).onFailure(t -> LOGGER.error("Could not create soundpacks destination folder [{}]", soundpacksPath, t));
        }

        return soundpacksPath;
    }
}

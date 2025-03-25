package com.dazednconfused.catalauncher.soundpack;

import com.dazednconfused.catalauncher.helper.Paths;

import com.dazednconfused.catalauncher.helper.result.Result;
import com.dazednconfused.catalauncher.mod.dto.ModDTO;

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
     * Returns all soundpacks currently found in {@link Paths#getCustomSoundpacksDir()}.
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
    public static Result<Throwable, Void> deleteSoundpack(File toBeDeleted) {
        LOGGER.info("Deleting soundpack [{}]...", toBeDeleted);
        return Try.run(() ->
            FileUtils.deleteDirectory(toBeDeleted)
        ).onFailure(t ->
            LOGGER.error("There was an error deleting soundpack [{}]", toBeDeleted, t)
        ).map(Result::success).recover(Result::failure).get();
    }

    /**
     * Installs given {@code toBeInstalled} soundpack inside {@link Paths#getCustomSoundpacksDir()}.
     * */
    public static Result<Throwable, Path> installSoundpack(File toBeInstalled, Consumer<Path> onDoneCallback) {
        LOGGER.info("Installing soundpack [{}]...", toBeInstalled);
        File installInto = new File(getSoundpacksFolder().getPath() + "/" + toBeInstalled.getName());

        return Try.of(() -> {
            LOGGER.debug("Copying [{}] into [{}]...", toBeInstalled, installInto);

            FileUtils.copyDirectory(toBeInstalled, installInto);

            return installInto.toPath();
        }).onFailure(t ->
            LOGGER.error("There was an error installing soundpack [{}]", toBeInstalled, t)
        ).map(installedPath -> {
            LOGGER.info("Soundpack [{}] has been successfully installed!", toBeInstalled);
            onDoneCallback.accept(installedPath);
            return Result.success(installedPath);
        }).recover(Result::failure).get();
    }

    /**
     * Retrieves the {@link Paths#getCustomSoundpacksDir()} as a {@link File}.
     * */
    protected static File getSoundpacksFolder() {
        File soundpacksPath = Paths.getCustomSoundpacksDir().toFile();
        if (!soundpacksPath.exists()) {
            LOGGER.debug("Soundpacks folder [{}] not found. Creating...", soundpacksPath);
            Try.of(soundpacksPath::mkdirs).onFailure(t -> LOGGER.error("Could not create soundpacks destination folder [{}]", soundpacksPath, t));
        }

        return soundpacksPath;
    }
}

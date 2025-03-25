package com.dazednconfused.catalauncher.soundpack;

import com.dazednconfused.catalauncher.helper.Paths;
import com.dazednconfused.catalauncher.helper.result.Result;
import com.dazednconfused.catalauncher.soundpack.dto.SoundpackDTO;
import com.dazednconfused.catalauncher.utils.CustomTimeUtils;

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

    public static final Consumer<SoundpackDTO> DO_NOTHING_ACTION = unused -> { }; // does nothing - represents an empty action

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
     * Uninstalls the given {@code toBeUninstalled} soundpack from {@link Paths#getCustomSoundpacksDir()}.
     * */
    public static Result<Throwable, SoundpackDTO> uninstallSoundpack(SoundpackDTO toBeUninstalled, Consumer<SoundpackDTO> onDoneCallback) {
        LOGGER.info("Uninstalling soundpack [{}]...", toBeUninstalled);

        return Try.of(() -> {
            // remove mod from mods folder -
            SoundpackManager.trashSoundpackFromSoundsFolder(toBeUninstalled).getOrElseThrowUnchecked();

            return toBeUninstalled;
        }).map(dto -> {
            // perform callback on successful uninstallation -
            onDoneCallback.accept(dto);
            return dto;
        }).onFailure(
            t -> LOGGER.error("There was an error uninstalling mod [{}]", toBeUninstalled.getName(), t)
        ).map(dto -> {
            LOGGER.info("Soundpack [{}] has been successfully uninstalled!", dto.getName());
            return Result.success(dto);
        }).recover(Result::failure).get();
    }

    /**
     * Installs given {@code toBeInstalled} soundpack inside {@link Paths#getCustomSoundpacksDir()}.
     * */
    public static Result<Throwable, SoundpackDTO> installSoundpack(File toBeInstalled, Consumer<SoundpackDTO> onDoneCallback) {
        LOGGER.info("Installing soundpack [{}]...", toBeInstalled);

        return Try.of(() -> {
            // parse destination -
            File installInto = new File(getSoundpacksFolder().getPath() + "/" + toBeInstalled.getName());

            // copy to sounds folder -
            LOGGER.debug("Copying [{}] into [{}]...", toBeInstalled, installInto);
            FileUtils.copyDirectory(toBeInstalled, installInto);

            return installInto.toPath();
        }).map(installPath ->
            // parse into DTO -
            SoundpackDTO.builder().name(installPath.getFileName().toString()).build()
        ).map(dto -> {
            // perform callback on successful installation -
            onDoneCallback.accept(dto);
            return dto;
        }).onFailure(t ->
            LOGGER.error("There was an error installing soundpack [{}]", toBeInstalled, t)
        ).map(dto -> {
            LOGGER.info("Soundpack [{}] has been successfully installed!", dto.getName());
            return Result.success(dto);
        }).recover(Result::failure).get();
    }

    /**
     * Moves the given {@code toBeUninstalled} soundpack from the {@link Paths#getCustomSoundpacksDir()} folder into the {@link Paths#getCustomTrashedSoundpacksPath()}.
     * */
    protected static Result<Throwable, Void> trashSoundpackFromSoundsFolder(SoundpackDTO toBeUninstalled) {
        return Try.run(() -> {

            File trashedSoundpacksDir = Paths.getCustomTrashedSoundpacksPath().toFile();
            if (!trashedSoundpacksDir.exists()) {
                LOGGER.debug("Trashed soundpacks' folder [{}] doesn't exist. Generating...", trashedSoundpacksDir);
                Try.of(trashedSoundpacksDir::mkdirs).onFailure(t -> LOGGER.error("There was an error while creating trashed soundpacks' folder [{}]", trashedSoundpacksDir, t));
            }

            File trashedSoundpackDir = new File(Path.of(
                trashedSoundpacksDir.getPath(),
                CustomTimeUtils.getYyyyMmDdHhMmSsTimestamp(),
                toBeUninstalled.getName()
            ).toString());

            File toBeTrashed = Paths.getCustomSoundpacksDir().resolve(toBeUninstalled.getName()).toFile();

            LOGGER.debug("Trashing soundpack [{}] into [{}]...", toBeTrashed, trashedSoundpackDir);

            File source = new File(toBeTrashed.getPath());
            File dest = new File(trashedSoundpackDir.getPath());
            FileUtils.moveDirectory(source, dest);

        }).onFailure(
            t -> LOGGER.error("There was an error trashing soundpack [{}]", toBeUninstalled, t)
        ).map(Result::success).recover(Result::failure).get();
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

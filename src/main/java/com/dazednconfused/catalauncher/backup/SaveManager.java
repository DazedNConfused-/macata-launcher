package com.dazednconfused.catalauncher.backup;

import com.dazednconfused.catalauncher.helper.Paths;
import com.dazednconfused.catalauncher.helper.Zipper;
import com.dazednconfused.catalauncher.helper.result.Result;

import io.vavr.control.Try;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SaveManager.class);

    /**
     * Returns the current {@link Paths#getCustomSavePath()} compression job, wrapped inside a {@link Thread} and ready to
     * be executed.
     * */
    public static Optional<Thread> backupCurrentSaves(Consumer<Integer> onPercentDoneCallback) {
        LOGGER.info("Backup-ing all saves...");

        if (!saveFilesExist()) {
            LOGGER.info("No saves found. Nothing to backup.");
            return Optional.empty();
        }

        File savesFolder = new File(Paths.getCustomSavePath());
        return Optional.of(compressFolderAsJob(
                savesFolder,
                getSaveBackupFolder().getAbsolutePath() + "/" + generateNameBasedOnCurrentTimestamp() + ".zip",
                onPercentDoneCallback
        ));
    }

    /**
     * Returns the current backup restoration job, wrapped inside a {@link Thread} and ready to be executed.
     * */
    public static Optional<Thread> restoreBackup(File backup2beRestored, Consumer<Integer> onPercentDoneCallback) {
        LOGGER.info("Restoring backup [{}]...", backup2beRestored);

        File trashedSaves = new File(Paths.getCustomTrashedSavePath());

        if (!trashedSaves.exists()) {
            LOGGER.debug("Trashed saves' folder [{}] doesn't exist. Generating...", trashedSaves);
            Try.of(trashedSaves::mkdirs).onFailure(t -> LOGGER.error("There was an error while creating trashed saves' folder [{}]", trashedSaves, t));
        }

        File trashedSavePath = new File(Paths.getCustomTrashedSavePath() + generateNameBasedOnCurrentTimestamp());

        if (!saveFilesExist()) {
            LOGGER.info("No current saves found. Nothing to move to trash folder.");
        } else {
            File currentSave = new File(Paths.getCustomSavePath());

            Try.of(() -> Files.move(
                    currentSave.toPath(),
                    trashedSavePath.toPath()
            )).onFailure(t -> LOGGER.error("There was an error while moving current save to trash folder [{}]", trashedSavePath, t));
        }

        return Optional.of(decompressFolderAsJob(
                backup2beRestored,
                Paths.getLauncherRootFolder(), // we don't decompress into CUSTOM_SAVE_PATH because we end up with ./saves/saves/<actual world saves>
                onPercentDoneCallback
        ));
    }

    /**
     * Deletes given {@code toBeDeleted} backup.
     * */
    public static boolean deleteBackup(File toBeDeleted) {
        LOGGER.info("Deleting backup [{}]...", toBeDeleted);
        return toBeDeleted.delete();
    }

    /**
     * Renames given {@code toBeRenamed} backup.
     *
     * @return {@link Result#success(Object)} with the new renamed {@link File}, or {@link Result#failure(Throwable)} if there
     *         was an error during the operation.
     * */
    public static Result<Throwable, File> renameBackup(File toBeRenamed, String newName) {
        LOGGER.info("Renaming backup [{}] into [{}]...", toBeRenamed, newName);

        File newFile = new File(toBeRenamed.getParentFile().getPath() + "/" + newName + ".zip");
        return Try.of(() -> Files.move(toBeRenamed.toPath(), newFile.toPath())).map(Path::toFile).onFailure(
            t -> LOGGER.error("There was an error while renaming save [{}] into [{}]", toBeRenamed, newFile, t)
        ).map(Result::success).recover(Result::failure).get();
    }

    /**
     * Returns all save backups currently found in {@link Paths#getSaveBackupPath()}.
     * */
    public static List<File> listAllBackups() {
        LOGGER.debug("Listing all backups...");
        return Arrays.stream(Objects.requireNonNull(getSaveBackupFolder().listFiles()))
                .filter(file -> !file.getName().equals(".DS_Store"))
                .collect(Collectors.toList());
    }

    /**
     * If save files exist in {@link Paths#getCustomSavePath()}, returns the last modified valid save file. Save file is valid
     * if it has a .sav file in it.
     * */
    public static Optional<File> getLastModifiedValidSave() {
        File savesFolder = new File(Paths.getCustomSavePath());
        if (!saveFilesExist()) {
            return Optional.empty();
        }
        File[] saveDirs = savesFolder.listFiles(file -> file.isDirectory() && !file.getName().equals(".DS_Store"));
        Arrays.sort(saveDirs, Comparator.comparingLong(File::lastModified).reversed());

        for (File directory : saveDirs) {
            File[] savFiles = directory.listFiles((dir, name) -> name.endsWith(".sav"));
            if (savFiles != null && savFiles.length > 0) {
                return Optional.of(directory);
            }
        }

        return Optional.empty(); // No directory with .sav file found

    }
    /**
     * Determines whether save files exist in {@link Paths#getCustomSavePath()}.
     * */

    public static boolean saveFilesExist() {
        File savesFolder = new File(Paths.getCustomSavePath());
        return savesFolder.exists() && Arrays.stream(Objects.requireNonNull(savesFolder.listFiles())).anyMatch(file -> !file.getName().equals(".DS_Store"));
    }

    /**
     * Gets the latest save {@link File} from {@link Paths#getCustomSavePath()}, wrapped inside an {@link Optional}. {@link Optional#empty()}
     * if given path doesn't exist or doesn't have any folders that could be assumed to be individual save files.
     * */
    public static Optional<File> getLatestSave() {

        if (!saveFilesExist() || (getLastModifiedValidSave().orElse(null) == null)) {
            LOGGER.debug("No saves found. No latest save can be retrieved.");
            return Optional.empty();
        }
        return getLastModifiedValidSave();
    }

    /**
     * Retrieves the {@link Paths#getSaveBackupPath()} as a {@link File}.
     * */
    private static File getSaveBackupFolder() {
        File backupPath = new File(Paths.getSaveBackupPath());
        if (!backupPath.exists()) {
            LOGGER.debug("Save backup destination folder [{}] not found. Creating...", backupPath);
            Try.of(backupPath::mkdirs).onFailure(t -> LOGGER.error("Could not create backup destination folder [{}]", backupPath, t));
        }

        return backupPath;
    }

    /**
     * Returns the requested compression job wrapped inside a {@link Thread} and ready to be executed.
     * */
    private static Thread compressFolderAsJob(File sourceDir, String outputFile, Consumer<Integer> onPercentDoneCallback) {
        return new Thread(() -> Zipper.compressAndCallback(sourceDir, outputFile, onPercentDoneCallback, 100));
    }

    /**
     * Returns the requested decompression job wrapped inside a {@link Thread} and ready to be executed.
     * */
    private static Thread decompressFolderAsJob(File sourceFile, String destinationPath, Consumer<Integer> onPercentDoneCallback) {
        return new Thread(() -> Zipper.decompressAndCallback(sourceFile, destinationPath, onPercentDoneCallback, 100));
    }

    /**
     * Generates a file name based on current's {@link java.util.Date}.
     * */
    private static String generateNameBasedOnCurrentTimestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
    }

}

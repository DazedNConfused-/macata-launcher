package com.dazednconfused.catalauncher.backup;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.progress.ProgressMonitor;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Zipper {

    private static final Logger LOGGER = LoggerFactory.getLogger(Zipper.class);

    /**
     * Compresses the given {@code sourceDir} {@link File} into the given {@code outputFile.zip}, calling the provided {@link Consumer}
     * callback every {@code callbackCheckMs} milliseconds.
     * */
    public static void compressAndCallback(File sourceDir, String outputFile, @Nullable Consumer<Integer> onPercentDoneCallback, int callbackCheckMs) {
        LOGGER.debug("Compressing folder [{}] into [{}]...", sourceDir, outputFile);

        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(CompressionMethod.DEFLATE);
        zipParameters.setCompressionLevel(CompressionLevel.ULTRA);

        try (ZipFile zipFile = new ZipFile(outputFile)) {
            ProgressMonitor progressMonitor = zipFile.getProgressMonitor();

            zipFile.setRunInThread(true);
            zipFile.addFolder(sourceDir);

            while (!progressMonitor.getState().equals(ProgressMonitor.State.READY)) {
                LOGGER.trace(
                        "Zipper task [{}]: processing file [{}]. Percent complete: [{}]",
                        progressMonitor.getCurrentTask(),
                        progressMonitor.getFileName(),
                        progressMonitor.getPercentDone()
                );

                if (onPercentDoneCallback != null) {
                    onPercentDoneCallback.accept(progressMonitor.getPercentDone());
                }

                Thread.sleep(callbackCheckMs);
            }

            if (progressMonitor.getResult().equals(ProgressMonitor.Result.SUCCESS)) {
                LOGGER.debug("Successfully added folder [{}] to zip [{}]", sourceDir, outputFile);
            } else if (progressMonitor.getResult().equals(ProgressMonitor.Result.ERROR)) {
                LOGGER.error(
                        "There was an error while compressing folder [{}] into [{}]. Error message: [{}]",
                        sourceDir, outputFile, progressMonitor.getException().getMessage()
                );
            } else if (progressMonitor.getResult().equals(ProgressMonitor.Result.CANCELLED)) {
                LOGGER.error("Compression task [{}] to zip [{}] cancelled", sourceDir, outputFile);
            }

            onPercentDoneCallback.accept(100); // whatever the result, set operation as "100% completed"

        } catch (InterruptedException | IOException e) {
            LOGGER.error("There was an error while compressing folder [{}] into [{}]", sourceDir, outputFile, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Decompresses the given {@code sourceDir} {@link File} into the given {@code outputFile.zip}, calling the provided {@link Consumer}
     * callback every {@code callbackCheckMs} milliseconds.
     * */
    public static void decompressAndCallback(File sourceFile, String destinationPath, @Nullable Consumer<Integer> onPercentDoneCallback, int callbackCheckMs) {
        LOGGER.debug("Decompressing file [{}] into [{}]...", sourceFile, destinationPath);

        try (ZipFile zipFile = new ZipFile(sourceFile)) {
            ProgressMonitor progressMonitor = zipFile.getProgressMonitor();

            zipFile.setRunInThread(true);
            zipFile.extractAll(destinationPath);

            while (!progressMonitor.getState().equals(ProgressMonitor.State.READY)) {
                LOGGER.trace(
                        "Zipper task [{}]: processing file [{}]. Percent complete: [{}]",
                        progressMonitor.getCurrentTask(),
                        progressMonitor.getFileName(),
                        progressMonitor.getPercentDone()
                );

                if (onPercentDoneCallback != null) {
                    onPercentDoneCallback.accept(progressMonitor.getPercentDone());
                }

                Thread.sleep(callbackCheckMs);
            }

            if (progressMonitor.getResult().equals(ProgressMonitor.Result.SUCCESS)) {
                LOGGER.debug("Successfully extracted zip [{}] into [{}]", sourceFile, destinationPath);
            } else if (progressMonitor.getResult().equals(ProgressMonitor.Result.ERROR)) {
                LOGGER.error(
                        "There was an error while extracting zip [{}] into [{}]. Error message: [{}]",
                        sourceFile, destinationPath, progressMonitor.getException().getMessage()
                );
            } else if (progressMonitor.getResult().equals(ProgressMonitor.Result.CANCELLED)) {
                LOGGER.error("Decompression task [{}] to [{}] cancelled", sourceFile, destinationPath);
            }

            onPercentDoneCallback.accept(100); // whatever the result, set operation as "100% completed"

        } catch (InterruptedException | IOException e) {
            LOGGER.error("There was an error while decompressing zip [{}] into [{}]", sourceFile, destinationPath, e);
            throw new RuntimeException(e);
        }
    }
}
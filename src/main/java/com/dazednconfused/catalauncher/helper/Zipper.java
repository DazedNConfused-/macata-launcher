package com.dazednconfused.catalauncher.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
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
     * Compresses the given {@code sourceDir} {@link File} into the given {@code outputFileZip.zip}, calling the provided
     * {@link Consumer} callback every {@code callbackCheckMs} milliseconds.
     * */
    public static void compressAndCallback(File sourceDir, Path outputFileZip, @Nullable Consumer<Integer> onPercentDoneCallback, int callbackCheckMs) {
        LOGGER.debug("Compressing folder [{}] into [{}]...", sourceDir, outputFileZip);

        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(CompressionMethod.DEFLATE);
        zipParameters.setCompressionLevel(CompressionLevel.ULTRA);

        try (ZipFile zipFile = new ZipFile(outputFileZip.toString())) {
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
                LOGGER.debug("Successfully added folder [{}] to zip [{}]", sourceDir, outputFileZip);
            } else if (progressMonitor.getResult().equals(ProgressMonitor.Result.ERROR)) {
                LOGGER.error(
                        "There was an error while compressing folder [{}] into [{}]. Error message: [{}]",
                        sourceDir, outputFileZip, progressMonitor.getException().getMessage()
                );
            } else if (progressMonitor.getResult().equals(ProgressMonitor.Result.CANCELLED)) {
                LOGGER.error("Compression task [{}] to zip [{}] cancelled", sourceDir, outputFileZip);
            }

            if (onPercentDoneCallback != null) {
                onPercentDoneCallback.accept(100); // whatever the result, set operation as "100% completed"
            }

        } catch (InterruptedException | IOException e) {
            LOGGER.error("There was an error while compressing folder [{}] into [{}]", sourceDir, outputFileZip, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Decompresses the given {@code sourceFileZip} {@link File} into the given {@code destinationPath}, calling the provided
     * {@link Consumer} callback every {@code callbackCheckMs} milliseconds.
     * */
    public static void decompressAndCallback(File sourceFileZip, Path destinationPath, @Nullable Consumer<Integer> onPercentDoneCallback, int callbackCheckMs) {
        LOGGER.debug("Decompressing file [{}] into [{}]...", sourceFileZip, destinationPath);

        try (ZipFile zipFile = new ZipFile(sourceFileZip)) {
            ProgressMonitor progressMonitor = zipFile.getProgressMonitor();

            zipFile.setRunInThread(true);
            zipFile.extractAll(destinationPath.toString());

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
                LOGGER.debug("Successfully extracted zip [{}] into [{}]", sourceFileZip, destinationPath);
            } else if (progressMonitor.getResult().equals(ProgressMonitor.Result.ERROR)) {
                LOGGER.error(
                        "There was an error while extracting zip [{}] into [{}]. Error message: [{}]",
                        sourceFileZip, destinationPath, progressMonitor.getException().getMessage()
                );
            } else if (progressMonitor.getResult().equals(ProgressMonitor.Result.CANCELLED)) {
                LOGGER.error("Decompression task [{}] to [{}] cancelled", sourceFileZip, destinationPath);
            }

            if (onPercentDoneCallback != null) {
                onPercentDoneCallback.accept(100); // whatever the result, set operation as "100% completed"
            }

        } catch (InterruptedException | IOException e) {
            LOGGER.error("There was an error while decompressing zip [{}] into [{}]", sourceFileZip, destinationPath, e);
            throw new RuntimeException(e);
        }
    }
}
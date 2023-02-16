package com.dazednconfused.catalauncher.helper;

import io.vavr.control.Try;

import java.awt.Desktop;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileExplorerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileExplorerManager.class);

    /**
     * Opens given {@link File} path in the underlying {@link Desktop}'s file explorer.
     * */
    public static void openFileInFileExplorer(File file) {
        if (Desktop.isDesktopSupported()) {
            Try.run(() -> Desktop.getDesktop().open(file)).onFailure(t -> LOGGER.error("There was an error while opening file [{}]", file, t));
        } else {
            LOGGER.error("Desktop is not supported! Cannot open [{}]", file);
        }
    }

}

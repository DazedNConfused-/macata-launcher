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
     *
     * @param file The file to open.
     * @param browse If set to {@code true}, opens the folder containing the {@code file} and selects it. Otherwise, attempts
     *               to open the file outright (which may trigger default programs depending on the {@link File}'s extension).
     * */
    public static void openFileInFileExplorer(File file, boolean browse) {
        LOGGER.debug("Opening [{}] in file explorer...", file);
        LOGGER.trace("browse flag = [{}]", browse);

        if (Desktop.isDesktopSupported()) {
            Try.run(() -> {
                if (browse) {
                    Desktop.getDesktop().browseFileDirectory(file);
                } else {
                    Desktop.getDesktop().open(file);
                }
            }).onFailure(t -> LOGGER.error("There was an error while opening file [{}]", file, t));
        } else {
            LOGGER.error("Desktop is not supported! Cannot open [{}]", file);
        }
    }

}

package com.dazednconfused.catalauncher.gui.helper;

import com.dazednconfused.catalauncher.gui.ErrorDialog;

import io.vavr.control.Try;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.apache.batik.swing.JSVGCanvas;

public class GuiResource {

    /**
     * Extracts the SVG in the provided {@code path} into a temporary {@link java.io.File} that {@link JSVGCanvas} can read
     * and load from.
     * */
    public static String extractIconFrom(String path) {
        return Try.of(() -> {
            Path tmpFilePath = Files.createTempFile(null, null);
            Objects.requireNonNull(ErrorDialog.class.getClassLoader().getResourceAsStream(path)).transferTo(new FileOutputStream(tmpFilePath.toFile()));
            return tmpFilePath.toFile().getPath();
        }).getOrNull();
    }

}

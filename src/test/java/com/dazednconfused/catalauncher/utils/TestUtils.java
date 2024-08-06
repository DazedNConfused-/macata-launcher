package com.dazednconfused.catalauncher.utils;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * Common test utilities.
 * */
public class TestUtils {

    /**
     * Collects all {@link File}s from {@code sourceDirectory} into the given {@code result} array. Useful for making directory
     * content assertions with relative paths.
     * */
    public static void collectAllFilesFromInto(File sourceDirectory, List<File> result) {
        File[] files = sourceDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    collectAllFilesFromInto(file, result);
                } else {
                    result.add(file);
                }
            }
        }
    }

    /**
     * Retrieves the given {@code fileName} from the Java Resources' folder.
     * */
    public static File getFromResource(String fileName) {
        URL resourceUrl = TestUtils.class.getClassLoader().getResource(fileName);

        // check that the resource exists
        if (resourceUrl != null) {
            return new File(resourceUrl.getFile()); // convert the URL to a File object
        }

        throw new IllegalArgumentException("Could not find resource: " + fileName);
    }
}

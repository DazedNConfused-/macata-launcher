package com.dazednconfused.catalauncher.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Common test utilities.
 * */
public class TestUtils {

    /**
     * Collects all {@link File}s from {@code sourceDirectory} into the given {@code result} array. Useful for making directory
     * content assertions with relative paths.
     * */
    public static void collectAllFilesFromInto(File sourceDirectory, List<File> result) {
       FileUtils.collectAllFilesFromInto(sourceDirectory, result);
    }

    /**
     * Retrieves the given {@code fileName} from the Java Resources' folder.
     * */
    public static File getFromResource(String fileName) {
        URL resourceUrl = TestUtils.class.getClassLoader().getResource(fileName);

        // check that the resource exists
        if (resourceUrl != null) {
            // decode the URL to handle spaces and special characters
            String decodedPath = URLDecoder.decode(resourceUrl.getFile(), StandardCharsets.UTF_8);
            return new File(decodedPath);
        }

        throw new IllegalArgumentException("Could not find resource: " + fileName);
    }

    /**
     * Unzips the given {@code zipFilePath} into the provided {@code destDir}.
     * */
    public static void unzip(String zipFilePath, String destDir) {
        File dir = new File(destDir);

        // create output directory if it doesn't exist
        if (!dir.exists()) {
            dir.mkdirs();
        }

        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = new File(dir, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    // if the entry is a directory, create the directory
                    newFile.mkdirs();
                } else {
                    // if the entry is a file, extract it
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

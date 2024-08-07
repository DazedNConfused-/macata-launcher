package com.dazednconfused.catalauncher.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class FileUtils {

    /**
     * Computes the MD5 checksum of the given {@link File}.
     * */
    public static String getFileChecksum(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[1024];
            int bytesCount;

            // read file data and update in message digest
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file [" + file.getPath() + "]", e);
        }

        // get the hash's bytes
        byte[] bytes = digest.digest();

        // convert bytes to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    /**
     * Collects all {@link File}s from {@code sourceDirectory} into the given {@code result} array.
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
}
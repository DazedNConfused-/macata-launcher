package com.dazednconfused.catalauncher.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class ZipperTest {

    @Test
    void compress_and_callback(@TempDir File tempDir) {

        // prepare mock data ---
        AtomicInteger calledTimes = new AtomicInteger(0);
        List<Integer> callbackedValues = new ArrayList<>();

        Consumer<Integer> MOCKED_CALLBACK = i -> {
            callbackedValues.add(i);
            calledTimes.incrementAndGet();
        };

        Path MOCKED_DESTINATION_PATH = Path.of(tempDir.getPath(), "zipped.zip");

        // execute test ---
        Zipper.compressAndCallback(
            getFromResource("zipper/test/compress"),
            MOCKED_DESTINATION_PATH,
            MOCKED_CALLBACK,
            1
        );

        // verify assertions ---
        assertThat(calledTimes).hasPositiveValue();
        assertThat(callbackedValues).size().isPositive();

        File EXPECTED_ZIP = new File(tempDir.getPath(), "zipped.zip");

        assertThat(EXPECTED_ZIP).isFile();
        assertThat(EXPECTED_ZIP).size().isGreaterThan(0);
    }

    @Test
    void decompress_and_callback_success(@TempDir File tempDir) {

        // prepare mock data ---
        AtomicInteger calledTimes = new AtomicInteger(0);
        List<Integer> callbackedValues = new ArrayList<>();

        Consumer<Integer> MOCKED_CALLBACK = i -> {
            callbackedValues.add(i);
            calledTimes.incrementAndGet();
        };

        // execute test ---
        Zipper.decompressAndCallback(
            getFromResource("zipper/test/decompress/sample.zip"),
            tempDir.getAbsoluteFile().toPath(),
            MOCKED_CALLBACK,
            1
        );

        // verify assertions ---
        assertThat(calledTimes).hasPositiveValue();
        assertThat(callbackedValues).size().isPositive();


        String[] EXPECTED_FILES = {"1.file", "2.file", "3.file"};
        String[] EXPECTED_FOLDERS = {"childFolder1", "childFolder2", "childFolder3"};

        assertDirectory(
            tempDir.toPath(),
            EXPECTED_FILES, EXPECTED_FOLDERS,
            0
        );
    }

    /**
     * Retrieves the given {@code fileName} from the Java Resources' folder.
     * */
    private static File getFromResource(String fileName) {
        URL resourceUrl = ZipperTest.class.getClassLoader().getResource(fileName);

        // check that the resource exists
        if (resourceUrl != null) {
            return new File(resourceUrl.getFile()); // convert the URL to a File object
        }

        throw new IllegalArgumentException();
    }

    /**
     * Custom {@link org.assertj.core.api.Assertions} for the {@link #decompress_and_callback_success(File)} test.
     * */
    private static void assertDirectory(Path directory, String[] expectedFiles, String[] expectedFolders, int level) {
        File dir = directory.toFile();
        assertThat(dir).isDirectory();

        // check for expected files
        for (String fileName : expectedFiles) {
            File file = new File(directory.toFile(), fileName);
            assertThat(file).isFile();
            assertThat(file.length()).isGreaterThan(0);
        }

        // if we've reached the last level, there should be no more folders
        if (level >= expectedFolders.length) {
            assertThat(directory.toFile().listFiles(File::isDirectory)).isEmpty();
            return;
        }

        // check for the expected folder
        File nextFolder = new File(directory.toFile(), expectedFolders[level]);
        assertThat(nextFolder).isDirectory();

        // recur into the next folder level
        assertDirectory(nextFolder.toPath(), expectedFiles, expectedFolders, level + 1);
    }
}
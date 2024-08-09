package com.dazednconfused.catalauncher.helper;

import com.dazednconfused.catalauncher.assertions.CustomFileAssertions;
import com.dazednconfused.catalauncher.utils.TestUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
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
            TestUtils.getFromResource("zipper/test/compress"),
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
            TestUtils.getFromResource("zipper/test/decompress/sample.zip"),
            tempDir.getAbsoluteFile().toPath(),
            MOCKED_CALLBACK,
            1
        );

        // verify assertions ---
        assertThat(calledTimes).hasPositiveValue();
        assertThat(callbackedValues).size().isPositive();

        CustomFileAssertions.assertThat(tempDir).containsExactlyFilesWithRelativePaths(Arrays.asList(
            "1.file",
            "2.file",
            "3.file",
            "childFolder1/1.file",
            "childFolder1/2.file",
            "childFolder1/3.file",
            "childFolder1/childFolder2/1.file",
            "childFolder1/childFolder2/2.file",
            "childFolder1/childFolder2/3.file",
            "childFolder1/childFolder2/childFolder3/1.file",
            "childFolder1/childFolder2/childFolder3/2.file",
            "childFolder1/childFolder2/childFolder3/3.file"
        ));
    }
}
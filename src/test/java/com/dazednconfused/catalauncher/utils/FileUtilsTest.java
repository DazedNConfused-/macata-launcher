package com.dazednconfused.catalauncher.utils;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.mockStatic;

import java.io.File;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

import com.dazednconfused.catalauncher.assertions.CustomFileAssertions;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class FileUtilsTest {

    @Test
    void get_file_checksum_success() {

        // prepare mock data ---
        File MOCKED_FILE = TestUtils.getFromResource("fileutils/md5/test.txt");

        // pre-test assertions ---
        assertThat(MOCKED_FILE).isNotEmpty();

        // execute test ---
        String result = FileUtils.getFileChecksum(MOCKED_FILE);

        // verify assertions ---
        assertThat(result).isEqualTo("515eca5e06529ae0643cad83ed6ce61b");
    }

    @Test
    void get_file_checksum_failure_when_file_is_null() {

        // execute test ---
        Throwable thrown = catchThrowable(() -> FileUtils.getFileChecksum(null));

        // verify assertions ---
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("File cannot be null");
    }

    @Test
    void get_file_checksum_failure_when_md5_algorithm_is_not_found() {
        try (MockedStatic<MessageDigest> mockedMessageDigest = mockStatic(MessageDigest.class)) {

            // prepare mock data ---
            mockedMessageDigest.when(() -> MessageDigest.getInstance("MD5")).thenThrow(new NoSuchAlgorithmException());
            File MOCKED_FILE = TestUtils.getFromResource("fileutils/md5/test.txt");

            // pre-test assertions ---
            assertThat(MOCKED_FILE).isNotEmpty();

            // execute test ---
            Throwable thrown = catchThrowable(() -> FileUtils.getFileChecksum(MOCKED_FILE));

            // verify assertions ---
            assertThat(thrown).isInstanceOf(RuntimeException.class).hasMessageContaining("MD5 algorithm not found");
        }
    }

    @Test
    void collect_all_files_from_into_success() {

        // prepare mock data ---
        File MOCKED_FILE = TestUtils.getFromResource("fileutils/collect");

        // pre-test assertions ---
        assertThat(MOCKED_FILE).isDirectory();

        // execute test ---
        ArrayList<File> result = new ArrayList<>();
        FileUtils.collectAllFilesFromInto(MOCKED_FILE, result);

        // verify assertions ---
        assertThat(result).containsExactlyInAnyOrder(
            TestUtils.getFromResource("fileutils/collect/1.file"),
            TestUtils.getFromResource("fileutils/collect/2.file"),
            TestUtils.getFromResource("fileutils/collect/3.file"),
            TestUtils.getFromResource("fileutils/collect/childFolder1/1.file"),
            TestUtils.getFromResource("fileutils/collect/childFolder1/2.file"),
            TestUtils.getFromResource("fileutils/collect/childFolder1/3.file"),
            TestUtils.getFromResource("fileutils/collect/childFolder1/childFolder2/1.file"),
            TestUtils.getFromResource("fileutils/collect/childFolder1/childFolder2/2.file"),
            TestUtils.getFromResource("fileutils/collect/childFolder1/childFolder2/3.file"),
            TestUtils.getFromResource("fileutils/collect/childFolder1/childFolder2/childFolder3/1.file"),
            TestUtils.getFromResource("fileutils/collect/childFolder1/childFolder2/childFolder3/2.file"),
            TestUtils.getFromResource("fileutils/collect/childFolder1/childFolder2/childFolder3/3.file")
        );
    }

    @Test
    void has_contents_non_empty_success() {

        // prepare mock data ---
        File MOCKED_FILE = TestUtils.getFromResource("fileutils/contents/nonempty");

        // pre-test assertions ---
        assertThat(MOCKED_FILE).isDirectory();

        // execute test ---
        boolean result = FileUtils.hasContents(MOCKED_FILE);

        // verify assertions ---
        assertThat(result).isTrue();
    }

    @Test
    void has_contents_empty_success() {

        // prepare mock data ---
        File MOCKED_FILE = TestUtils.getFromResource("fileutils/contents/empty");

        // pre-test assertions ---
        assertThat(MOCKED_FILE).isDirectory();

        // execute test ---
        boolean result = FileUtils.hasContents(MOCKED_FILE);

        // verify assertions ---
        assertThat(result).isFalse();
    }

    @Test
    void has_contents_mixed_success() {

        // prepare mock data ---
        File MOCKED_FILE = TestUtils.getFromResource("fileutils/contents/mixed");

        // pre-test assertions ---
        assertThat(MOCKED_FILE).isDirectory();

        // execute test ---
        boolean result = FileUtils.hasContents(MOCKED_FILE);

        // verify assertions ---
        assertThat(result).isTrue();
    }
}
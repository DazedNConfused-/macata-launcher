package com.dazednconfused.catalauncher.utils;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.mockStatic;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class FileHashUtilsTest {

    @Test
    void get_file_checksum_success() {

        // prepare mock data ---
        File MOCKED_FILE = TestUtils.getFromResource("filehash/test.txt");

        // pre-test assertions ---
        assertThat(MOCKED_FILE).isNotEmpty();

        // execute test ---
        String result = FileHashUtils.getFileChecksum(MOCKED_FILE);

        // verify assertions ---
        assertThat(result).isEqualTo("515eca5e06529ae0643cad83ed6ce61b");
    }

    @Test
    void get_file_checksum_failure_when_file_is_null() {

        // execute test ---
        Throwable thrown = catchThrowable(() -> FileHashUtils.getFileChecksum(null));

        // verify assertions ---
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("File cannot be null");
    }

    @Test
    void get_file_checksum_failure_when_md5_algorithm_is_not_found() {
        try (MockedStatic<MessageDigest> mockedMessageDigest = mockStatic(MessageDigest.class)) {

            // prepare mock data ---
            mockedMessageDigest.when(() -> MessageDigest.getInstance("MD5")).thenThrow(new NoSuchAlgorithmException());
            File MOCKED_FILE = TestUtils.getFromResource("filehash/test.txt");

            // pre-test assertions ---
            assertThat(MOCKED_FILE).isNotEmpty();

            // execute test ---
            Throwable thrown = catchThrowable(() -> FileHashUtils.getFileChecksum(MOCKED_FILE));

            // verify assertions ---
            assertThat(thrown).isInstanceOf(RuntimeException.class).hasMessageContaining("MD5 algorithm not found");
        }
    }
}
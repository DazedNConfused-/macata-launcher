package com.dazednconfused.catalauncher.assertions;

import com.dazednconfused.catalauncher.utils.TestUtils;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomFileAssertions extends AbstractAssert<CustomFileAssertions, File> {

    protected CustomFileAssertions(File actual) {
        super(actual, CustomFileAssertions.class);
    }

    public static CustomFileAssertions assertThat(File actual) {
        return new CustomFileAssertions(actual);
    }

    public CustomFileAssertions hasExactlyFilesWithRelativePaths(List<String> expectedRelativePaths) {

        // collect all files from the directory
        List<File> actualFiles = new ArrayList<>();
        TestUtils.collectAllFilesFromInto(actual, actualFiles);

        // convert actual files to relative paths
        List<String> actualRelativePaths = actualFiles.stream()
                .map(file -> actual.toPath().relativize(file.toPath()).toString())
                .collect(Collectors.toList());

        // perform the assertion
        Assertions.assertThat(actualRelativePaths)
            .as("Expected paths to be present in the directory")
            .containsAll(expectedRelativePaths);

        return this;
    }
}
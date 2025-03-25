package com.dazednconfused.catalauncher.assertions;

import com.dazednconfused.catalauncher.utils.TestUtils;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom assertions for {@link File} objects.
 * <p>
 * This class provides custom assertion methods for {@link File} objects,
 * extending the functionality of AssertJ's {@link AbstractAssert}.
 * It allows for more readable and expressive tests when working with file
 * system operations, particularly in verifying the contents of directories.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * File directory = new File("path/to/directory");
 * CustomFileAssertions.assertThat(directory)
 *     .containsExactlyFilesWithRelativePaths(Arrays.asList("file1.txt", "file2.txt"));
 * }
 * </pre>
 * </p>
 */
public class CustomFileAssertions extends AbstractAssert<CustomFileAssertions, File> {

    /**
     * Protected constructor.
     *
     * @param actual the actual File object to be asserted
     */
    protected CustomFileAssertions(File actual) {
        super(actual, CustomFileAssertions.class);
    }

    /**
     * Entry point for {@link CustomFileAssertions}.
     *
     * @param actual the actual File object to be asserted
     * @return a new instance of CustomFileAssertions
     */
    public static CustomFileAssertions assertThat(File actual) {
        return new CustomFileAssertions(actual);
    }

    /**
     * Asserts that the directory contains exactly the files with the specified relative paths.
     *
     * @param expectedRelativePaths the list of expected relative paths of files
     * @return the current instance of {@link CustomFileAssertions} for method chaining
     */
    public CustomFileAssertions containsExactlyFilesWithRelativePaths(List<String> expectedRelativePaths) {

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
            .containsExactlyInAnyOrderElementsOf(expectedRelativePaths);

        return this;
    }
}
package com.dazednconfused.catalauncher.update;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a version with optional pre-release information. This class supports semantic versioning and pre-release tags.
 * <br/><br/>
 * Examples of valid version strings:
 * <ul>
 *     <li>{@code v1.2.3}</li>
 *     <li>{@code prerelease-1.2.3-YYYYmmDD_HHmmSS}</li>
 * </ul>
 *
 * @see <a href="https://stackoverflow.com/a/11024200">https://stackoverflow.com/a/11024200</a>
 *
 */
public class Version implements Comparable<Version> {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^(?:prerelease-)?v?([0-9]+(\\.[0-9]+)*)(?:-(.+))?$");
    private static final String PRERELEASE_PREFIX = "prerelease-";

    private final String semver;
    private final String preReleaseTag;
    private final boolean isPreRelease;

    /**
     * Constructs a {@link Version} object from a version string.
     *
     * @param versionString the version string, e.g., "v1.2.3", "prerelease-v1.2.3-alpha", etc
     * @throws IllegalArgumentException if the version string is null or does not match the expected format
     */
    public Version(String versionString) {
        if (versionString == null) {
            throw new IllegalArgumentException("Version cannot be null");
        }
        Matcher matcher = VERSION_PATTERN.matcher(versionString);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid version format");
        }
        this.isPreRelease = versionString.startsWith(PRERELEASE_PREFIX);
        this.semver = matcher.group(1);
        this.preReleaseTag = matcher.group(3);
    }

    /**
     * Returns the semantic version of the {@link Version}.
     *
     * @return The semantic version (ie: {@code 1.2.3})
     */
    public String getSemver() {
        return this.semver;
    }

    /**
     * Returns the pre-release tag of the {@link Version}.
     *
     * @return The pre-release tag, or {@code null} if not a pre-release
     */
    public String getPreReleaseTag() {
        return this.preReleaseTag;
    }

    /**
     * Checks if the {@link Version} is a pre-release.
     *
     * @return {@code true} if the version is a pre-release, {@code false} otherwise
     */
    public boolean isPreRelease() {
        return this.isPreRelease;
    }

    @Override
    public int compareTo(Version that) {
        if (that == null) {
            return 1;
        }
        String[] thisParts = this.semver.split("\\.");
        String[] thatParts = that.semver.split("\\.");
        int length = Math.max(thisParts.length, thatParts.length);
        for (int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length ? Integer.parseInt(thisParts[i]) : 0;
            int thatPart = i < thatParts.length ? Integer.parseInt(thatParts[i]) : 0;
            if (thisPart < thatPart) {
                return -1;
            }
            if (thisPart > thatPart) {
                return 1;
            }
        }

        // if base versions are equal, pre-releases are considered lower than releases
        if (this.isPreRelease && !that.isPreRelease) {
            return -1;
        }
        if (!this.isPreRelease && that.isPreRelease) {
            return 1;
        }

        // optionally, compare pre-release tags lexicographically if both are pre-releases
        if (this.isPreRelease) {
            if (this.preReleaseTag == null && that.preReleaseTag != null) {
                return -1;
            }
            if (this.preReleaseTag != null && that.preReleaseTag == null) {
                return 1;
            }
            if (this.preReleaseTag != null) {
                return this.preReleaseTag.compareTo(that.preReleaseTag);
            }
        }

        // if both versions are equal, return 0
        return 0;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null || this.getClass() != that.getClass()) {
            return false;
        }
        return this.compareTo((Version) that) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(semver, preReleaseTag, isPreRelease);
    }

    /**
     * Returns the full version string.
     *
     * @return "v" followed by the semantic version if not a pre-release (ie: {@code v1.2.3}); or "prerelease-" followed
     *         by the semantic version and pre-release tag if it is a pre-release (ie: {@code prerelease-1.2.3-YYYYmmDD_HHmmSS}).
     */
    @Override
    public String toString() {
        if (isPreRelease) {
            if (preReleaseTag != null) {
                return PRERELEASE_PREFIX + semver + "-" + preReleaseTag;
            }
            return PRERELEASE_PREFIX + semver;
        }
        return "v" + this.semver;
    }
}
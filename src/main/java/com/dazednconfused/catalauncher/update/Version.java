package com.dazednconfused.catalauncher.update;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @see <a href="https://stackoverflow.com/a/11024200">https://stackoverflow.com/a/11024200</a>
 * */
public class Version implements Comparable<Version> {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^(?:prerelease-)?v?([0-9]+(\\.[0-9]+)*)(?:-(.+))?$");

    private final String semver;
    private final String preReleaseTag;
    private final boolean isPreRelease;

    public String get() {
        if (isPreRelease && preReleaseTag != null) {
            return "prerelease-" + semver + "-" + preReleaseTag;
        }
        return "v" + this.semver;
    }

    public String getPreReleaseTag() {
        return this.preReleaseTag;
    }

    public boolean isPreRelease() {
        return this.isPreRelease;
    }

    /**
     * Constructs a Version object from a version string.
     *
     * @param versionString the version string, e.g., "v1.2.3", "prerelease-v1.2.3-alpha", etc.
     *
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
        this.isPreRelease = versionString.startsWith("prerelease-");
        this.semver = matcher.group(1);
        this.preReleaseTag = matcher.group(3);
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

    @Override
    public String toString() {
        if (isPreRelease && preReleaseTag != null) {
            return "prerelease-" + semver + "-" + preReleaseTag;
        }
        return semver;
    }
}
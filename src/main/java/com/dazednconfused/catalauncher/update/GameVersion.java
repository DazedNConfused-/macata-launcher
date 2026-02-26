package com.dazednconfused.catalauncher.update;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a game version supporting multiple CDDA version formats.
 * <br/><br/>
 * Supported formats:
 * <ul>
 *     <li>Stable releases: {@code 0.G}, {@code 0.F-3}, {@code 0.E}</li>
 *     <li>Experimental/date-based: {@code 2024-01-15-0123}, {@code cdda-experimental-2024-01-15-0123}</li>
 *     <li>Generic tags: Falls back to string comparison</li>
 * </ul>
 */
public class GameVersion implements Comparable<GameVersion> {

    /**
     * Pattern for stable releases like "0.G", "0.F-3", "0.E".
     * Group 1: major version (number)
     * Group 2: minor version (letter)
     * Group 3: optional patch number
     */
    private static final Pattern STABLE_PATTERN = Pattern.compile("^(\\d+)\\.([A-Z])(?:-(\\d+))?$");

    /**
     * Pattern for experimental/date-based releases like "2024-01-15-0123" or "cdda-experimental-2024-01-15-0123".
     * Group 1: year
     * Group 2: month
     * Group 3: day
     * Group 4: build number
     */
    private static final Pattern DATE_PATTERN = Pattern.compile("^(?:cdda-experimental-)?(\\d{4})-(\\d{2})-(\\d{2})-(\\d+)$");

    private enum VersionType {
        STABLE,
        EXPERIMENTAL,
        GENERIC
    }

    private final String originalVersion;
    private final VersionType type;

    // Stable version components
    private int stableMajor;
    private char stableMinor;
    private int stablePatch;

    // Experimental version components
    private int expYear;
    private int expMonth;
    private int expDay;
    private int expBuild;

    /**
     * Constructs a {@link GameVersion} object from a version string.
     *
     * @param versionString the version string
     * @throws IllegalArgumentException if the version string is null
     */
    public GameVersion(String versionString) {
        if (versionString == null) {
            throw new IllegalArgumentException("Version cannot be null");
        }

        this.originalVersion = versionString.trim();

        // Try to parse as stable version
        Matcher stableMatcher = STABLE_PATTERN.matcher(this.originalVersion);
        if (stableMatcher.matches()) {
            this.type = VersionType.STABLE;
            this.stableMajor = Integer.parseInt(stableMatcher.group(1));
            this.stableMinor = stableMatcher.group(2).charAt(0);
            this.stablePatch = stableMatcher.group(3) != null ? Integer.parseInt(stableMatcher.group(3)) : 0;
            return;
        }

        // Try to parse as experimental/date-based version
        Matcher dateMatcher = DATE_PATTERN.matcher(this.originalVersion);
        if (dateMatcher.matches()) {
            this.type = VersionType.EXPERIMENTAL;
            this.expYear = Integer.parseInt(dateMatcher.group(1));
            this.expMonth = Integer.parseInt(dateMatcher.group(2));
            this.expDay = Integer.parseInt(dateMatcher.group(3));
            this.expBuild = Integer.parseInt(dateMatcher.group(4));
            return;
        }

        // Fallback to generic type
        this.type = VersionType.GENERIC;
    }

    /**
     * Returns the original version string.
     *
     * @return the original version string
     */
    public String getOriginalVersion() {
        return originalVersion;
    }

    /**
     * Checks if this version is a stable release.
     *
     * @return {@code true} if stable, {@code false} otherwise
     */
    public boolean isStable() {
        return type == VersionType.STABLE;
    }

    /**
     * Checks if this version is an experimental/date-based release.
     *
     * @return {@code true} if experimental, {@code false} otherwise
     */
    public boolean isExperimental() {
        return type == VersionType.EXPERIMENTAL;
    }

    @Override
    public int compareTo(GameVersion that) {
        if (that == null) {
            return 1;
        }

        // Same type comparison
        if (this.type == that.type) {
            switch (this.type) {
                case STABLE:
                    return compareStable(that);
                case EXPERIMENTAL:
                    return compareExperimental(that);
                case GENERIC:
                    return this.originalVersion.compareTo(that.originalVersion);
            }
        }

        // Cross-type comparison: Experimental is always considered newer than Stable
        // (experimental builds are cutting-edge development versions)
        if (this.type == VersionType.EXPERIMENTAL && that.type == VersionType.STABLE) {
            return 1;
        }
        if (this.type == VersionType.STABLE && that.type == VersionType.EXPERIMENTAL) {
            return -1;
        }

        // Generic versions compared by string
        return this.originalVersion.compareTo(that.originalVersion);
    }

    private int compareStable(GameVersion that) {
        // Compare major version
        if (this.stableMajor != that.stableMajor) {
            return Integer.compare(this.stableMajor, that.stableMajor);
        }

        // Compare minor version (letter - A < B < C < ... < Z)
        if (this.stableMinor != that.stableMinor) {
            return Character.compare(this.stableMinor, that.stableMinor);
        }

        // Compare patch version
        return Integer.compare(this.stablePatch, that.stablePatch);
    }

    private int compareExperimental(GameVersion that) {
        // Compare year
        if (this.expYear != that.expYear) {
            return Integer.compare(this.expYear, that.expYear);
        }

        // Compare month
        if (this.expMonth != that.expMonth) {
            return Integer.compare(this.expMonth, that.expMonth);
        }

        // Compare day
        if (this.expDay != that.expDay) {
            return Integer.compare(this.expDay, that.expDay);
        }

        // Compare build number
        return Integer.compare(this.expBuild, that.expBuild);
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null || this.getClass() != that.getClass()) {
            return false;
        }
        return this.compareTo((GameVersion) that) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalVersion);
    }

    @Override
    public String toString() {
        return originalVersion;
    }
}

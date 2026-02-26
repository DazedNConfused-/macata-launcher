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
    private static final Pattern DATE_PATTERN = Pattern.compile("^(?:cdda-experimental-|cataclysm-tlg-.*)?(\\d{4})-(\\d{2})-(\\d{2})(-(\\d+))?$");

    /**
     * Enum representing the type of version.
     * <ul>
     *     <li>{@code STABLE}: Stable release versions</li>
     *     <li>{@code EXPERIMENTAL}: Experimental or date-based versions</li>
     *     <li>{@code GENERIC}: Generic versions that do not match the other patterns</li>
     * </ul>
     */
    private enum VersionType {
        STABLE,
        EXPERIMENTAL,
        GENERIC
    }

    private final String originalVersion; // the original version string provided
    private final VersionType type; // the type of the version (STABLE, EXPERIMENTAL, GENERIC)

    // stable version components
    private int stableMajor; // major version number for stable releases
    private char stableMinor; // minor version letter for stable releases
    private int stablePatch; // patch number for stable releases (optional)

    // experimental version components
    private int expYear; // year component for experimental versions
    private int expMonth; // month component for experimental versions
    private int expDay; // day component for experimental versions
    private int expBuild; // build number for experimental versions

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

        // try to parse as stable version
        Matcher stableMatcher = STABLE_PATTERN.matcher(this.originalVersion);
        if (stableMatcher.matches()) {
            this.type = VersionType.STABLE;
            this.stableMajor = Integer.parseInt(stableMatcher.group(1));
            this.stableMinor = stableMatcher.group(2).charAt(0);
            this.stablePatch = stableMatcher.group(3) != null ? Integer.parseInt(stableMatcher.group(3)) : 0;
            return;
        }

        // try to parse as experimental/date-based version
        Matcher dateMatcher = DATE_PATTERN.matcher(this.originalVersion);
        if (dateMatcher.matches()) {
            this.type = VersionType.EXPERIMENTAL;
            this.expYear = Integer.parseInt(dateMatcher.group(1));
            this.expMonth = Integer.parseInt(dateMatcher.group(2));
            this.expDay = Integer.parseInt(dateMatcher.group(3));
            this.expBuild = dateMatcher.group(5) != null ? Integer.parseInt(dateMatcher.group(5)) : 0;
            return;
        }

        // fallback to generic type
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

    /**
     * Compares this {@link GameVersion} with another {@link GameVersion}.
     *
     * <p>Comparison rules:
     * <ul>
     *     <li>Stable versions are compared by major, minor, and patch numbers.</li>
     *     <li>Experimental versions are compared by year, month, day, and build number.</li>
     *     <li>Generic versions are compared lexicographically by their original strings.</li>
     *     <li>Experimental versions are always considered newer than stable versions.</li>
     * </ul>
     *
     * @param that the other {@link GameVersion} to compare to
     * @return a negative integer, zero, or a positive integer as this version is less than, equal to, or greater than the specified version
     */
    @Override
    public int compareTo(GameVersion that) {
        if (that == null) {
            return 1;
        }

        // same type comparison
        if (this.type == that.type) {
            switch (this.type) {
                case STABLE:
                    return compareStable(that);
                case EXPERIMENTAL:
                    return compareExperimental(that);
                case GENERIC:
                default:
                    return this.originalVersion.compareTo(that.originalVersion);
            }
        }

        // cross-type comparison: Experimental is always considered newer than Stable
        // (experimental builds are cutting-edge development versions)
        if (this.type == VersionType.EXPERIMENTAL && that.type == VersionType.STABLE) {
            return 1;
        }
        if (this.type == VersionType.STABLE && that.type == VersionType.EXPERIMENTAL) {
            return -1;
        }

        // generic versions compared by string
        return this.originalVersion.compareTo(that.originalVersion);
    }

    /**
     * Compares two stable versions.
     *
     * @param that the other {@link GameVersion} to compare to
     * @return a negative integer, zero, or a positive integer as this stable version is less than, equal to, or greater than
     *         the specified stable version
     */
    private int compareStable(GameVersion that) {
        // compare major version
        if (this.stableMajor != that.stableMajor) {
            return Integer.compare(this.stableMajor, that.stableMajor);
        }

        // compare minor version (letter - A < B < C < ... < Z)
        if (this.stableMinor != that.stableMinor) {
            return Character.compare(this.stableMinor, that.stableMinor);
        }

        // compare patch version
        return Integer.compare(this.stablePatch, that.stablePatch);
    }

    /**
     * Compares two experimental versions.
     *
     * @param that the other {@link GameVersion} to compare to
     * @return a negative integer, zero, or a positive integer as this experimental version is less than, equal to, or greater
     *         than the specified experimental version
     */
    private int compareExperimental(GameVersion that) {
        // compare year
        if (this.expYear != that.expYear) {
            return Integer.compare(this.expYear, that.expYear);
        }

        // compare month
        if (this.expMonth != that.expMonth) {
            return Integer.compare(this.expMonth, that.expMonth);
        }

        // compare day
        if (this.expDay != that.expDay) {
            return Integer.compare(this.expDay, that.expDay);
        }

        // compare build number
        return Integer.compare(this.expBuild, that.expBuild);
    }

    /**
     * Checks if this {@link GameVersion} is equal to another object.
     *
     * <p>Two {@link GameVersion} objects are considered equal if their comparison result is zero.
     *
     * @param that the object to compare to
     * @return {@code true} if the objects are equal, {@code false} otherwise
     */
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

    /**
     * Returns the hash code for this {@link GameVersion}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(originalVersion);
    }

    /**
     * Returns the string representation of this {@link GameVersion}.
     *
     * @return the original version string
     */
    @Override
    public String toString() {
        return originalVersion;
    }
}
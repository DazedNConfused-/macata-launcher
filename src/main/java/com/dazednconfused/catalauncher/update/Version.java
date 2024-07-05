package com.dazednconfused.catalauncher.update;

import java.util.Objects;

/**
 * @see <a href="https://stackoverflow.com/a/11024200">https://stackoverflow.com/a/11024200</a>
 * */
public class Version implements Comparable<Version> {

    private final String version;

    public final String get() {
        return this.version;
    }

    /**
     * Constructor.
     * */
    public Version(String version) {
        if (version == null) {
            throw new IllegalArgumentException("Version cannot be null");
        }
        if (!version.matches("v?[0-9]+(\\.[0-9]+)*")) {
            throw new IllegalArgumentException("Invalid version format");

        }
        if (version.startsWith("v")) {
            // prune any potential version tags starting with a 'v'
            version = version.substring(1);
        }

        this.version = version;
    }

    @Override
    public int compareTo(Version that) {
        if (that == null) {
            return 1;
        }
        String[] thisParts = this.get().split("\\.");
        String[] thatParts = that.get().split("\\.");
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
        return 0;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (this.getClass() != that.getClass()) {
            return false;
        }
        return this.compareTo((Version) that) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(version);
    }

    @Override
    public String toString() {
        return this.version;
    }
}
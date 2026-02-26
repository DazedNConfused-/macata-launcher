package com.dazednconfused.catalauncher.update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class GameVersionTest {

    // ========================================
    // Constructor tests
    // ========================================

    @Test
    void constructor_with_null_throws_exception() {
        assertThatThrownBy(() -> new GameVersion(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Version cannot be null");
    }

    @Test
    void constructor_with_stable_version_success() {
        // execute test ---
        GameVersion version = new GameVersion("0.G");

        // verify assertions ---
        assertThat(version.isStable()).isTrue();
        assertThat(version.isExperimental()).isFalse();
        assertThat(version.getOriginalVersion()).isEqualTo("0.G");
    }

    @Test
    void constructor_with_stable_version_with_patch_success() {
        // execute test ---
        GameVersion version = new GameVersion("0.F-3");

        // verify assertions ---
        assertThat(version.isStable()).isTrue();
        assertThat(version.isExperimental()).isFalse();
        assertThat(version.getOriginalVersion()).isEqualTo("0.F-3");
    }

    @Test
    void constructor_with_experimental_version_success() {
        // execute test ---
        GameVersion version = new GameVersion("2024-01-15-0123");

        // verify assertions ---
        assertThat(version.isStable()).isFalse();
        assertThat(version.isExperimental()).isTrue();
        assertThat(version.getOriginalVersion()).isEqualTo("2024-01-15-0123");
    }

    @Test
    void constructor_with_cdda_experimental_prefix_success() {
        // execute test ---
        GameVersion version = new GameVersion("cdda-experimental-2024-01-15-0123");

        // verify assertions ---
        assertThat(version.isStable()).isFalse();
        assertThat(version.isExperimental()).isTrue();
        assertThat(version.getOriginalVersion()).isEqualTo("cdda-experimental-2024-01-15-0123");
    }

    @Test
    void constructor_with_generic_version_success() {
        // execute test ---
        GameVersion version = new GameVersion("some-random-tag");

        // verify assertions ---
        assertThat(version.isStable()).isFalse();
        assertThat(version.isExperimental()).isFalse();
        assertThat(version.getOriginalVersion()).isEqualTo("some-random-tag");
    }

    @Test
    void constructor_trims_whitespace_success() {
        // execute test ---
        GameVersion version = new GameVersion("  0.G  ");

        // verify assertions ---
        assertThat(version.getOriginalVersion()).isEqualTo("0.G");
        assertThat(version.isStable()).isTrue();
    }

    // ========================================
    // Stable version comparison tests
    // ========================================

    @Test
    void compare_stable_versions_same_success() {
        // prepare mock data ---
        GameVersion a = new GameVersion("0.G");
        GameVersion b = new GameVersion("0.G");

        // verify assertions ---
        assertThat(a.compareTo(b)).isEqualTo(0);
        assertThat(a.equals(b)).isTrue();
    }

    @Test
    void compare_stable_versions_different_minor_success() {
        // prepare mock data ---
        GameVersion older = new GameVersion("0.F");
        GameVersion newer = new GameVersion("0.G");

        // verify assertions ---
        assertThat(older.compareTo(newer)).isLessThan(0);
        assertThat(newer.compareTo(older)).isGreaterThan(0);
        assertThat(older.equals(newer)).isFalse();
    }

    @Test
    void compare_stable_versions_different_major_success() {
        // prepare mock data ---
        GameVersion older = new GameVersion("0.Z");
        GameVersion newer = new GameVersion("1.A");

        // verify assertions ---
        assertThat(older.compareTo(newer)).isLessThan(0);
        assertThat(newer.compareTo(older)).isGreaterThan(0);
    }

    @Test
    void compare_stable_versions_with_patch_success() {
        // prepare mock data ---
        GameVersion base = new GameVersion("0.F");
        GameVersion patch1 = new GameVersion("0.F-1");
        GameVersion patch3 = new GameVersion("0.F-3");

        // verify assertions ---
        assertThat(base.compareTo(patch1)).isLessThan(0);
        assertThat(patch1.compareTo(patch3)).isLessThan(0);
        assertThat(patch3.compareTo(base)).isGreaterThan(0);
    }

    @Test
    void compare_stable_versions_minor_letter_ordering_success() {
        // prepare mock data ---
        GameVersion versionA = new GameVersion("0.A");
        GameVersion versionE = new GameVersion("0.E");
        GameVersion versionZ = new GameVersion("0.Z");

        // verify assertions ---
        assertThat(versionA.compareTo(versionE)).isLessThan(0);
        assertThat(versionE.compareTo(versionZ)).isLessThan(0);
        assertThat(versionZ.compareTo(versionA)).isGreaterThan(0);
    }

    // ========================================
    // Experimental version comparison tests
    // ========================================

    @Test
    void compare_experimental_versions_same_success() {
        // prepare mock data ---
        GameVersion a = new GameVersion("2024-01-15-0123");
        GameVersion b = new GameVersion("2024-01-15-0123");

        // verify assertions ---
        assertThat(a.compareTo(b)).isEqualTo(0);
        assertThat(a.equals(b)).isTrue();
    }

    @Test
    void compare_experimental_versions_different_year_success() {
        // prepare mock data ---
        GameVersion older = new GameVersion("2023-12-31-9999");
        GameVersion newer = new GameVersion("2024-01-01-0001");

        // verify assertions ---
        assertThat(older.compareTo(newer)).isLessThan(0);
        assertThat(newer.compareTo(older)).isGreaterThan(0);
    }

    @Test
    void compare_experimental_versions_different_month_success() {
        // prepare mock data ---
        GameVersion older = new GameVersion("2024-01-31-9999");
        GameVersion newer = new GameVersion("2024-02-01-0001");

        // verify assertions ---
        assertThat(older.compareTo(newer)).isLessThan(0);
        assertThat(newer.compareTo(older)).isGreaterThan(0);
    }

    @Test
    void compare_experimental_versions_different_day_success() {
        // prepare mock data ---
        GameVersion older = new GameVersion("2024-01-15-9999");
        GameVersion newer = new GameVersion("2024-01-16-0001");

        // verify assertions ---
        assertThat(older.compareTo(newer)).isLessThan(0);
        assertThat(newer.compareTo(older)).isGreaterThan(0);
    }

    @Test
    void compare_experimental_versions_different_build_success() {
        // prepare mock data ---
        GameVersion older = new GameVersion("2024-01-15-0001");
        GameVersion newer = new GameVersion("2024-01-15-0002");

        // verify assertions ---
        assertThat(older.compareTo(newer)).isLessThan(0);
        assertThat(newer.compareTo(older)).isGreaterThan(0);
    }

    @Test
    void compare_experimental_versions_with_prefix_success() {
        // prepare mock data ---
        GameVersion withPrefix = new GameVersion("cdda-experimental-2024-01-15-0123");
        GameVersion withoutPrefix = new GameVersion("2024-01-15-0123");

        // verify assertions ---
        // both should be parsed as experimental and considered equal in terms of date/build
        assertThat(withPrefix.isExperimental()).isTrue();
        assertThat(withoutPrefix.isExperimental()).isTrue();
    }

    // ========================================
    // Cross-type comparison tests
    // ========================================

    @Test
    void compare_experimental_is_newer_than_stable_success() {
        // prepare mock data ---
        GameVersion stable = new GameVersion("0.G");
        GameVersion experimental = new GameVersion("2024-01-15-0123");

        // verify assertions ---
        assertThat(experimental.compareTo(stable)).isGreaterThan(0);
        assertThat(stable.compareTo(experimental)).isLessThan(0);
    }

    @Test
    void compare_with_null_success() {
        // prepare mock data ---
        GameVersion version = new GameVersion("0.G");

        // verify assertions ---
        assertThat(version.compareTo(null)).isEqualTo(1);
    }

    // ========================================
    // Generic version comparison tests
    // ========================================

    @Test
    void compare_generic_versions_lexicographically_success() {
        // prepare mock data ---
        GameVersion a = new GameVersion("alpha-release");
        GameVersion b = new GameVersion("beta-release");

        // verify assertions ---
        assertThat(a.compareTo(b)).isLessThan(0);
        assertThat(b.compareTo(a)).isGreaterThan(0);
    }

    @Test
    void compare_generic_versions_same_success() {
        // prepare mock data ---
        GameVersion a = new GameVersion("custom-tag-v1");
        GameVersion b = new GameVersion("custom-tag-v1");

        // verify assertions ---
        assertThat(a.compareTo(b)).isEqualTo(0);
        assertThat(a.equals(b)).isTrue();
    }

    // ========================================
    // Collection sorting tests
    // ========================================

    @Test
    void versions_comparable_in_collections_stable_success() {
        // prepare mock data ---
        List<GameVersion> versions = new ArrayList<>();
        versions.add(new GameVersion("0.G"));
        versions.add(new GameVersion("0.E"));
        versions.add(new GameVersion("0.F-3"));
        versions.add(new GameVersion("0.F"));

        // execute test ---
        Collections.sort(versions);

        // verify assertions ---
        assertThat(versions.get(0).getOriginalVersion()).isEqualTo("0.E");
        assertThat(versions.get(1).getOriginalVersion()).isEqualTo("0.F");
        assertThat(versions.get(2).getOriginalVersion()).isEqualTo("0.F-3");
        assertThat(versions.get(3).getOriginalVersion()).isEqualTo("0.G");
    }

    @Test
    void versions_comparable_in_collections_experimental_success() {
        // prepare mock data ---
        List<GameVersion> versions = new ArrayList<>();
        versions.add(new GameVersion("2024-02-15-0200"));
        versions.add(new GameVersion("2024-01-15-0100"));
        versions.add(new GameVersion("2024-02-15-0100"));
        versions.add(new GameVersion("2023-12-31-9999"));

        // execute test ---
        Collections.sort(versions);

        // verify assertions ---
        assertThat(versions.get(0).getOriginalVersion()).isEqualTo("2023-12-31-9999");
        assertThat(versions.get(1).getOriginalVersion()).isEqualTo("2024-01-15-0100");
        assertThat(versions.get(2).getOriginalVersion()).isEqualTo("2024-02-15-0100");
        assertThat(versions.get(3).getOriginalVersion()).isEqualTo("2024-02-15-0200");
    }

    @Test
    void versions_min_max_in_collections_success() {
        // prepare mock data ---
        List<GameVersion> versions = new ArrayList<>();
        versions.add(new GameVersion("0.G"));
        versions.add(new GameVersion("0.E"));
        versions.add(new GameVersion("2024-01-15-0100"));

        // verify assertions ---
        assertThat(Collections.min(versions).getOriginalVersion()).isEqualTo("0.E");
        assertThat(Collections.max(versions).getOriginalVersion()).isEqualTo("2024-01-15-0100");
    }

    // ========================================
    // equals() and hashCode() tests
    // ========================================

    @Test
    void equals_same_instance_success() {
        // prepare mock data ---
        GameVersion version = new GameVersion("0.G");

        // verify assertions ---
        assertThat(version.equals(version)).isTrue();
    }

    @Test
    void equals_different_class_success() {
        // prepare mock data ---
        GameVersion version = new GameVersion("0.G");

        // verify assertions ---
        assertThat(version.equals("0.G")).isFalse();
    }

    @Test
    void equals_null_success() {
        // prepare mock data ---
        GameVersion version = new GameVersion("0.G");

        // verify assertions ---
        assertThat(version.equals(null)).isFalse();
    }

    @Test
    void hashCode_same_version_success() {
        // prepare mock data ---
        GameVersion a = new GameVersion("0.G");
        GameVersion b = new GameVersion("0.G");

        // verify assertions ---
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    // ========================================
    // toString() tests
    // ========================================

    @Test
    void toString_returns_original_version_success() {
        // prepare mock data ---
        GameVersion version = new GameVersion("0.G");

        // verify assertions ---
        assertThat(version.toString()).isEqualTo("0.G");
    }

    @Test
    void toString_returns_experimental_version_success() {
        // prepare mock data ---
        GameVersion version = new GameVersion("cdda-experimental-2024-01-15-0123");

        // verify assertions ---
        assertThat(version.toString()).isEqualTo("cdda-experimental-2024-01-15-0123");
    }

    // ========================================
    // Edge case tests
    // ========================================

    @Test
    void stable_version_H_RELEASE_format_success() {
        // this is a real format used by CleverRaven/Cataclysm-DDA
        GameVersion version = new GameVersion("0.H-RELEASE");

        // this should be parsed as generic since it doesn't match stable pattern exactly
        assertThat(version.getOriginalVersion()).isEqualTo("0.H-RELEASE");
    }

    @Test
    void cataclysm_tlg_prefix_experimental_success() {
        // test cataclysm-tlg prefix format
        GameVersion version = new GameVersion("cataclysm-tlg-1.0-2024-01-15-0123");

        // verify assertions ---
        assertThat(version.isExperimental()).isTrue();
    }

    @Test
    void empty_string_parsed_as_generic_success() {
        // execute test ---
        GameVersion version = new GameVersion("");

        // verify assertions ---
        assertThat(version.isStable()).isFalse();
        assertThat(version.isExperimental()).isFalse();
        assertThat(version.getOriginalVersion()).isEqualTo("");
    }
}

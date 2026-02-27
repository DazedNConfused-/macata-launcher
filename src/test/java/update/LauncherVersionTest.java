package update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.dazednconfused.catalauncher.update.LauncherVersion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class LauncherVersionTest {

    @Test
    void version_compare_minor_with_no_minor_success() {
        LauncherVersion a = new LauncherVersion("1.1");
        LauncherVersion b = new LauncherVersion("1.1.1");

        assertThat(a.compareTo(b)).isEqualTo(-1); // (a<b)
        assertThat(a.equals(b)).isFalse();
    }

    @Test
    void version_compare_major_with_latest_minor_success() {
        LauncherVersion a = new LauncherVersion("2.0");
        LauncherVersion b = new LauncherVersion("1.9.9");

        assertThat(a.compareTo(b)).isEqualTo(1); // (a>b)
        assertThat(a.equals(b)).isFalse();
    }

    @Test
    void version_compare_minor_zero_with_no_minor_success() {
        LauncherVersion a = new LauncherVersion("1.0");
        LauncherVersion b = new LauncherVersion("1");

        assertThat(a.compareTo(b)).isEqualTo(0); // (a=b)
        assertThat(a.equals(b)).isTrue();
    }

    @Test
    void version_compare_with_null_success() {
        LauncherVersion a = new LauncherVersion("1");
        LauncherVersion b = null;

        assertThat(a.compareTo(b)).isEqualTo(1); // (a>b)
        assertThat(a.equals(b)).isFalse();
    }

    @Test
    void versions_comparable_in_collections_success() {
        List<LauncherVersion> versions = new ArrayList<LauncherVersion>();
        versions.add(new LauncherVersion("2"));
        versions.add(new LauncherVersion("1.0.5"));
        versions.add(new LauncherVersion("1.01.0"));
        versions.add(new LauncherVersion("1.00.1"));

        assertThat(Collections.min(versions).getSemver()).isEqualTo("1.00.1");
        assertThat(Collections.max(versions).getSemver()).isEqualTo("2");
    }

    @Test
    void versions_does_not_consider_extra_zeroes_as_increments_success() {
        LauncherVersion a = new LauncherVersion("2.06");
        LauncherVersion b = new LauncherVersion("2.060");

        assertThat(a.equals(b)).isFalse();
    }

    @Test
    void versions_is_compatible_with_v_prefix() {
        LauncherVersion a = new LauncherVersion("v1.0.1");
        LauncherVersion b = new LauncherVersion("1.0.1");

        assertThat(a.equals(b)).isTrue();
    }

    @Test
    void version_with_prerelease_is_lower_than_release() {
        LauncherVersion prerelease = new LauncherVersion("prerelease-1.0.0-alpha");
        LauncherVersion release = new LauncherVersion("1.0.0");

        assertThat(prerelease.compareTo(release)).isEqualTo(-1); // (prerelease < release)
        assertThat(prerelease).isNotEqualTo(release);
    }

    @Test
    void version_with_prerelease_tags_are_compared_lexicographically() {
        LauncherVersion alpha = new LauncherVersion("prerelease-1.0.0-alpha");
        LauncherVersion beta = new LauncherVersion("prerelease-1.0.0-beta");

        assertThat(alpha.compareTo(beta)).isEqualTo(-1); // (alpha < beta)
        assertThat(alpha).isNotEqualTo(beta);
    }

    @Test
    void version_with_prerelease_and_no_tag_is_lower_than_with_tag() {
        LauncherVersion noTag = new LauncherVersion("prerelease-1.0.0");
        LauncherVersion withTag = new LauncherVersion("prerelease-1.0.0-alpha");

        assertThat(noTag.compareTo(withTag)).isEqualTo(-1); // (noTag < withTag)
        assertThat(noTag).isNotEqualTo(withTag);
    }

    @Test
    void version_with_invalid_format_throws_exception() {
        assertThatThrownBy(() -> new LauncherVersion("invalid-version"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid version format");
    }

    @Test
    void version_with_null_string_throws_exception() {
        assertThatThrownBy(() -> new LauncherVersion(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Version cannot be null");
    }

    @Test
    void version_with_extra_zeroes_in_prerelease_tag_is_not_equal() {
        LauncherVersion a = new LauncherVersion("prerelease-1.0.0-alpha");
        LauncherVersion b = new LauncherVersion("prerelease-1.0.0-alpha.0");

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void version_with_prerelease_and_same_base_version_is_equal() {
        LauncherVersion a = new LauncherVersion("prerelease-1.0.0");
        LauncherVersion b = new LauncherVersion("prerelease-1.0.0");

        assertThat(a.compareTo(b)).isEqualTo(0); // (a == b)
        assertThat(a.equals(b)).isTrue();
    }

    @Test
    void version_toString_returns_expected_for_release() {
        LauncherVersion v = new LauncherVersion("1.2.3");
        assertThat(v.toString()).hasToString("v1.2.3");
    }

    @Test
    void version_toString_returns_expected_for_release_with_v_prefix() {
        LauncherVersion v = new LauncherVersion("v2.0.1");
        assertThat(v.toString()).hasToString("v2.0.1");
    }

    @Test
    void version_toString_returns_expected_for_prerelease_with_tag() {
        LauncherVersion v = new LauncherVersion("prerelease-1.2.3-alpha");
        assertThat(v.toString()).hasToString("prerelease-1.2.3-alpha");
    }

    @Test
    void version_toString_returns_expected_for_prerelease_with_complex_tag() {
        LauncherVersion v = new LauncherVersion("prerelease-1.2.3-20240601_123456");
        assertThat(v.toString()).hasToString("prerelease-1.2.3-20240601_123456");
    }

    @Test
    void version_toString_returns_expected_for_prerelease_without_tag() {
        LauncherVersion v = new LauncherVersion("prerelease-1.2.3");
        assertThat(v.toString()).hasToString("prerelease-1.2.3");
    }

}

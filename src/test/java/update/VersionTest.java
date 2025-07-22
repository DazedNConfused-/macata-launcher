package update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.dazednconfused.catalauncher.update.Version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class VersionTest {

    @Test
    void version_compare_minor_with_no_minor_success() {
        Version a = new Version("1.1");
        Version b = new Version("1.1.1");

        assertThat(a.compareTo(b)).isEqualTo(-1); // (a<b)
        assertThat(a.equals(b)).isFalse();
    }

    @Test
    void version_compare_major_with_latest_minor_success() {
        Version a = new Version("2.0");
        Version b = new Version("1.9.9");

        assertThat(a.compareTo(b)).isEqualTo(1); // (a>b)
        assertThat(a.equals(b)).isFalse();
    }

    @Test
    void version_compare_minor_zero_with_no_minor_success() {
        Version a = new Version("1.0");
        Version b = new Version("1");

        assertThat(a.compareTo(b)).isEqualTo(0); // (a=b)
        assertThat(a.equals(b)).isTrue();
    }

    @Test
    void version_compare_with_null_success() {
        Version a = new Version("1");
        Version b = null;

        assertThat(a.compareTo(b)).isEqualTo(1); // (a>b)
        assertThat(a.equals(b)).isFalse();
    }

    @Test
    void versions_comparable_in_collections_success() {
        List<Version> versions = new ArrayList<Version>();
        versions.add(new Version("2"));
        versions.add(new Version("1.0.5"));
        versions.add(new Version("1.01.0"));
        versions.add(new Version("1.00.1"));

        assertThat(Collections.min(versions).getSemver()).isEqualTo("1.00.1");
        assertThat(Collections.max(versions).getSemver()).isEqualTo("2");
    }

    @Test
    void versions_does_not_consider_extra_zeroes_as_increments_success() {
        Version a = new Version("2.06");
        Version b = new Version("2.060");

        assertThat(a.equals(b)).isFalse();
    }

    @Test
    void versions_is_compatible_with_v_prefix() {
        Version a = new Version("v1.0.1");
        Version b = new Version("1.0.1");

        assertThat(a.equals(b)).isTrue();
    }

    @Test
    void version_with_prerelease_is_lower_than_release() {
        Version prerelease = new Version("prerelease-1.0.0-alpha");
        Version release = new Version("1.0.0");

        assertThat(prerelease.compareTo(release)).isEqualTo(-1); // (prerelease < release)
        assertThat(prerelease.equals(release)).isFalse();
    }

    @Test
    void version_with_prerelease_tags_are_compared_lexicographically() {
        Version alpha = new Version("prerelease-1.0.0-alpha");
        Version beta = new Version("prerelease-1.0.0-beta");

        assertThat(alpha.compareTo(beta)).isEqualTo(-1); // (alpha < beta)
        assertThat(alpha.equals(beta)).isFalse();
    }

    @Test
    void version_with_prerelease_and_no_tag_is_lower_than_with_tag() {
        Version noTag = new Version("prerelease-1.0.0");
        Version withTag = new Version("prerelease-1.0.0-alpha");

        assertThat(noTag.compareTo(withTag)).isEqualTo(-1); // (noTag < withTag)
        assertThat(noTag.equals(withTag)).isFalse();
    }

    @Test
    void version_with_invalid_format_throws_exception() {
        assertThatThrownBy(() -> new Version("invalid-version"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid version format");
    }

    @Test
    void version_with_null_string_throws_exception() {
        assertThatThrownBy(() -> new Version(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Version cannot be null");
    }

    @Test
    void version_with_extra_zeroes_in_prerelease_tag_is_not_equal() {
        Version a = new Version("prerelease-1.0.0-alpha");
        Version b = new Version("prerelease-1.0.0-alpha.0");

        assertThat(a.equals(b)).isFalse();
    }

    @Test
    void version_with_prerelease_and_same_base_version_is_equal() {
        Version a = new Version("prerelease-1.0.0");
        Version b = new Version("prerelease-1.0.0");

        assertThat(a.compareTo(b)).isEqualTo(0); // (a == b)
        assertThat(a.equals(b)).isTrue();
    }

    @Test
    void version_toString_returns_expected_for_release() {
        Version v = new Version("1.2.3");
        assertThat(v.toString()).isEqualTo("v1.2.3");
    }

    @Test
    void version_toString_returns_expected_for_release_with_v_prefix() {
        Version v = new Version("v2.0.1");
        assertThat(v.toString()).isEqualTo("v2.0.1");
    }

    @Test
    void version_toString_returns_expected_for_prerelease_with_tag() {
        Version v = new Version("prerelease-1.2.3-alpha");
        assertThat(v.toString()).isEqualTo("prerelease-1.2.3-alpha");
    }

    @Test
    void version_toString_returns_expected_for_prerelease_with_complex_tag() {
        Version v = new Version("prerelease-1.2.3-20240601_123456");
        assertThat(v.toString()).isEqualTo("prerelease-1.2.3-20240601_123456");
    }

    @Test
    void version_toString_returns_expected_for_prerelease_without_tag() {
        Version v = new Version("prerelease-1.2.3");
        assertThat(v.toString()).isEqualTo("v1.2.3");
    }

}

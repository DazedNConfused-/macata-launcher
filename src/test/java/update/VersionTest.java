package update;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(Collections.min(versions).get()).isEqualTo("1.00.1");
        assertThat(Collections.max(versions).get()).isEqualTo("2");
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
}

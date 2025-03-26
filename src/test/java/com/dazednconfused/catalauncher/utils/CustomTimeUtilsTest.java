package com.dazednconfused.catalauncher.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomTimeUtilsTest {

    @Test
    void getYyyyMmDdHhMmSsTimestamp_returnsNonEmptyString() {
        String timestamp = CustomTimeUtils.getYyyyMmDdHhMmSsTimestamp();
        assertThat(timestamp).isNotEmpty();
    }

    @Test
    void getYyyyMmDdHhMmSsTimestamp_returnsCorrectFormat() {
        String timestamp = CustomTimeUtils.getYyyyMmDdHhMmSsTimestamp();
        assertThat(timestamp).matches("\\d{8}_\\d{6}");
    }

}
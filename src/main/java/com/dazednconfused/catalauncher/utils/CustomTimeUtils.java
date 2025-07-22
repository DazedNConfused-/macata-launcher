package com.dazednconfused.catalauncher.utils;

import java.text.SimpleDateFormat;

public class CustomTimeUtils {

    /**
     * Generates a timestamp based on the current date and time.
     * The format of the timestamp is "yyyyMMdd_HHmmss".
     *
     * @return a string representing the current timestamp in the format "yyyyMMdd_HHmmss"
     */
    public static String getYyyyMmDdHhMmSsTimestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
    }

}

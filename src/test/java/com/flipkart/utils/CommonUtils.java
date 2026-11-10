package com.flipkart.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class CommonUtils {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private CommonUtils() {
    }

    public static String timestamp() {
        return LocalDateTime.now().format(FORMAT);
    }

    // masks the value for logs/report, eg 9876543210 becomes 98******10
    public static String mask(String value) {
        if (value == null || value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "*".repeat(value.length() - 4) + value.substring(value.length() - 2);
    }
}

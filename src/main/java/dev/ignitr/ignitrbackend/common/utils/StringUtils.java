package dev.ignitr.ignitrbackend.common.utils;

public class StringUtils {

    private StringUtils() { }

    public static boolean isNotNullOrEmpty(String str) {
        return str != null && !str.isEmpty();
    }
}

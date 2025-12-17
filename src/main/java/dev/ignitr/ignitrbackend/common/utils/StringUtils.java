package dev.ignitr.ignitrbackend.common.utils;

import org.bson.types.ObjectId;

public class StringUtils {

    private StringUtils() { }

    public static boolean isNotNullOrEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    public static boolean isValidObjectId(String id) {
        return ObjectId.isValid(id);
    }
}

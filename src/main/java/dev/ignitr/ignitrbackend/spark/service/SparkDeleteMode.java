package dev.ignitr.ignitrbackend.spark.service;

import lombok.Getter;

@Getter
public enum SparkDeleteMode {
    CASCADE("cascade"),
    PROMOTE("promote");

    private final String mode;

    SparkDeleteMode(String mode) {
        this.mode = mode;
    }

    public static SparkDeleteMode fromValue(String value) {
        for (SparkDeleteMode deleteMode : SparkDeleteMode.values()) {
            if (deleteMode.mode.equalsIgnoreCase(value)) {
                return deleteMode;
            }
        }
        throw new IllegalArgumentException("Unknown spark delete mode: '" + value + "'. Valid values are: 'cascade', 'promote'");
    }
}

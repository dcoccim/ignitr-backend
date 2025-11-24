package dev.ignitr.ignitrbackend.spark.exception;

import lombok.Getter;

@Getter
public class SparkNotFoundException extends RuntimeException {

    private final String id;

    public SparkNotFoundException(String id) {
        super("Spark with id '%s' was not found".formatted(id));
        this.id = id;
    }
}

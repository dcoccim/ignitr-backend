package dev.ignitr.ignitrbackend.spark.exception;

import lombok.Getter;

@Getter
public class SparkAlreadyExistsException extends RuntimeException{

    private final String title;

    public SparkAlreadyExistsException(String title) {
        super("A spark titled '%s' already exists".formatted(title));
        this.title = title;
    }

}

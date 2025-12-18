package dev.ignitr.ignitrbackend.spark.exception;

import lombok.Getter;
import org.bson.types.ObjectId;

@Getter
public class SparkNotFoundException extends RuntimeException {

    private final ObjectId id;

    public SparkNotFoundException(ObjectId id) {
        super("Spark with id '%s' was not found".formatted(id.toHexString()));
        this.id = id;
    }
}

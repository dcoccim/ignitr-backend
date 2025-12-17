package dev.ignitr.ignitrbackend.reason.exception;

import lombok.Getter;
import org.bson.types.ObjectId;

@Getter
public class ReasonNotFoundException extends RuntimeException {

    private final ObjectId id;

    public ReasonNotFoundException(ObjectId id) {
        super("Reason with id '%s' was not found".formatted(id.toHexString()));
        this.id = id;
    }
}

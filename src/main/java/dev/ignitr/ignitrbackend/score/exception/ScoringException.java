package dev.ignitr.ignitrbackend.score.exception;

import lombok.Getter;
import org.bson.types.ObjectId;

@Getter
public class ScoringException extends RuntimeException {

    private final ObjectId rootId;

    public ScoringException(ObjectId rootId) {
        super("Scoring failed for spark tree with root id '%s'".formatted(rootId.toHexString()));
        this.rootId = rootId;
    }
}

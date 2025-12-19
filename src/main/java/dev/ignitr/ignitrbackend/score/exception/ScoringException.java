package dev.ignitr.ignitrbackend.score.exception;

import lombok.Getter;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ScoringException extends RuntimeException {

    private final List<ObjectId> rootIds;

    /* ===== Primary constructors ===== */

    public ScoringException(String message, List<ObjectId> rootIds, Throwable cause) {
        super(formatMessage(message, rootIds), cause);
        this.rootIds = rootIds;
    }

    public ScoringException(String message, List<ObjectId> rootIds) {
        super(formatMessage(message, rootIds));
        this.rootIds = rootIds;
    }

    public ScoringException(String message, ObjectId rootId, Throwable cause) {
        this(message, List.of(rootId), cause);
    }

    public ScoringException(List<ObjectId> rootIds) {
        this("SparkTree scoring failed", rootIds);
    }

    public ScoringException(ObjectId rootId) {
        this(List.of(rootId));
    }

    private static String formatMessage(String base, List<ObjectId> rootIds) {
        if (rootIds == null || rootIds.isEmpty()) {
            return base;
        }
        return "%s for root id(s): %s".formatted(
                base,
                rootIds.stream()
                        .map(ObjectId::toHexString)
                        .collect(Collectors.joining(","))
        );
    }
}
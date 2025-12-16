package dev.ignitr.ignitrbackend.score.dto;

import dev.ignitr.ignitrbackend.reason.model.ReasonType;

import java.time.Instant;
import java.util.List;

public record SparkScoreRequestDTO(
    String id,
    String parentId,
    List<ReasonScoreRequestDTO> reasons,
    Instant createdAt
) {
    public record ReasonScoreRequestDTO(
            ReasonType type,
            int votes
    ) {}
}
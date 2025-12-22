package dev.ignitr.ignitrbackend.spark.dto;

import java.time.Instant;
import java.util.List;

public record SparkTreeDTO(
        String id,
        String title,
        String description,
        int goodReasonsCount,
        int badReasonsCount,
        Integer score,
        int depthBelow,
        Instant createdAt,
        Instant updatedAt,
        int childrenCount,
        List<SparkTreeDTO> children
) {}
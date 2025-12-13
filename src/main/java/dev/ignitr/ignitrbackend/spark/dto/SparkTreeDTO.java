package dev.ignitr.ignitrbackend.spark.dto;

import dev.ignitr.ignitrbackend.spark.model.Spark;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record SparkTreeDTO(
        String id,
        String title,
        String description,
        int goodReasonsCount,
        int badReasonsCount,
        Instant createdAt,
        Instant updatedAt,
        List<SparkTreeDTO> children
) {
    public static SparkTreeDTO from(Spark s, int goodReasonsCount, int badReasonsCount) {
        return new SparkTreeDTO(
                s.getId(),
                s.getTitle(),
                s.getDescription(),
                goodReasonsCount,
                badReasonsCount,
                s.getCreatedAt(),
                s.getUpdatedAt(),
                new ArrayList<>()
        );
    }
}
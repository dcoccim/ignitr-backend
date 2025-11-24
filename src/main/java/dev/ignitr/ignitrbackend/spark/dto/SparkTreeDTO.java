package dev.ignitr.ignitrbackend.spark.dto;

import java.time.Instant;
import java.util.List;

public record SparkTreeDTO(
        String id,
        String title,
        String description,
        Instant createdAt,
        Instant updatedAt,
        List<SparkTreeDTO> children
) {}

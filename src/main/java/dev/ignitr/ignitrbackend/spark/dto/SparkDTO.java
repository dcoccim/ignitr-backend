package dev.ignitr.ignitrbackend.spark.dto;

import java.time.Instant;

public record SparkDTO (
    String id,
    String title,
    String description,
    Instant createdAt,
    Instant updatedAt
) {}

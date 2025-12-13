package dev.ignitr.ignitrbackend.spark.dto;

import dev.ignitr.ignitrbackend.reason.dto.ReasonDTO;

import java.time.Instant;
import java.util.List;

public record SparkDTO (
    String id,
    String title,
    String description,
    List<ReasonDTO> reasons,
    Instant createdAt,
    Instant updatedAt
) {}

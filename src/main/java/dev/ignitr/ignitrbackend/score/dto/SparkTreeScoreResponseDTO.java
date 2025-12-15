package dev.ignitr.ignitrbackend.score.dto;

import java.util.List;

public record SparkTreeScoreResponseDTO(
    String id,
    int score,
    List<SparkTreeScoreResponseDTO> children
) {}

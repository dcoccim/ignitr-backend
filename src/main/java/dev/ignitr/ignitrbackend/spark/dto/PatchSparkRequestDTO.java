package dev.ignitr.ignitrbackend.spark.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PatchSparkRequestDTO(
        @Pattern(regexp = "^(?!\\s*$).*$", message = "title must not be blank when provided")
        @Size(max = 100, message = "title must be at most 100 characters")
        String title,
        @Size(max = 1500, message ="description must be at most 1500 characters")
        String description
) {}
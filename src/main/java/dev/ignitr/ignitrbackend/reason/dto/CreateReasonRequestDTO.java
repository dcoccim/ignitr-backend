package dev.ignitr.ignitrbackend.reason.dto;

import dev.ignitr.ignitrbackend.reason.model.ReasonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReasonRequestDTO(
        @NotBlank
        @Size(max = 1500, message = "content must be at most 1500 characters")
        String content,
        @NotNull(message = "type must not be null")
        ReasonType type
) {}

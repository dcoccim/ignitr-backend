package dev.ignitr.ignitrbackend.reason.dto;

public record ReasonDTO (
        String id,
        String content,
        String type,
        String createdAt,
        String updatedAt
){}
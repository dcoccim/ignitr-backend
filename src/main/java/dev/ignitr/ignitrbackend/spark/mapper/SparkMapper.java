package dev.ignitr.ignitrbackend.spark.mapper;

import dev.ignitr.ignitrbackend.spark.dto.CreateSparkRequestDTO;
import dev.ignitr.ignitrbackend.spark.dto.SparkDTO;
import dev.ignitr.ignitrbackend.spark.dto.SparkTreeDTO;
import dev.ignitr.ignitrbackend.spark.dto.UpdateSparkRequestDTO;
import dev.ignitr.ignitrbackend.spark.model.Spark;

import java.time.Instant;
import java.util.List;

public class SparkMapper {

    private SparkMapper() {}

    public static Spark toNewEntity(CreateSparkRequestDTO dto, Instant now) {
        return new Spark(
                null,
                dto.title(),
                dto.description(),
                null,
                now,
                now
        );
    }

    public static void updateEntity(Spark spark, UpdateSparkRequestDTO dto, Instant now) {
        spark.setTitle(dto.title());
        spark.setDescription(dto.description());
        spark.setUpdatedAt(now);
    }

    public static Spark toNewChildEntity(CreateSparkRequestDTO dto, String parentId, Instant now) {
        return new Spark(
                null,
                dto.title(),
                dto.description(),
                parentId,
                now,
                now
        );
    }

    public static SparkDTO toSparkDto(Spark entity) {
        return new SparkDTO(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static SparkTreeDTO toTreeDto(Spark entity, List<SparkTreeDTO> children) {
        return new SparkTreeDTO(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                children
        );
    }
}

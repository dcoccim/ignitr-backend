package dev.ignitr.ignitrbackend.spark.mapper;

import dev.ignitr.ignitrbackend.reason.model.ReasonType;
import dev.ignitr.ignitrbackend.spark.dto.*;
import dev.ignitr.ignitrbackend.spark.model.Spark;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SparkMapper {

    private SparkMapper() {}

    public static Spark toNewEntity(CreateSparkRequestDTO dto, Instant now) {
        return new Spark(
                null,
                dto.title(),
                dto.description(),
                null,
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

    public static void partialUpdateEntity(Spark spark, PatchSparkRequestDTO dto, Instant now) {
        if (dto.title() != null) {
            spark.setTitle(dto.title());
        }
        if (dto.description() != null) {
            spark.setDescription(dto.description());
        }
        if (dto.title() != null || dto.description() != null) {
            spark.setUpdatedAt(now);
        }
    }

    public static Spark toNewChildEntity(CreateSparkRequestDTO dto, String parentId, Instant now) {
        return new Spark(
                null,
                dto.title(),
                dto.description(),
                parentId,
                null,
                now,
                now
        );
    }

    public static SparkDTO toSparkDto(Spark entity) {
        return new SparkDTO(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static SparkTreeDTO toSparkTreeDto(List<Spark> sparks, String rootId) {
        Map<String, SparkTreeDTO> map = new HashMap<>(sparks.size());
        for (Spark spark : sparks) {
            int goodReasonsCount = (int) spark.getReasons()
                    .stream()
                    .filter(r -> r.getType() == ReasonType.GOOD)
                    .count();
            int badReasonsCount = (int) spark.getReasons()
                    .stream()
                    .filter(r -> r.getType() == ReasonType.BAD)
                    .count();
            SparkTreeDTO dto = SparkTreeDTO.from(spark, goodReasonsCount, badReasonsCount);
            map.put(spark.getId(), dto);
        }

        for (Spark spark : sparks) {
            String parentId = spark.getParentId();
            if (parentId != null) {
                SparkTreeDTO parentDto = map.get(parentId);
                if (parentDto != null) {
                    parentDto.children().add(map.get(spark.getId()));
                }
            }
        }

        return map.get(rootId);
    }
}

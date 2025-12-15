package dev.ignitr.ignitrbackend.spark.mapper;

import dev.ignitr.ignitrbackend.reason.model.ReasonType;
import dev.ignitr.ignitrbackend.score.tree.ScoredSparkTree;
import dev.ignitr.ignitrbackend.spark.dto.*;
import dev.ignitr.ignitrbackend.spark.model.Spark;
import dev.ignitr.ignitrbackend.spark.tree.SparkTree;

import java.time.Instant;
import java.util.ArrayList;
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

    public static SparkTree toSparkTree(Map<String, Spark> sparkMap, String rootId) {
        Spark rootSpark = sparkMap.get(rootId);
        if (rootSpark == null) {
            return null;
        }

        int goodReasonsCount = 0;
        int badReasonsCount = 0;
        for (var r : rootSpark.getReasons()) {
            if (r.getType() == ReasonType.GOOD) {
                goodReasonsCount++;
            } else if (r.getType() == ReasonType.BAD) {
                badReasonsCount++;
            }
        }

        SparkTree rootNode = SparkTree.fromSpark(rootSpark, goodReasonsCount, badReasonsCount, new ArrayList<>());

        for (Spark spark : sparkMap.values()) {
            String parentId = spark.getParentId();
            if (parentId != null && parentId.equals(rootId)) {
                SparkTree childNode = toSparkTree(sparkMap, spark.getId());
                if (childNode != null) {
                    rootNode.getChildren().add(childNode);
                }
            }
        }

        return rootNode;
    }


    public static SparkTreeDTO toSparkTreeDto(SparkTree sparkTree)  {

        Integer score = null;

        if(sparkTree instanceof ScoredSparkTree){
            score = ((ScoredSparkTree) sparkTree).getScore();
        }

        SparkTreeDTO dto = new SparkTreeDTO(
                sparkTree.getId(),
                sparkTree.getTitle(),
                sparkTree.getDescription(),
                sparkTree.getGoodReasonsCount(),
                sparkTree.getBadReasonsCount(),
                score,
                sparkTree.getCreatedAt(),
                sparkTree.getUpdatedAt(),
                new java.util.ArrayList<>()
        );

        for (SparkTree child : sparkTree.getChildren()) {
            dto.children().add(toSparkTreeDto(child));
        }

        return dto;
    }
}

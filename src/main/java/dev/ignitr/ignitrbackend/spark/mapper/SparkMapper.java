package dev.ignitr.ignitrbackend.spark.mapper;

import dev.ignitr.ignitrbackend.reason.mapper.ReasonMapper;
import dev.ignitr.ignitrbackend.reason.model.ReasonType;
import dev.ignitr.ignitrbackend.spark.dto.*;
import dev.ignitr.ignitrbackend.spark.model.Spark;
import dev.ignitr.ignitrbackend.spark.tree.SparkTree;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SparkMapper {

    private SparkMapper() {}

    public static Spark toNewEntity(String title, String description, Instant now) {
        return new Spark(
                null,
                title,
                description,
                null,
                new ArrayList<>(),
                now,
                now
        );
    }

    public static void updateEntity(Spark spark, String title, String description, Instant now) {
        spark.setTitle(title);
        spark.setDescription(description);
        spark.setUpdatedAt(now);
    }

    public static void partialUpdateEntity(Spark spark, String title, String description, Instant now) {
        boolean updated = false;
        if (title != null) {
            updated = true;
            spark.setTitle(title);
        }
        if (description != null) {
            updated = true;
            spark.setDescription(description);
        }
        if (updated) {
            spark.setUpdatedAt(now);
        }
    }

    public static Spark toNewChildEntity(String title, String description, ObjectId parentId, Instant now) {
        return new Spark(
                null,
                title,
                description,
                parentId,
                new ArrayList<>(),
                now,
                now
        );
    }

    public static SparkDTO toSparkDto(Spark entity, boolean includeReasons) {
        return new SparkDTO(
                entity.getId().toHexString(),
                entity.getTitle(),
                entity.getDescription(),
                includeReasons ? entity.getReasons().stream().map(ReasonMapper::toDto).toList() : List.of(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static List<SparkTree> toSparkTreeList(Map<ObjectId, Spark> sparkMap, List<ObjectId> rootIds) {
        List<SparkTree> sparkTrees = new ArrayList<>();
        for (ObjectId rootId : rootIds) {
            SparkTree sparkTree = toSparkTree(sparkMap, rootId);
            if (sparkTree != null) {
                sparkTrees.add(sparkTree);
            }
        }
        return sparkTrees;
    }

    public static SparkTree toSparkTree(Map<ObjectId, Spark> sparkMap, ObjectId rootId) {
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

        SparkTree rootNode = SparkTree.fromSpark(rootSpark, null, goodReasonsCount, badReasonsCount, new ArrayList<>());

        for (Spark spark : sparkMap.values()) {
            ObjectId parentId = spark.getParentId();
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

        SparkTreeDTO dto = new SparkTreeDTO(
                sparkTree.getId().toHexString(),
                sparkTree.getTitle(),
                sparkTree.getDescription(),
                sparkTree.getGoodReasonsCount(),
                sparkTree.getBadReasonsCount(),
                sparkTree.getScore(),
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

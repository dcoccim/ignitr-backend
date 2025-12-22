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

import static dev.ignitr.ignitrbackend.spark.tree.SparkTreeUtils.computeSubtreeDepth;

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

    public static SparkTreeDTO toSparkTreeDto(SparkTree root, int requestMaxDepth, int maxChildrenPerNode) {
        Map<ObjectId, Integer> subtreeDepth = computeSubtreeDepth(root);
        return toSparkTreeDto(root, requestMaxDepth, maxChildrenPerNode, subtreeDepth);
    }

    public static List<SparkTreeDTO> toSparkTreeDtoList(List<SparkTree> sparkTrees, int requestMaxDepth, int maxChildrenPerNode) {
        Map<ObjectId, Integer> subtreeDepth = computeSubtreeDepth(sparkTrees);
        return sparkTrees.stream().map(
                node -> toSparkTreeDto(node, requestMaxDepth, maxChildrenPerNode, subtreeDepth)
        ).toList();
    }

    private static SparkTreeDTO toSparkTreeDto(
            SparkTree node,
            int requestMaxDepth,
            int maxChildrenPerNode,
            Map<ObjectId, Integer> subtreeDepth
    ) {
        List<SparkTree> children = node.getChildren() != null ? node.getChildren() : List.of();

        int depthBelow = subtreeDepth.getOrDefault(node.getId(), 0);

        SparkTreeDTO dto = new SparkTreeDTO(
                node.getId().toHexString(),
                node.getTitle(),
                node.getDescription(),
                node.getGoodReasonsCount(),
                node.getBadReasonsCount(),
                node.getScore(),
                depthBelow,
                node.getCreatedAt(),
                node.getUpdatedAt(),
                children.size(),
                new ArrayList<>()
        );

        if (requestMaxDepth <= 0 || maxChildrenPerNode <= 0 || children.isEmpty()) {
            return dto;
        }

        int limit = Math.min(maxChildrenPerNode, children.size());
        for (int i = 0; i < limit; i++) {
            dto.children().add(toSparkTreeDto(children.get(i), requestMaxDepth - 1, maxChildrenPerNode, subtreeDepth));
        }

        return dto;
    }
}

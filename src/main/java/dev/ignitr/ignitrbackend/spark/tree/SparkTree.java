package dev.ignitr.ignitrbackend.spark.tree;

import dev.ignitr.ignitrbackend.spark.model.Spark;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class SparkTree {
    private String id;
    private String title;
    private String description;
    private String parentId;
    private int goodReasonsCount;
    private int badReasonsCount;
    private List<SparkTree> children;
    private Instant createdAt;
    private Instant updatedAt;

    public SparkTree(String id, String title, String description, String parentId,
                     int goodReasonsCount, int badReasonsCount, List<SparkTree> children,
                     Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.parentId = parentId;
        this.goodReasonsCount = goodReasonsCount;
        this.badReasonsCount = badReasonsCount;
        this.children = children;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SparkTree fromSpark(Spark spark, int goodReasonsCount, int badReasonsCount, List<SparkTree> children) {
        return new SparkTree(
                spark.getId(),
                spark.getTitle(),
                spark.getDescription(),
                spark.getParentId(),
                goodReasonsCount,
                badReasonsCount,
                children,
                spark.getCreatedAt(),
                spark.getUpdatedAt()
        );
    }
}

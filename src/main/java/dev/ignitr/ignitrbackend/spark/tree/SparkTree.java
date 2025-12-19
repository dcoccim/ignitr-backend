package dev.ignitr.ignitrbackend.spark.tree;

import dev.ignitr.ignitrbackend.spark.model.Spark;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class SparkTree {
    private ObjectId id;
    private String title;
    private String description;
    private ObjectId parentId;
    private Integer score;
    private int goodReasonsCount;
    private int badReasonsCount;
    private List<SparkTree> children;
    private Instant createdAt;
    private Instant updatedAt;

    public SparkTree(ObjectId id, String title, String description, ObjectId parentId, Integer score,
                     int goodReasonsCount, int badReasonsCount, List<SparkTree> children,
                     Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.parentId = parentId;
        this.score = score;
        this.goodReasonsCount = goodReasonsCount;
        this.badReasonsCount = badReasonsCount;
        this.children = children;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SparkTree fromSpark(Spark spark, Integer score, int goodReasonsCount, int badReasonsCount, List<SparkTree> children) {
        return new SparkTree(
                spark.getId(),
                spark.getTitle(),
                spark.getDescription(),
                spark.getParentId(),
                score,
                goodReasonsCount,
                badReasonsCount,
                children,
                spark.getCreatedAt(),
                spark.getUpdatedAt()
        );
    }
}

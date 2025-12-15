package dev.ignitr.ignitrbackend.score.tree;

import dev.ignitr.ignitrbackend.spark.model.Spark;
import dev.ignitr.ignitrbackend.spark.tree.SparkTree;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ScoredSparkTree extends SparkTree {

    private int score;

    public ScoredSparkTree(String id, String title, String description, String parentId,
                           int goodReasonsCount, int badReasonsCount, List<SparkTree> children,
                           Instant createdAt, Instant updatedAt, int score) {
        super(id, title, description, parentId, goodReasonsCount, badReasonsCount, children, createdAt, updatedAt);
        this.score = score;
    }

    public static ScoredSparkTree fromSpark(Spark spark, int goodReasonsCount, int badReasonsCount, List<SparkTree> children, int score) {
        SparkTree node = SparkTree.fromSpark(spark, goodReasonsCount, badReasonsCount, children);
        return new ScoredSparkTree(
                node.getId(),
                node.getTitle(),
                node.getDescription(),
                node.getParentId(),
                node.getGoodReasonsCount(),
                node.getBadReasonsCount(),
                node.getChildren(),
                node.getCreatedAt(),
                node.getUpdatedAt(),
                score
        );
    }
}

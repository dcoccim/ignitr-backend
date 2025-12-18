package dev.ignitr.ignitrbackend.score.service;

import dev.ignitr.ignitrbackend.score.tree.ScoredSparkTree;
import dev.ignitr.ignitrbackend.spark.model.Spark;
import org.bson.types.ObjectId;

import java.util.Map;

public interface SparkScoreService {
    ScoredSparkTree scoreTree(ObjectId rootId, Map<ObjectId, Spark> sparkMap);
}

package dev.ignitr.ignitrbackend.score.service;

import dev.ignitr.ignitrbackend.spark.model.Spark;
import dev.ignitr.ignitrbackend.spark.tree.SparkTree;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

public interface SparkScoreService {
    SparkTree scoreTree(ObjectId rootId, Map<ObjectId, Spark> sparkMap);
    List<SparkTree> scoreTrees(List<ObjectId> rootIds, Map<ObjectId, Spark> sparkMap);
}

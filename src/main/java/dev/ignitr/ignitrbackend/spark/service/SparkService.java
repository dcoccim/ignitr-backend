package dev.ignitr.ignitrbackend.spark.service;

import dev.ignitr.ignitrbackend.spark.exception.SparkNotFoundException;
import dev.ignitr.ignitrbackend.spark.model.Spark;
import dev.ignitr.ignitrbackend.spark.tree.SparkTree;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SparkService {

    Spark saveSpark(Spark spark);

    Spark createSpark(String title, String description);

    Spark createChildSpark(ObjectId parentId, String title, String description);

    Spark getSparkById(ObjectId id) throws SparkNotFoundException;

    List<Spark> getChildren(ObjectId parentId);

    SparkTree getSparkTree(ObjectId rootId);

    Page<SparkTree> getSparkTrees(ObjectId parentId, int page, int size);

    Spark updateSpark(ObjectId id, String title, String description);

    Spark partialUpdateSpark(ObjectId id, String title, String description);

    void deleteSpark(ObjectId id, SparkDeleteMode mode);

    Page<Spark> searchSparks(String title, ParentSearchScope scope, ObjectId parentId, int page, int size);
}

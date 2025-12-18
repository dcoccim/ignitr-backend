package dev.ignitr.ignitrbackend.spark.repository;

import dev.ignitr.ignitrbackend.spark.model.Spark;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SparkRepository extends MongoRepository<Spark, ObjectId> {

    boolean existsByTitle(String title);

    List<Spark> findByParentId(ObjectId parentId);

    Page<Spark> findByParentId(ObjectId parentId, Pageable pageable);

    Page<Spark> findByParentIdIsNull(Pageable pageable);

    Page<Spark> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Spark> findByParentIdAndTitleContainingIgnoreCase(ObjectId parentId, String title, Pageable pageable);

    Page<Spark> findByParentIdIsNullAndTitleContainingIgnoreCase(String title, Pageable pageable);
}
package dev.ignitr.ignitrbackend.spark.repository;

import dev.ignitr.ignitrbackend.spark.model.Spark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SparkRepository extends MongoRepository<Spark, String> {

    boolean existsByTitle(String title);

    List<Spark> findByParentId(String parentId);

    Page<Spark> findByParentId(String parentId, Pageable pageable);

    Page<Spark> findByParentIdIsNull(Pageable pageable);

    Page<Spark> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Spark> findByParentIdAndTitleContainingIgnoreCase(String parentId, String title, Pageable pageable);
}
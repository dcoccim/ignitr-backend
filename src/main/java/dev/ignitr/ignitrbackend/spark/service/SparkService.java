package dev.ignitr.ignitrbackend.spark.service;

import dev.ignitr.ignitrbackend.spark.dto.*;
import dev.ignitr.ignitrbackend.spark.exception.SparkNotFoundException;
import dev.ignitr.ignitrbackend.spark.model.Spark;
import dev.ignitr.ignitrbackend.spark.model.SparkDeleteMode;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SparkService {

    Spark saveSpark(Spark spark);

    Spark createSpark(CreateSparkRequestDTO dto);

    Spark createChildSpark(String parentId, CreateSparkRequestDTO dto);

    Spark getSparkById(String id) throws SparkNotFoundException;

    List<Spark> getChildren(String parentId);

    List<Spark> getSparkTreeList(String rootId);

    Spark updateSpark(String id, UpdateSparkRequestDTO dto);

    Spark partialUpdateSpark(String id, PatchSparkRequestDTO dto);

    void deleteSpark(String id, SparkDeleteMode mode);

    Page<Spark> searchSparks(String title, String parentId, int page, int size);
}

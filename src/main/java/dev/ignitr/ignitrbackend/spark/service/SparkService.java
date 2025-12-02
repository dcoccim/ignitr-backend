package dev.ignitr.ignitrbackend.spark.service;

import dev.ignitr.ignitrbackend.spark.dto.*;
import dev.ignitr.ignitrbackend.spark.model.SparkDeleteMode;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SparkService {

    SparkDTO createSpark(CreateSparkRequestDTO dto);

    SparkDTO createChildSpark(String parentId, CreateSparkRequestDTO dto);

    SparkDTO getSparkById(String id);

    List<SparkDTO> getChildren(String parentId);

    SparkTreeDTO getSparkTree(String rootId);

    SparkDTO updateSpark(String id, UpdateSparkRequestDTO dto);

    SparkDTO partialUpdateSpark(String id, PatchSparkRequestDTO dto);

    void deleteSpark(String id, SparkDeleteMode mode);

    Page<SparkDTO> searchSparks(String title, String parentId, int page, int size);
}

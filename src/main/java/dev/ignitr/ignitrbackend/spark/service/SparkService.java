package dev.ignitr.ignitrbackend.spark.service;

import dev.ignitr.ignitrbackend.spark.dto.CreateSparkRequestDTO;
import dev.ignitr.ignitrbackend.spark.dto.SparkDTO;
import dev.ignitr.ignitrbackend.spark.dto.SparkTreeDTO;
import dev.ignitr.ignitrbackend.spark.dto.UpdateSparkRequestDTO;
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

    void deleteSpark(String id, SparkDeleteMode mode);

    Page<SparkDTO> searchSparks(String title, String parentId, int page, int size);
}

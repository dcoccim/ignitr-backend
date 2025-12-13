package dev.ignitr.ignitrbackend.spark.controller;

import dev.ignitr.ignitrbackend.common.dto.PagedResponse;
import dev.ignitr.ignitrbackend.spark.dto.*;
import dev.ignitr.ignitrbackend.spark.mapper.SparkMapper;
import dev.ignitr.ignitrbackend.spark.model.Spark;
import dev.ignitr.ignitrbackend.spark.model.SparkDeleteMode;
import dev.ignitr.ignitrbackend.spark.service.SparkService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/sparks")
@Validated
public class SparkController {

    private final SparkService sparkService;

    public SparkController(SparkService sparkService) {
        this.sparkService = sparkService;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SparkDTO> create(@Valid @RequestBody CreateSparkRequestDTO request) {
        Spark newSpark = sparkService.createSpark(request);
        SparkDTO response = SparkMapper.toSparkDto(newSpark);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(
            path = "/{parentId}/children",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SparkDTO> createChild(
            @PathVariable String parentId,
            @Valid @RequestBody CreateSparkRequestDTO request
    ) {
        Spark newChildSpark = sparkService.createChildSpark(parentId, request);
        SparkDTO response = SparkMapper.toSparkDto(newChildSpark);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(
            path = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SparkDTO> getById(@PathVariable String id) {
        Spark spark = sparkService.getSparkById(id);
        SparkDTO response = SparkMapper.toSparkDto(spark);
        return ResponseEntity.ok(response);
    }

    @GetMapping(
            path="/{id}/children",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<SparkDTO>> getChildren(@PathVariable String id) {
        List<Spark> children = sparkService.getChildren(id);
        List<SparkDTO> response = children.stream()
                .map(SparkMapper::toSparkDto)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping(
            path = "/{id}/tree",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SparkTreeDTO> getSparkTree(@PathVariable String id) {
        List<Spark> sparkTreeList = sparkService.getSparkTreeList(id);
        SparkTreeDTO response = SparkMapper.toSparkTreeDto(sparkTreeList, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping(
            path = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SparkDTO> updateSpark(
            @PathVariable String id,
            @Valid @RequestBody UpdateSparkRequestDTO request
    ) {
        Spark updatedSpark = sparkService.updateSpark(id, request);
        SparkDTO response = SparkMapper.toSparkDto(updatedSpark);
        return ResponseEntity.ok(response);
    }

    @PatchMapping (
            path = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SparkDTO> patchSpark(
            @PathVariable String id,
            @Valid @RequestBody PatchSparkRequestDTO request
    ) {
        Spark updatedSpark = sparkService.partialUpdateSpark(id, request);
        SparkDTO response = SparkMapper.toSparkDto(updatedSpark);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpark(
            @PathVariable String id,
            @RequestParam(name = "mode", defaultValue = "CASCADE")SparkDeleteMode mode
            ) {
        sparkService.deleteSpark(id, mode);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResponse<SparkDTO>> searchSparks(
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "parentId", required = false) String parentId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        Page<Spark> sparksPage = sparkService.searchSparks(title, parentId, page, size);
        Page<SparkDTO> response = sparksPage.map(SparkMapper::toSparkDto);
        PagedResponse<SparkDTO> pagedResponse = new PagedResponse<>(
                response.getContent(),
                response.getNumber(),
                response.getSize(),
                response.getTotalElements(),
                response.getTotalPages()
        );
        return ResponseEntity.ok(pagedResponse);
    }
}

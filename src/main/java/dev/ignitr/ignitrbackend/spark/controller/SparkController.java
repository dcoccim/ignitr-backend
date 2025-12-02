package dev.ignitr.ignitrbackend.spark.controller;

import dev.ignitr.ignitrbackend.spark.dto.*;
import dev.ignitr.ignitrbackend.spark.model.SparkDeleteMode;
import dev.ignitr.ignitrbackend.spark.service.SparkService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/sparks")
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
        SparkDTO response = sparkService.createSpark(request);
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
        SparkDTO response = sparkService.createChildSpark(parentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(
            path = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SparkDTO> getById(@PathVariable String id) {
        SparkDTO response = sparkService.getSparkById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping(
            path="/{id}/children",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<SparkDTO>> getChildren(@PathVariable String id) {
        List<SparkDTO> response = sparkService.getChildren(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping(
            path = "/{id}/tree",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SparkTreeDTO> getSparkTree(@PathVariable String id) {
        SparkTreeDTO response = sparkService.getSparkTree(id);
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
        SparkDTO response = sparkService.updateSpark(id, request);
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
        SparkDTO response = sparkService.partialUpdateSpark(id, request);
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
    public ResponseEntity<List<SparkDTO>> searchSparks(
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "parentId", required = false) String parentId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        List<SparkDTO> response = sparkService.searchSparks(title, parentId, page, size).getContent();
        return ResponseEntity.ok(response);
    }
}

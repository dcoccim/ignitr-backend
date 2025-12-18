package dev.ignitr.ignitrbackend.spark.controller;

import dev.ignitr.ignitrbackend.common.dto.PagedResponse;
import dev.ignitr.ignitrbackend.spark.dto.*;
import dev.ignitr.ignitrbackend.spark.mapper.SparkMapper;
import dev.ignitr.ignitrbackend.spark.model.Spark;
import dev.ignitr.ignitrbackend.spark.service.ParentSearchScope;
import dev.ignitr.ignitrbackend.spark.service.SparkDeleteMode;
import dev.ignitr.ignitrbackend.spark.service.SparkService;

import dev.ignitr.ignitrbackend.spark.tree.SparkTree;
import jakarta.validation.Valid;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static dev.ignitr.ignitrbackend.common.utils.StringUtils.isInvalidObjectId;


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
        Spark newSpark = sparkService.createSpark(request.title(), request.description());
        SparkDTO response = SparkMapper.toSparkDto(newSpark, false);
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
        if(isInvalidObjectId(parentId)) {
            throw new IllegalArgumentException("Invalid parent ID format.");
        }
        Spark newChildSpark = sparkService.createChildSpark(new ObjectId(parentId), request.title(), request.description());
        SparkDTO response = SparkMapper.toSparkDto(newChildSpark, false);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(
            path = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SparkDTO> getById(
            @PathVariable String id,
            @RequestParam (name = "includeReasons", defaultValue = "false") boolean includeReasons
    ) {
        if(isInvalidObjectId(id)) {
            throw new IllegalArgumentException("Invalid spark ID format.");
        }
        Spark spark = sparkService.getSparkById(new ObjectId(id));
        SparkDTO response = SparkMapper.toSparkDto(spark, includeReasons);
        return ResponseEntity.ok(response);
    }

    @GetMapping(
            path="/{id}/children",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<SparkDTO>> getChildren(
            @PathVariable String id,
            @RequestParam (name = "includeReasons", defaultValue = "false") boolean includeReasons
    ) {
        if(isInvalidObjectId(id)) {
            throw new IllegalArgumentException("Invalid spark ID format.");
        }
        List<Spark> children = sparkService.getChildren(new ObjectId(id));
        List<SparkDTO> response = children.stream()
                .map((s) -> SparkMapper.toSparkDto(s, includeReasons))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping(
            path = "/{id}/tree",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SparkTreeDTO> getSparkTree(@PathVariable String id) {
        if(isInvalidObjectId(id)) {
            throw new IllegalArgumentException("Invalid spark ID format.");
        }
        SparkTree sparkTree = sparkService.getSparkTree(new ObjectId(id));
        SparkTreeDTO response = SparkMapper.toSparkTreeDto(sparkTree);
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
        if(isInvalidObjectId(id)) {
            throw new IllegalArgumentException("Invalid spark ID format.");
        }
        Spark updatedSpark = sparkService.updateSpark(new ObjectId(id), request.title(), request.description());
        SparkDTO response = SparkMapper.toSparkDto(updatedSpark, false);
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
        if(isInvalidObjectId(id)) {
            throw new IllegalArgumentException("Invalid spark ID format.");
        }
        Spark updatedSpark = sparkService.partialUpdateSpark(new ObjectId(id), request.title(), request.description());
        SparkDTO response = SparkMapper.toSparkDto(updatedSpark, false);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpark(
            @PathVariable String id,
            @RequestParam(name = "mode")String mode
            ) {
        if(isInvalidObjectId(id)) {
            throw new IllegalArgumentException("Invalid spark ID format.");
        }
        SparkDeleteMode deleteMode = SparkDeleteMode.fromValue(mode);
        sparkService.deleteSpark(new ObjectId(id), deleteMode);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResponse<SparkDTO>> searchSparks(
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "parentId", required = false) String parentId,
            @RequestParam(name = "includeReasons", defaultValue = "false") boolean includeReasons,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        ParentSearchScope scope;
        ObjectId parentObjectId = null;

        if (parentId == null || parentId.isBlank()) {
            scope = ParentSearchScope.ANY;
        } else if ("root".equalsIgnoreCase(parentId)) {
            scope = ParentSearchScope.ROOT;
        } else {
            if (isInvalidObjectId(parentId)) {
                throw new IllegalArgumentException("Invalid parentId. Must be 'root' or a valid ObjectId.");
            }
            scope = ParentSearchScope.ID;
            parentObjectId = new ObjectId(parentId);
        }
        Page<Spark> sparksPage = sparkService.searchSparks(title, scope, parentObjectId , page, size);
        Page<SparkDTO> response = sparksPage.map((s) -> SparkMapper.toSparkDto(s, includeReasons));
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

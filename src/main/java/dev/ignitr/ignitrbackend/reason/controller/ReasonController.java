package dev.ignitr.ignitrbackend.reason.controller;

import dev.ignitr.ignitrbackend.common.dto.PagedResponse;
import dev.ignitr.ignitrbackend.reason.dto.CreateReasonRequestDTO;
import dev.ignitr.ignitrbackend.reason.dto.ReasonDTO;
import dev.ignitr.ignitrbackend.reason.dto.UpdateReasonRequestDTO;
import dev.ignitr.ignitrbackend.reason.mapper.ReasonMapper;
import dev.ignitr.ignitrbackend.reason.model.Reason;
import dev.ignitr.ignitrbackend.reason.model.ReasonType;
import dev.ignitr.ignitrbackend.reason.service.ReasonService;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static dev.ignitr.ignitrbackend.common.utils.StringUtils.isInvalidObjectId;

@RestController
@RequestMapping("/sparks/{sparkId}/reasons")
@Validated
public class ReasonController {

    private final ReasonService reasonService;

    public ReasonController(ReasonService reasonService) {
        this.reasonService = reasonService;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ReasonDTO> createReason(@PathVariable String sparkId, @Valid @RequestBody CreateReasonRequestDTO dto) {
        if(isInvalidObjectId(sparkId)) {
            throw new IllegalArgumentException("Invalid spark ID format.");
        }
        Reason newReason = reasonService.createReason(new ObjectId(sparkId), dto.content(), dto.type());
        ReasonDTO response = ReasonMapper.toDto(newReason);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping(
            path = "/{reasonId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ReasonDTO> getReasonById(@PathVariable String sparkId, @PathVariable String reasonId) {
        if(isInvalidObjectId(sparkId)) {
            throw new IllegalArgumentException("Invalid spark ID format.");
        }
        if(isInvalidObjectId(reasonId)) {
            throw new IllegalArgumentException("Invalid reason ID format.");
        }
        Reason reason = reasonService.getReasonById(new ObjectId(sparkId), new ObjectId(reasonId));
        ReasonDTO response = ReasonMapper.toDto(reason);
        return ResponseEntity.ok(response);
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PagedResponse<ReasonDTO>> getReasonsBySparkId(
            @PathVariable String sparkId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if(isInvalidObjectId(sparkId)) {
            throw new IllegalArgumentException("Invalid spark ID format.");
        }
        ReasonType reasonType = type != null ? ReasonType.fromValue(type) : null;
        Page<Reason> reasonsPage = reasonService.getReasonsBySparkId(new ObjectId(sparkId), reasonType, page, size);
        Page<ReasonDTO> dtoPage = reasonsPage.map(ReasonMapper::toDto);
        PagedResponse<ReasonDTO> response = new PagedResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping(
            path = "/{reasonId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ReasonDTO> updateReason(
            @PathVariable String sparkId,
            @PathVariable String reasonId,
            @Valid @RequestBody UpdateReasonRequestDTO dto
    ) {
        if(isInvalidObjectId(sparkId)) {
            throw new IllegalArgumentException("Invalid spark ID format.");
        }
        if(isInvalidObjectId(reasonId)) {
            throw new IllegalArgumentException("Invalid reason ID format.");
        }
        Reason updatedReason = reasonService.updateReason(new ObjectId(sparkId), new ObjectId(reasonId), dto.content(), dto.type());
        ReasonDTO response = ReasonMapper.toDto(updatedReason);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reasonId}")
    public ResponseEntity<Void> deleteReason(
            @PathVariable String sparkId,
            @PathVariable String reasonId
    ) {
        if(isInvalidObjectId(sparkId)) {
            throw new IllegalArgumentException("Invalid spark ID format.");
        }
        if(isInvalidObjectId(reasonId)) {
            throw new IllegalArgumentException("Invalid reason ID format.");
        }
        reasonService.deleteReason(new ObjectId(sparkId), new ObjectId(reasonId));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping()
    public ResponseEntity<Void> deleteAllReasonsBySparkId(
            @PathVariable String sparkId
    ) {
        if(isInvalidObjectId(sparkId)) {
            throw new IllegalArgumentException("Invalid spark ID format.");
        }
        reasonService.deleteAllReasonsBySparkId(new ObjectId(sparkId));
        return ResponseEntity.noContent().build();
    }
}

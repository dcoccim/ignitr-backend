package dev.ignitr.ignitrbackend.reason.controller;

import dev.ignitr.ignitrbackend.common.dto.PagedResponse;
import dev.ignitr.ignitrbackend.reason.dto.CreateReasonRequestDTO;
import dev.ignitr.ignitrbackend.reason.dto.ReasonDTO;
import dev.ignitr.ignitrbackend.reason.dto.UpdateReasonRequestDTO;
import dev.ignitr.ignitrbackend.reason.mapper.ReasonMapper;
import dev.ignitr.ignitrbackend.reason.model.Reason;
import dev.ignitr.ignitrbackend.reason.service.ReasonService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
        Reason newReason = reasonService.createReason(sparkId, dto);
        ReasonDTO response = ReasonMapper.toDto(newReason);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping(
            path = "/{reasonId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ReasonDTO> getReasonById(@PathVariable String sparkId, @PathVariable String reasonId) {
        Reason reason = reasonService.getReasonById(sparkId, reasonId);
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
        Page<Reason> reasonsPage = reasonService.getReasonsBySparkId(sparkId, type, page, size);
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
        Reason updatedReason = reasonService.updateReason(sparkId, reasonId, dto);
        ReasonDTO response = ReasonMapper.toDto(updatedReason);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reasonId}")
    public ResponseEntity<Void> deleteReason(
            @PathVariable String sparkId,
            @PathVariable String reasonId
    ) {
        reasonService.deleteReason(sparkId, reasonId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping()
    public ResponseEntity<Void> deleteAllReasonsBySparkId(
            @PathVariable String sparkId
    ) {
        reasonService.deleteAllReasonsBySparkId(sparkId);
        return ResponseEntity.noContent().build();
    }
}

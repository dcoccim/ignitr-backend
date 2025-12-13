package dev.ignitr.ignitrbackend.reason.service;

import dev.ignitr.ignitrbackend.reason.dto.CreateReasonRequestDTO;
import dev.ignitr.ignitrbackend.reason.dto.UpdateReasonRequestDTO;
import dev.ignitr.ignitrbackend.reason.model.Reason;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface ReasonService {

    Reason createReason(String sparkId, CreateReasonRequestDTO dto);

    Reason getReasonById(String sparkId, String reasonId);

    Page<Reason> getReasonsBySparkId(String sparkId, String type, int page, int size);

    Reason updateReason(String sparkId, String reasonId, UpdateReasonRequestDTO dto);

    void deleteReason(String sparkId, String reasonId);

    void deleteAllReasonsBySparkId(String sparkId);
}

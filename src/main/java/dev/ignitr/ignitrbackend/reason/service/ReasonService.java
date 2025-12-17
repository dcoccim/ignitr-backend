package dev.ignitr.ignitrbackend.reason.service;

import dev.ignitr.ignitrbackend.reason.model.Reason;
import dev.ignitr.ignitrbackend.reason.model.ReasonType;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface ReasonService {

    Reason createReason(String sparkId, String content, ReasonType type);

    Reason getReasonById(String sparkId, ObjectId reasonId);

    Page<Reason> getReasonsBySparkId(String sparkId, ReasonType type, int page, int size);

    Reason updateReason(String sparkId, ObjectId reasonId, String content, ReasonType type);

    void deleteReason(String sparkId, ObjectId reasonId);

    void deleteAllReasonsBySparkId(String sparkId);
}

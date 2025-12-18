package dev.ignitr.ignitrbackend.reason.service;

import dev.ignitr.ignitrbackend.reason.model.Reason;
import dev.ignitr.ignitrbackend.reason.model.ReasonType;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface ReasonService {

    Reason createReason(ObjectId sparkId, String content, ReasonType type);

    Reason getReasonById(ObjectId sparkId, ObjectId reasonId);

    Page<Reason> getReasonsBySparkId(ObjectId sparkId, ReasonType type, int page, int size);

    Reason updateReason(ObjectId sparkId, ObjectId reasonId, String content, ReasonType type);

    void deleteReason(ObjectId sparkId, ObjectId reasonId);

    void deleteAllReasonsBySparkId(ObjectId sparkId);
}

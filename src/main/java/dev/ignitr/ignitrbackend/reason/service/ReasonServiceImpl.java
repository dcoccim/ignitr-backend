package dev.ignitr.ignitrbackend.reason.service;

import dev.ignitr.ignitrbackend.reason.dto.CreateReasonRequestDTO;
import dev.ignitr.ignitrbackend.reason.dto.UpdateReasonRequestDTO;
import dev.ignitr.ignitrbackend.reason.exception.ReasonAlreadyExistsException;
import dev.ignitr.ignitrbackend.reason.exception.ReasonNotFoundException;
import dev.ignitr.ignitrbackend.reason.mapper.ReasonMapper;
import dev.ignitr.ignitrbackend.reason.model.Reason;
import dev.ignitr.ignitrbackend.spark.model.Spark;
import dev.ignitr.ignitrbackend.spark.service.SparkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static dev.ignitr.ignitrbackend.common.utils.LoggingUtils.debug;
import static dev.ignitr.ignitrbackend.common.utils.LoggingUtils.info;
import static dev.ignitr.ignitrbackend.common.utils.LoggingUtils.warn;

@Service
public class ReasonServiceImpl implements ReasonService {

    private static final Logger logger = LoggerFactory.getLogger(ReasonServiceImpl.class);

    private final SparkService sparkService;

    public ReasonServiceImpl(SparkService sparkService) {
        this.sparkService = sparkService;
    }

    private void checkUniqueReasonContent(Spark spark, String content, String operation) {
        boolean exists = spark.getReasons().stream()
                .anyMatch(reason -> reason.getContent().equalsIgnoreCase(content));
        if (exists) {
            warn(logger, operation, spark.getId(),
                    "Reason with content '{}' already exists for Spark '{}'.", content, spark.getId());
            throw new ReasonAlreadyExistsException(content);
        }
    }

    @Override
    public Reason createReason(String sparkId, CreateReasonRequestDTO dto) {

        debug(logger, "createReason", sparkId, "Creating Reason...");

        Instant now = Instant.now();
        Reason newReason = ReasonMapper.toNewEntity(dto, now);

        Spark existingSpark = sparkService.getSparkById(sparkId);

        checkUniqueReasonContent(existingSpark, newReason.getContent(), "createReason");

        existingSpark.getReasons().add(newReason);

        Spark savedSpark = sparkService.saveSpark(existingSpark);

        Reason savedReason = savedSpark.getReasons().getLast();

        info(logger, "createReason", savedReason.getId(), "Reason created successfully.");

        return savedReason;
    }

    @Override
    public Reason getReasonById(String sparkId, String reasonId) {
        debug(logger, "getReasonById", reasonId, "Fetching Reason by ID...");
        Spark spark = sparkService.getSparkById(sparkId);
        Reason reason = spark.getReasons().stream()
                .filter(r -> r.getId().equals(reasonId))
                .findFirst()
                .orElseThrow(() -> {
                    ReasonNotFoundException exception = new ReasonNotFoundException(reasonId);
                    warn(logger, "getReasonById", reasonId, "Reason not found.", exception);
                    return exception;
                });
        info(logger, "getReasonById", reasonId, "Reason fetched successfully.");
        return reason;
    }

    @Override
    public Page<Reason> getReasonsBySparkId(String sparkId, String type, int page, int size) {
        debug(logger, "getReasonsBySparkId", sparkId,
                "Fetching {} Reasons for spark...", type != null ? type : "all");
        Spark existingSpark = sparkService.getSparkById(sparkId);
        Page<Reason> reasonsPage = ReasonMapper.filterAndPaginateReasons(existingSpark.getReasons(), type, page, size);
        info(logger, "getReasonsBySparkId", sparkId,
                "Fetched {} Reasons for Spark successfully.", reasonsPage.getTotalElements());
        return reasonsPage;
    }

    @Override
    public Reason updateReason(String sparkId, String reasonId, UpdateReasonRequestDTO dto) {
        debug(logger, "updateReason", reasonId, "Updating Reason...");

        Spark existingSpark = sparkService.getSparkById(sparkId);

        checkUniqueReasonContent(existingSpark, dto.content(), "updateReason");

        Reason reason = existingSpark.getReasons().stream()
                .filter(r -> r.getId().equals(reasonId))
                .findFirst()
                .orElseThrow(() -> {
                    ReasonNotFoundException exception = new ReasonNotFoundException(reasonId);
                    warn(logger, "updateReason", reasonId, "Reason not found.", exception);
                    return exception;
                });

        Instant now = Instant.now();
        ReasonMapper.updateEntity(dto, reason, now);

        Spark savedSpark = sparkService.saveSpark(existingSpark);

        Reason updatedReason = savedSpark.getReasons().stream()
                .filter(r -> r.getId().equals(reasonId))
                .findFirst()
                .orElseThrow(() -> {
                    ReasonNotFoundException exception = new ReasonNotFoundException(reasonId);
                    warn(logger, "updateReason", reasonId, "Reason not found after update.", exception);
                    return exception;
                });

        info(logger, "updateReason", reasonId, "Reason updated successfully.");

        return updatedReason;
    }

    @Override
    public void deleteReason(String sparkId, String reasonId) {

        debug(logger, "deleteReason", reasonId, "Deleting Reason...");

        Spark existingSpark = sparkService.getSparkById(sparkId);

        boolean removed = existingSpark.getReasons().removeIf(r -> r.getId().equals(reasonId));

        if (!removed) {
            ReasonNotFoundException exception = new ReasonNotFoundException(reasonId);
            warn(logger, "deleteReason", reasonId, "Reason not found.", exception);
            throw exception;
        }

        sparkService.saveSpark(existingSpark);

        info(logger, "deleteReason", reasonId, "Reason deleted successfully.");
    }

    @Override
    public void deleteAllReasonsBySparkId(String sparkId) {
        debug(logger, "deleteAllReasonsBySparkId", sparkId, "Deleting all Reasons for Spark...");

        Spark existingSpark = sparkService.getSparkById(sparkId);

        existingSpark.getReasons().clear();

        sparkService.saveSpark(existingSpark);

        info(logger, "deleteAllReasonsBySparkId", sparkId, "All Reasons for Spark deleted successfully.");
    }
}

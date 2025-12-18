package dev.ignitr.ignitrbackend.reason.service;

import dev.ignitr.ignitrbackend.reason.exception.ReasonAlreadyExistsException;
import dev.ignitr.ignitrbackend.reason.exception.ReasonNotFoundException;
import dev.ignitr.ignitrbackend.reason.model.Reason;
import dev.ignitr.ignitrbackend.reason.model.ReasonType;
import dev.ignitr.ignitrbackend.spark.model.Spark;
import dev.ignitr.ignitrbackend.spark.service.SparkService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReasonServiceImplTest {

    @Mock
    private SparkService sparkService;

    private ReasonService reasonService;

    @BeforeEach
    void setUp() {
        reasonService = new ReasonServiceImpl(sparkService);
    }

    private Spark buildSpark(ObjectId id) {
        Spark spark = new Spark();
        spark.setId(id);
        spark.setTitle("Title " + id.toHexString());
        spark.setDescription("Description " + id.toHexString());
        spark.setReasons(new ArrayList<>());
        spark.setCreatedAt(Instant.now());
        spark.setUpdatedAt(Instant.now());
        return spark;
    }

    private Reason buildReason(ObjectId id, ReasonType type, String content) {
        Reason reason = new Reason();
        reason.setId(id);
        reason.setType(type);
        reason.setContent(content);
        reason.setCreatedAt(Instant.now());
        reason.setUpdatedAt(Instant.now());
        return reason;
    }

    @Test
    void createReason_returnsSavedReason_whenSparkExists() {

        ObjectId sparkId = new ObjectId();
        String content = "Great work";
        ReasonType type = ReasonType.GOOD;

        Spark spark = buildSpark(sparkId);

        when(sparkService.getSparkById(sparkId)).thenReturn(spark);
        when(sparkService.saveSpark(any(Spark.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reason result = reasonService.createReason(sparkId, content, type);

        assertThat(result.getContent()).isEqualTo(content);
        assertThat(result.getType()).isEqualTo(type);
        assertThat(spark.getReasons()).hasSize(1);

        verify(sparkService).getSparkById(sparkId);
        verify(sparkService).saveSpark(spark);
    }

    @Test
    void getReasonById_returnsReason_whenFound() {

        ObjectId sparkId = new ObjectId();
        ObjectId reasonId = new ObjectId();
        Reason reason = buildReason(reasonId, ReasonType.GOOD, "Content");

        Spark spark = buildSpark(sparkId);
        spark.getReasons().add(reason);

        when(sparkService.getSparkById(sparkId)).thenReturn(spark);

        Reason result = reasonService.getReasonById(sparkId, reasonId);

        assertThat(result).isEqualTo(reason);
        verify(sparkService).getSparkById(sparkId);
    }

    @Test
    void getReasonById_throwsWhenReasonMissing() {

        ObjectId sparkId = new ObjectId();
        Spark spark = buildSpark(sparkId);

        when(sparkService.getSparkById(sparkId)).thenReturn(spark);

        ObjectId missingReasonId = new ObjectId("000000000000000000000001");

        assertThatThrownBy(() -> reasonService.getReasonById(sparkId, missingReasonId))
                .isInstanceOf(ReasonNotFoundException.class)
                .hasMessageContaining(missingReasonId.toHexString());

        verify(sparkService).getSparkById(sparkId);
    }

    @Test
    void getReasonsBySparkId_filtersAndPaginates() {

        ObjectId sparkId = new ObjectId();
        Reason goodReason1 = buildReason(new ObjectId(), ReasonType.GOOD, "Nice");
        Reason goodReason2 = buildReason(new ObjectId(), ReasonType.GOOD, "Awesome");
        Reason badReason1 = buildReason(new ObjectId(), ReasonType.BAD, "Needs work");

        Spark spark = buildSpark(sparkId);
        spark.getReasons().addAll(List.of(
                goodReason1,
                goodReason2,
                badReason1
        ));

        when(sparkService.getSparkById(sparkId)).thenReturn(spark);

        var page = reasonService.getReasonsBySparkId(sparkId, ReasonType.GOOD, 0, 2);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).containsExactly(goodReason1, goodReason2);

        verify(sparkService).getSparkById(sparkId);
    }

    @Test
    void updateReason_updatesReason_whenPresent() {

        ObjectId sparkId = new ObjectId();
        ObjectId reasonId = new ObjectId();
        Reason existing = buildReason(reasonId, ReasonType.BAD, "Old");

        Spark spark = buildSpark(sparkId);
        spark.getReasons().add(existing);

        String newContent = "New content";
        ReasonType newType = ReasonType.GOOD;

        when(sparkService.getSparkById(sparkId)).thenReturn(spark);
        when(sparkService.saveSpark(any(Spark.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reason result = reasonService.updateReason(sparkId, reasonId, newContent, newType);

        assertThat(result.getContent()).isEqualTo(newContent);
        assertThat(result.getType()).isEqualTo(newType);
        verify(sparkService).saveSpark(spark);
    }

    @Test
    void updateReason_throwsWhenMissing() {

        ObjectId sparkId = new ObjectId();
        Spark spark = buildSpark(sparkId);

        when(sparkService.getSparkById(sparkId)).thenReturn(spark);

        ObjectId missingReasonId = new ObjectId("000000000000000000000002");

        assertThatThrownBy(() -> reasonService.updateReason(sparkId, missingReasonId, "Content", ReasonType.GOOD))
                .isInstanceOf(ReasonNotFoundException.class)
                .hasMessageContaining(missingReasonId.toHexString());

        verify(sparkService, never()).saveSpark(any(Spark.class));
    }

    @Test
    void deleteReason_removesReason_whenPresent() {

        ObjectId sparkId = new ObjectId();
        ObjectId reasonId = new ObjectId();
        Reason reason = buildReason(reasonId, ReasonType.BAD, "Content");

        Spark spark = buildSpark(sparkId);
        spark.getReasons().add(reason);

        when(sparkService.getSparkById(sparkId)).thenReturn(spark);

        reasonService.deleteReason(sparkId, reasonId);

        assertThat(spark.getReasons()).isEmpty();
        verify(sparkService).saveSpark(spark);
    }

    @Test
    void deleteReason_throwsWhenMissing() {

        ObjectId sparkId = new ObjectId();
        Spark spark = buildSpark(sparkId);

        when(sparkService.getSparkById(sparkId)).thenReturn(spark);

        ObjectId missingReasonId = new ObjectId("000000000000000000000003");

        assertThatThrownBy(() -> reasonService.deleteReason(sparkId, missingReasonId))
                .isInstanceOf(ReasonNotFoundException.class)
                .hasMessageContaining(missingReasonId.toHexString());

        verify(sparkService, never()).saveSpark(any(Spark.class));
    }

    @Test
    void deleteAllReasonsBySparkId_clearsReasons() {

        ObjectId sparkId = new ObjectId();
        Spark spark = buildSpark(sparkId);
        spark.getReasons().addAll(List.of(
                buildReason(new ObjectId(), ReasonType.GOOD, "Nice"),
                buildReason(new ObjectId(), ReasonType.BAD, "Bad")
        ));

        when(sparkService.getSparkById(sparkId)).thenReturn(spark);

        reasonService.deleteAllReasonsBySparkId(sparkId);

        assertThat(spark.getReasons()).isEmpty();
        verify(sparkService).saveSpark(spark);
    }

    @Test
    void createReason_throwsWhenContentAlreadyExists() {

        ObjectId sparkId = new ObjectId();
        Reason existing = buildReason(new ObjectId(), ReasonType.GOOD, "Duplicate");

        Spark spark = buildSpark(sparkId);
        spark.getReasons().add(existing);

        when(sparkService.getSparkById(sparkId)).thenReturn(spark);

        String duplicateContent = "Duplicate";
        ReasonType badType = ReasonType.BAD;

        assertThatThrownBy(() -> reasonService.createReason(sparkId, duplicateContent, badType))
                .isInstanceOf(ReasonAlreadyExistsException.class)
                .hasMessageContaining("Duplicate");

        verify(sparkService, never()).saveSpark(any(Spark.class));
    }

    @Test
    void updateReason_throwsWhenContentAlreadyExists() {

        ObjectId sparkId = new ObjectId();
        ObjectId reason1Id = new ObjectId();
        ObjectId reason2Id = new ObjectId();

        String duplicateContent = "Conflict";

        Reason existing = buildReason(reason1Id, ReasonType.GOOD, "Original");
        Reason duplicate = buildReason(reason2Id, ReasonType.BAD, duplicateContent);

        Spark spark = buildSpark(sparkId);
        spark.getReasons().addAll(List.of(existing, duplicate));

        when(sparkService.getSparkById(sparkId)).thenReturn(spark);

        ReasonType badType = ReasonType.BAD;

        assertThatThrownBy(() -> reasonService.updateReason(sparkId, reason1Id, duplicateContent, badType))
                .isInstanceOf(ReasonAlreadyExistsException.class)
                .hasMessageContaining("Conflict");

        verify(sparkService, never()).saveSpark(any(Spark.class));
    }
}

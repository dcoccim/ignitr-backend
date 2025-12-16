package dev.ignitr.ignitrbackend.reason.service;

import dev.ignitr.ignitrbackend.reason.dto.CreateReasonRequestDTO;
import dev.ignitr.ignitrbackend.reason.dto.UpdateReasonRequestDTO;
import dev.ignitr.ignitrbackend.reason.exception.ReasonAlreadyExistsException;
import dev.ignitr.ignitrbackend.reason.exception.ReasonNotFoundException;
import dev.ignitr.ignitrbackend.reason.model.Reason;
import dev.ignitr.ignitrbackend.reason.model.ReasonType;
import dev.ignitr.ignitrbackend.spark.model.Spark;
import dev.ignitr.ignitrbackend.spark.service.SparkService;
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

    @Test
    void createReason_returnsSavedReason_whenSparkExists() {

        String sparkId = "spark-1";
        CreateReasonRequestDTO dto = new CreateReasonRequestDTO("Great work", ReasonType.GOOD);

        Spark spark = buildSpark(sparkId);

        when(sparkService.getSparkById(sparkId)).thenReturn(spark);
        when(sparkService.saveSpark(any(Spark.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reason result = reasonService.createReason(sparkId, dto);

        assertThat(result.getContent()).isEqualTo(dto.content());
        assertThat(result.getType()).isEqualTo(dto.type());
        assertThat(spark.getReasons()).hasSize(1);

        verify(sparkService).getSparkById(sparkId);
        verify(sparkService).saveSpark(spark);
    }

    @Test
    void getReasonById_returnsReason_whenFound() {

        String sparkId = "spark-1";
        Reason reason = buildReason("reason-1", ReasonType.GOOD, "Content");

        Spark spark = buildSpark(sparkId);
        spark.getReasons().add(reason);

        when(sparkService.getSparkById(sparkId)).thenReturn(spark);

        Reason result = reasonService.getReasonById(sparkId, reason.getId());

        assertThat(result).isEqualTo(reason);
        verify(sparkService).getSparkById(sparkId);
    }

    @Test
    void getReasonById_throwsWhenReasonMissing() {

        String sparkId = "spark-1";
        Spark spark = buildSpark(sparkId);

        when(sparkService.getSparkById(sparkId)).thenReturn(spark);

        assertThatThrownBy(() -> reasonService.getReasonById(sparkId, "missing"))
                .isInstanceOf(ReasonNotFoundException.class)
                .hasMessageContaining("missing");

        verify(sparkService).getSparkById(sparkId);
    }

    @Test
    void getReasonsBySparkId_filtersAndPaginates() {

        String sparkId = "spark-1";
        Reason r1 = buildReason("r1", ReasonType.GOOD, "Nice");
        Reason r2 = buildReason("r2", ReasonType.BAD, "Needs work");
        Reason r3 = buildReason("r3", ReasonType.GOOD, "Awesome");

        Spark spark = buildSpark(sparkId);
        spark.getReasons().addAll(List.of(r1, r2, r3));

        when(sparkService.getSparkById(sparkId)).thenReturn(spark);

        var page = reasonService.getReasonsBySparkId(sparkId, "GOOD", 0, 2);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).containsExactly(r1, r3);

        verify(sparkService).getSparkById(sparkId);
    }

    @Test
    void updateReason_updatesReason_whenPresent() {

        String sparkId = "spark-1";
        String reasonId = "reason-1";
        Reason existing = buildReason(reasonId, ReasonType.BAD, "Old");

        Spark spark = buildSpark(sparkId);
        spark.getReasons().add(existing);

        UpdateReasonRequestDTO dto = new UpdateReasonRequestDTO("New content", ReasonType.GOOD);

        when(sparkService.getSparkById(sparkId)).thenReturn(spark);
        when(sparkService.saveSpark(any(Spark.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reason result = reasonService.updateReason(sparkId, reasonId, dto);

        assertThat(result.getContent()).isEqualTo(dto.content());
        assertThat(result.getType()).isEqualTo(dto.type());
        verify(sparkService).saveSpark(spark);
    }

    @Test
    void updateReason_throwsWhenMissing() {

        String sparkId = "spark-1";
        Spark spark = buildSpark(sparkId);

        when(sparkService.getSparkById(sparkId)).thenReturn(spark);

        UpdateReasonRequestDTO dto = new UpdateReasonRequestDTO("Content", ReasonType.GOOD);

        assertThatThrownBy(() -> reasonService.updateReason(sparkId, "missing", dto))
                .isInstanceOf(ReasonNotFoundException.class)
                .hasMessageContaining("missing");

        verify(sparkService, never()).saveSpark(any(Spark.class));
    }

    @Test
    void deleteReason_removesReason_whenPresent() {

        String sparkId = "spark-1";
        String reasonId = "reason-1";
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

        String sparkId = "spark-1";
        Spark spark = buildSpark(sparkId);

        when(sparkService.getSparkById(sparkId)).thenReturn(spark);

        assertThatThrownBy(() -> reasonService.deleteReason(sparkId, "missing"))
                .isInstanceOf(ReasonNotFoundException.class)
                .hasMessageContaining("missing");

        verify(sparkService, never()).saveSpark(any(Spark.class));
    }

    @Test
    void deleteAllReasonsBySparkId_clearsReasons() {

        String sparkId = "spark-1";
        Spark spark = buildSpark(sparkId);
        spark.getReasons().addAll(List.of(
                buildReason("r1", ReasonType.GOOD, "Nice"),
                buildReason("r2", ReasonType.BAD, "Bad")
        ));

        when(sparkService.getSparkById(sparkId)).thenReturn(spark);

        reasonService.deleteAllReasonsBySparkId(sparkId);

        assertThat(spark.getReasons()).isEmpty();
        verify(sparkService).saveSpark(spark);
    }

    @Test
    void createReason_throwsWhenContentAlreadyExists() {

        String sparkId = "spark-1";
        Reason existing = buildReason("reason-1", ReasonType.GOOD, "Duplicate");

        Spark spark = buildSpark(sparkId);
        spark.getReasons().add(existing);

        when(sparkService.getSparkById(sparkId)).thenReturn(spark);

        CreateReasonRequestDTO dto = new CreateReasonRequestDTO("Duplicate", ReasonType.BAD);

        assertThatThrownBy(() -> reasonService.createReason(sparkId, dto))
                .isInstanceOf(ReasonAlreadyExistsException.class)
                .hasMessageContaining("Duplicate");

        verify(sparkService, never()).saveSpark(any(Spark.class));
    }

    @Test
    void updateReason_throwsWhenContentAlreadyExists() {

        String sparkId = "spark-1";
        String reasonId = "reason-1";

        Reason existing = buildReason(reasonId, ReasonType.GOOD, "Original");
        Reason duplicate = buildReason("reason-2", ReasonType.BAD, "Conflict");

        Spark spark = buildSpark(sparkId);
        spark.getReasons().addAll(List.of(existing, duplicate));

        when(sparkService.getSparkById(sparkId)).thenReturn(spark);

        UpdateReasonRequestDTO dto = new UpdateReasonRequestDTO("Conflict", ReasonType.GOOD);

        assertThatThrownBy(() -> reasonService.updateReason(sparkId, reasonId, dto))
                .isInstanceOf(ReasonAlreadyExistsException.class)
                .hasMessageContaining("Conflict");

        verify(sparkService, never()).saveSpark(any(Spark.class));
    }

    private Spark buildSpark(String id) {
        Spark spark = new Spark();
        spark.setId(id);
        spark.setTitle("Title " + id);
        spark.setDescription("Description " + id);
        spark.setReasons(new ArrayList<>());
        spark.setCreatedAt(Instant.now());
        spark.setUpdatedAt(Instant.now());
        return spark;
    }

    private Reason buildReason(String id, ReasonType type, String content) {
        Reason reason = new Reason();
        reason.setId(id);
        reason.setType(type);
        reason.setContent(content);
        reason.setCreatedAt(Instant.now());
        reason.setUpdatedAt(Instant.now());
        return reason;
    }
}

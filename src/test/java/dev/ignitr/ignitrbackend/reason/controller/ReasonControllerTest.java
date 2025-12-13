package dev.ignitr.ignitrbackend.reason.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ignitr.ignitrbackend.common.error.GlobalExceptionHandler;
import dev.ignitr.ignitrbackend.reason.dto.CreateReasonRequestDTO;
import dev.ignitr.ignitrbackend.reason.dto.UpdateReasonRequestDTO;
import dev.ignitr.ignitrbackend.reason.exception.ReasonAlreadyExistsException;
import dev.ignitr.ignitrbackend.reason.exception.ReasonNotFoundException;
import dev.ignitr.ignitrbackend.reason.model.Reason;
import dev.ignitr.ignitrbackend.reason.model.ReasonType;
import dev.ignitr.ignitrbackend.reason.service.ReasonService;
import dev.ignitr.ignitrbackend.spark.exception.SparkNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReasonController.class)
@Import(GlobalExceptionHandler.class)
class ReasonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReasonService reasonService;

    @Test
    void createReason_returns201AndBody_onSuccess() throws Exception {

        String sparkId = "spark-1";
        CreateReasonRequestDTO request = new CreateReasonRequestDTO("Great job", ReasonType.GOOD);
        Reason savedReason = buildReason("reason-1", ReasonType.GOOD, "Great job");

        when(reasonService.createReason(eq(sparkId), any(CreateReasonRequestDTO.class)))
                .thenReturn(savedReason);

        mockMvc.perform(post("/sparks/{sparkId}/reasons", sparkId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(savedReason.getId())))
                .andExpect(jsonPath("$.content", is(savedReason.getContent())))
                .andExpect(jsonPath("$.type", is(savedReason.getType().name())));
    }

    @Test
    void createReason_returns404_whenSparkNotFound() throws Exception {

        String sparkId = "missing-spark";
        CreateReasonRequestDTO request = new CreateReasonRequestDTO("Insight", ReasonType.GOOD);

        when(reasonService.createReason(eq(sparkId), any(CreateReasonRequestDTO.class)))
                .thenThrow(new SparkNotFoundException(sparkId));

        mockMvc.perform(post("/sparks/{sparkId}/reasons", sparkId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("SPARK_NOT_FOUND")))
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void createReason_returns400_onInvalidPayload() throws Exception {

        String sparkId = "spark-1";
        CreateReasonRequestDTO request = new CreateReasonRequestDTO("", ReasonType.GOOD);

        mockMvc.perform(post("/sparks/{sparkId}/reasons", sparkId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.status", is(400)));

        verifyNoInteractions(reasonService);
    }

    @Test
    void getReasonById_returns200_onSuccess() throws Exception {

        String sparkId = "spark-1";
        String reasonId = "reason-1";
        Reason reason = buildReason(reasonId, ReasonType.BAD, "Could improve");

        when(reasonService.getReasonById(sparkId, reasonId)).thenReturn(reason);

        mockMvc.perform(get("/sparks/{sparkId}/reasons/{reasonId}", sparkId, reasonId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(reasonId)))
                .andExpect(jsonPath("$.content", is(reason.getContent())))
                .andExpect(jsonPath("$.type", is(reason.getType().name())));
    }

    @Test
    void getReasonById_returns404_whenMissing() throws Exception {

        String sparkId = "spark-1";
        String reasonId = "missing";

        when(reasonService.getReasonById(sparkId, reasonId))
                .thenThrow(new ReasonNotFoundException(reasonId));

        mockMvc.perform(get("/sparks/{sparkId}/reasons/{reasonId}", sparkId, reasonId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("REASON_NOT_FOUND")))
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void getReasonsBySparkId_returns200AndContent_onSuccess() throws Exception {

        String sparkId = "spark-1";
        Reason reason1 = buildReason("r1", ReasonType.GOOD, "Great");
        Reason reason2 = buildReason("r2", ReasonType.BAD, "Needs work");

        Page<Reason> page = new PageImpl<>(List.of(reason1, reason2), PageRequest.of(0, 10), 2);

        when(reasonService.getReasonsBySparkId(sparkId, null, 0, 10)).thenReturn(page);

        mockMvc.perform(get("/sparks/{sparkId}/reasons", sparkId)
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id", is("r1")))
                .andExpect(jsonPath("$.content[1].id", is("r2")))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.page", is(0)));
    }

    @Test
    void getReasonsBySparkId_passesQueryParamsToService() throws Exception {

        String sparkId = "spark-1";

        when(reasonService.getReasonsBySparkId(sparkId, "good", 1, 5)).thenReturn(Page.empty());

        mockMvc.perform(get("/sparks/{sparkId}/reasons", sparkId)
                        .param("type", "good")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(reasonService).getReasonsBySparkId(sparkId, "good", 1, 5);
    }

    @Test
    void updateReason_returns200_onSuccess() throws Exception {

        String sparkId = "spark-1";
        String reasonId = "reason-1";
        UpdateReasonRequestDTO request = new UpdateReasonRequestDTO("Updated", ReasonType.GOOD);
        Reason updated = buildReason(reasonId, ReasonType.GOOD, "Updated");

        when(reasonService.updateReason(eq(sparkId), eq(reasonId), any(UpdateReasonRequestDTO.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/sparks/{sparkId}/reasons/{reasonId}", sparkId, reasonId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(reasonId)))
                .andExpect(jsonPath("$.content", is("Updated")))
                .andExpect(jsonPath("$.type", is("GOOD")));
    }

    @Test
    void updateReason_returns404_whenMissing() throws Exception {

        String sparkId = "spark-1";
        String reasonId = "missing";
        UpdateReasonRequestDTO request = new UpdateReasonRequestDTO("Updated", ReasonType.GOOD);

        when(reasonService.updateReason(eq(sparkId), eq(reasonId), any(UpdateReasonRequestDTO.class)))
                .thenThrow(new ReasonNotFoundException(reasonId));

        mockMvc.perform(put("/sparks/{sparkId}/reasons/{reasonId}", sparkId, reasonId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("REASON_NOT_FOUND")))
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void deleteReason_returns204_onSuccess() throws Exception {

        String sparkId = "spark-1";
        String reasonId = "reason-1";

        doNothing().when(reasonService).deleteReason(sparkId, reasonId);

        mockMvc.perform(delete("/sparks/{sparkId}/reasons/{reasonId}", sparkId, reasonId))
                .andExpect(status().isNoContent());

        verify(reasonService).deleteReason(sparkId, reasonId);
    }

    @Test
    void deleteReason_returns404_whenMissing() throws Exception {

        String sparkId = "spark-1";
        String reasonId = "missing";

        doThrow(new ReasonNotFoundException(reasonId))
                .when(reasonService)
                .deleteReason(sparkId, reasonId);

        mockMvc.perform(delete("/sparks/{sparkId}/reasons/{reasonId}", sparkId, reasonId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("REASON_NOT_FOUND")))
                .andExpect(jsonPath("$.status", is(404)));

        verify(reasonService).deleteReason(sparkId, reasonId);
    }

    @Test
    void deleteAllReasonsBySparkId_returns204_onSuccess() throws Exception {

        String sparkId = "spark-1";

        doNothing().when(reasonService).deleteAllReasonsBySparkId(sparkId);

        mockMvc.perform(delete("/sparks/{sparkId}/reasons", sparkId))
                .andExpect(status().isNoContent());

        verify(reasonService).deleteAllReasonsBySparkId(sparkId);
    }

    @Test
    void createReason_returns409_whenContentDuplicate() throws Exception {

        String sparkId = "spark-1";
        CreateReasonRequestDTO request = new CreateReasonRequestDTO("Duplicate", ReasonType.GOOD);

        when(reasonService.createReason(eq(sparkId), any(CreateReasonRequestDTO.class)))
                .thenThrow(new ReasonAlreadyExistsException("Duplicate"));

        mockMvc.perform(post("/sparks/{sparkId}/reasons", sparkId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("REASON_ALREADY_EXISTS")))
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    void updateReason_returns409_whenContentDuplicate() throws Exception {

        String sparkId = "spark-1";
        String reasonId = "reason-1";
        UpdateReasonRequestDTO request = new UpdateReasonRequestDTO("Duplicate", ReasonType.GOOD);

        when(reasonService.updateReason(eq(sparkId), eq(reasonId), any(UpdateReasonRequestDTO.class)))
                .thenThrow(new ReasonAlreadyExistsException("Duplicate"));

        mockMvc.perform(put("/sparks/{sparkId}/reasons/{reasonId}", sparkId, reasonId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("REASON_ALREADY_EXISTS")))
                .andExpect(jsonPath("$.status", is(409)));
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

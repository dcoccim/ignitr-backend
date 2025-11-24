package dev.ignitr.ignitrbackend.spark.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ignitr.ignitrbackend.common.error.GlobalExceptionHandler;
import dev.ignitr.ignitrbackend.spark.dto.CreateSparkRequestDTO;
import dev.ignitr.ignitrbackend.spark.dto.SparkDTO;
import dev.ignitr.ignitrbackend.spark.dto.SparkTreeDTO;
import dev.ignitr.ignitrbackend.spark.dto.UpdateSparkRequestDTO;
import dev.ignitr.ignitrbackend.spark.exception.SparkAlreadyExistsException;
import dev.ignitr.ignitrbackend.spark.exception.SparkNotFoundException;
import dev.ignitr.ignitrbackend.spark.model.SparkDeleteMode;
import dev.ignitr.ignitrbackend.spark.service.SparkService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SparkController.class)
@Import(GlobalExceptionHandler.class)
class SparkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SparkService sparkService;

    @Test
    void createTopLevelSpark_returns201AndBody_onSuccess() throws Exception {

        String title = "Test Spark";
        String description = "Test description";

        CreateSparkRequestDTO request = new CreateSparkRequestDTO(title, description);

        String mockId = "spark-id-123";
        Instant now = Instant.now();

        SparkDTO spark = new SparkDTO(mockId, title, description, now, now);

        when(sparkService.createSpark(any(CreateSparkRequestDTO.class)))
                .thenReturn(spark);

        mockMvc.perform(post("/api/sparks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(mockId)))
                .andExpect(jsonPath("$.title", is(title)))
                .andExpect(jsonPath("$.description", is(description)));
    }

    @Test
    void createSpark_returns409AndApiError_onDuplicateTitle() throws Exception {

        String duplicateTitle = "Duplicate";
        String description = "Description";

        CreateSparkRequestDTO request = new CreateSparkRequestDTO(duplicateTitle, description);

        when(sparkService.createSpark(any(CreateSparkRequestDTO.class)))
                .thenThrow(new SparkAlreadyExistsException(duplicateTitle));

        mockMvc.perform(post("/api/sparks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("SPARK_ALREADY_EXISTS")))
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    void createSpark_returns400AndValidationError_onInvalidPayload() throws Exception {

        String blankTitle = "";
        String description = "Description";

        CreateSparkRequestDTO request = new CreateSparkRequestDTO(
                blankTitle,
                description
        );

        mockMvc.perform(post("/api/sparks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void createChildSpark_returns201AndBody_onSuccess() throws Exception {

        String parentId = "parent-123";
        String title = "Child Spark";
        String description = "Description";

        CreateSparkRequestDTO request = new CreateSparkRequestDTO(title, description);

        String mockChildId = "child-id-456";
        Instant now = Instant.now();

        SparkDTO child = new SparkDTO (mockChildId, title, description, now, now);

        when(sparkService.createChildSpark(eq(parentId), any(CreateSparkRequestDTO.class)))
                .thenReturn(child);

        mockMvc.perform(post("/api/sparks/{parentId}/children", parentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(mockChildId)))
                .andExpect(jsonPath("$.title", is(title)))
                .andExpect(jsonPath("$.description", is(description)));
    }

    @Test
    void createChildSpark_returns404AndApiError_whenParentNotFound() throws Exception {

        String parentId = "missing-parent";
        String title = "Child Spark";
        String description = "Description";

        CreateSparkRequestDTO request = new CreateSparkRequestDTO(title, description);

        when(sparkService.createChildSpark(eq(parentId), any(CreateSparkRequestDTO.class)))
                .thenThrow(new SparkNotFoundException(parentId));

        mockMvc.perform(post("/api/sparks/{parentId}/children", parentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("SPARK_NOT_FOUND")))
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void getSpark_returns200AndBody_onSuccess() throws Exception {

        String id = "spark-123";
        String title = "My Spark";
        String description = "Description";
        Instant now = Instant.now();

        SparkDTO spark = new SparkDTO (id, title, description, now, now);

        when(sparkService.getSparkById(id)).thenReturn(spark);

        mockMvc.perform(get("/api/sparks/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.title", is(title)))
                .andExpect(jsonPath("$.description", is(description)));
    }

    @Test
    void getSpark_returns404AndApiError_whenNotFound() throws Exception {

        String missingId = "missing-id";

        when(sparkService.getSparkById(missingId))
                .thenThrow(new SparkNotFoundException(missingId));

        mockMvc.perform(get("/api/sparks/{id}", missingId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("SPARK_NOT_FOUND")))
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void getSparkChildren_returns200AndList_onSuccess() throws Exception {

        String parentId = "parent-123";
        String child1Id = "child-1";
        String child1Title = "Child 1";
        String child2Id = "child-2";
        String child2Title = "Child 2";
        Instant now = Instant.now();


        SparkDTO child1 = new SparkDTO(child1Id, child1Title, "Desc 1", now, now);
        SparkDTO child2 = new SparkDTO(child2Id, child2Title, "Desc 2", now, now);

        when(sparkService.getChildren(parentId)).thenReturn(List.of(child1, child2));

        mockMvc.perform(get("/api/sparks/{id}/children", parentId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id", is(child1Id)))
                .andExpect(jsonPath("$[0].title", is(child1Title)))
                .andExpect(jsonPath("$[1].id", is(child2Id)))
                .andExpect(jsonPath("$[1].title", is(child2Title)));
    }

    @Test
    void getSparkChildren_returns404AndApiError_whenParentNotFound() throws Exception {

        String missingParentId = "missing-parent";

        when(sparkService.getChildren(missingParentId))
                .thenThrow(new SparkNotFoundException(missingParentId));

        mockMvc.perform(get("/api/sparks/{id}/children", missingParentId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("SPARK_NOT_FOUND")))
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void getSparkTree_returns200AndBody_onSuccess() throws Exception {

        String rootId = "root-1";
        String child1Id = "child-1";
        String child2Id = "child-2";
        String childOfChild1Id = "child-of-child-1";
        String rootTitle = "Root";
        String child1Title = "Child 1";
        String child2Title = "Child 2";
        String childOfChild1Title = "Child 1 of child 1";
        Instant now = Instant.now();

        SparkTreeDTO childOfChild1 = new SparkTreeDTO(
                childOfChild1Id,
                childOfChild1Title,
                "Desc Child of Child 1",
                now,
                now,
                List.of()
        );

        SparkTreeDTO child1 = new SparkTreeDTO(
                child1Id,
                child1Title,
                "Desc Child 1",
                now,
                now,
                List.of(childOfChild1)
        );

        SparkTreeDTO child2 = new SparkTreeDTO(
                child2Id,
                child2Title,
                "Desc Child 2",
                now,
                now,
                List.of()
        );

        SparkTreeDTO tree = new SparkTreeDTO(
                rootId,
                rootTitle,
                "Root desc",
                now,
                now,
                List.of(child1, child2)
        );

        when(sparkService.getSparkTree(rootId)).thenReturn(tree);

        mockMvc.perform(get("/api/sparks/{id}/tree", rootId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.id", is(rootId)))
                .andExpect(jsonPath("$.title", is(rootTitle)))
                .andExpect(jsonPath("$.children").isArray())

                .andExpect(jsonPath("$.children[0].id", is(child1Id)))
                .andExpect(jsonPath("$.children[0].title", is(child1Title)))
                .andExpect(jsonPath("$.children[0].children").isArray())
                .andExpect(jsonPath("$.children[0].children[0].id", is(childOfChild1Id)))
                .andExpect(jsonPath("$.children[0].children[0].title", is(childOfChild1Title)))

                .andExpect(jsonPath("$.children[1].id", is(child2Id)))
                .andExpect(jsonPath("$.children[1].title", is(child2Title)))
                .andExpect(jsonPath("$.children[1].children").isArray())
                .andExpect(jsonPath("$.children[1].children").isEmpty());
    }

    @Test
    void getSparkTree_returns404AndApiError_whenRootNotFound() throws Exception {

        String missingId = "missing-id";
        when(sparkService.getSparkTree(missingId))
                .thenThrow(new SparkNotFoundException(missingId));

        mockMvc.perform(get("/api/sparks/{id}/tree", missingId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("SPARK_NOT_FOUND")))
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void updateSpark_returns200AndBody_onSuccess() throws Exception {

        String id = "spark-1";
        String newTitle = "Updated title";
        String newDescription = "Updated description";
        Instant now = Instant.now();

        UpdateSparkRequestDTO request = new UpdateSparkRequestDTO(newTitle, newDescription);

        SparkDTO updated = new SparkDTO(
                id,
                newTitle,
                newDescription,
                now.minusSeconds(3600),
                now
        );

        when(sparkService.updateSpark(eq(id), any(UpdateSparkRequestDTO.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/sparks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.title", is(newTitle)))
                .andExpect(jsonPath("$.description", is(newDescription)));
    }

    @Test
    void updateSpark_returns404AndApiError_whenNotFound() throws Exception {

        String missingId = "missing-id";
        UpdateSparkRequestDTO request = new UpdateSparkRequestDTO("Title", "Desc");

        when(sparkService.updateSpark(eq(missingId), any(UpdateSparkRequestDTO.class)))
                .thenThrow(new SparkNotFoundException(missingId));

        mockMvc.perform(put("/api/sparks/{id}", missingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("SPARK_NOT_FOUND")))
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void updateSpark_returns409AndApiError_onDuplicateTitle() throws Exception {

        String id = "spark-1";
        String duplicateTitle = "Duplicate title";
        UpdateSparkRequestDTO request = new UpdateSparkRequestDTO(duplicateTitle, "Desc");

        when(sparkService.updateSpark(eq(id), any(UpdateSparkRequestDTO.class)))
                .thenThrow(new SparkAlreadyExistsException(duplicateTitle));

        mockMvc.perform(put("/api/sparks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("SPARK_ALREADY_EXISTS")))
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    void updateSpark_returns400AndValidationError_onInvalidPayload() throws Exception {

        String id = "spark-1";

        UpdateSparkRequestDTO request = new UpdateSparkRequestDTO(
                "",
                "Some description"
        );

        mockMvc.perform(put("/api/sparks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void deleteSpark_returns204_onCascadeSuccess() throws Exception {

        String id = "spark-123";

        // Service does not throw → success
        doNothing().when(sparkService).deleteSpark(id, SparkDeleteMode.CASCADE);

        mockMvc.perform(delete("/api/sparks/{id}", id)
                        .param("mode", "CASCADE"))
                .andExpect(status().isNoContent());

        verify(sparkService).deleteSpark(id, SparkDeleteMode.CASCADE);
    }

    @Test
    void deleteSpark_returns204_onPromoteSuccess() throws Exception {

        String id = "spark-456";

        doNothing().when(sparkService).deleteSpark(id, SparkDeleteMode.PROMOTE);

        mockMvc.perform(delete("/api/sparks/{id}", id)
                        .param("mode", "PROMOTE"))
                .andExpect(status().isNoContent());

        verify(sparkService).deleteSpark(id, SparkDeleteMode.PROMOTE);
    }

    @Test
    void deleteSpark_returns404AndApiError_whenNotFound() throws Exception {

        String missingId = "missing-id";

        // Whenever deleteSpark is called with this id + CASCADE, throw not found
        doThrow(new SparkNotFoundException(missingId))
                .when(sparkService)
                .deleteSpark(missingId, SparkDeleteMode.CASCADE);

        mockMvc.perform(delete("/api/sparks/{id}", missingId)
                        .param("mode", "CASCADE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("SPARK_NOT_FOUND")))
                .andExpect(jsonPath("$.status", is(404)));

        verify(sparkService).deleteSpark(missingId, SparkDeleteMode.CASCADE);
    }

    @Test
    void searchSparks_returns200AndList_onSuccess() throws Exception {

        String id1 = "spark-1";
        String id2 = "spark-2";
        Instant now = Instant.now();

        SparkDTO dto1 = new SparkDTO(id1, "First spark", "Desc 1", now, now);
        SparkDTO dto2 = new SparkDTO(id2, "Second spark", "Desc 2", now, now);

        Page<SparkDTO> page = new PageImpl<>(
                List.of(dto1, dto2),
                PageRequest.of(0, 20),
                2
        );

        when(sparkService.searchSparks(null, null, 0, 20)).thenReturn(page);

        mockMvc.perform(get("/api/sparks")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id", is(id1)))
                .andExpect(jsonPath("$[0].title", is("First spark")))
                .andExpect(jsonPath("$[1].id", is(id2)))
                .andExpect(jsonPath("$[1].title", is("Second spark")));
    }

    @Test
    void searchSparks_returns200AndPassesQueryParams_toService() throws Exception {

        when(sparkService.searchSparks("test", "ROOT", 1, 10))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/sparks")
                        .param("title", "test")
                        .param("parentId", "ROOT")
                        .param("page", "1")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(sparkService).searchSparks("test", "ROOT", 1, 10);
    }
}
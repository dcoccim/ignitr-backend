package dev.ignitr.ignitrbackend.spark.controller;

import dev.ignitr.ignitrbackend.common.error.GlobalExceptionHandler;
import dev.ignitr.ignitrbackend.spark.dto.CreateSparkRequestDTO;
import dev.ignitr.ignitrbackend.spark.dto.PatchSparkRequestDTO;
import dev.ignitr.ignitrbackend.spark.dto.UpdateSparkRequestDTO;
import dev.ignitr.ignitrbackend.spark.exception.SparkAlreadyExistsException;
import dev.ignitr.ignitrbackend.spark.exception.SparkNotFoundException;
import dev.ignitr.ignitrbackend.spark.model.Spark;
import dev.ignitr.ignitrbackend.spark.service.ParentSearchScope;
import dev.ignitr.ignitrbackend.spark.service.SparkDeleteMode;
import dev.ignitr.ignitrbackend.spark.service.SparkService;
import dev.ignitr.ignitrbackend.spark.tree.SparkTree;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SparkController.class)
@Import(GlobalExceptionHandler.class)
class SparkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SparkService sparkService;

    @Test
    void createTopLevelSpark_returns201AndBody_onSuccess() throws Exception {

        String title = "Test Spark";
        String description = "Test description";

        CreateSparkRequestDTO request = new CreateSparkRequestDTO(title, description);

        ObjectId id = new ObjectId();
        Instant now = Instant.now();

        Spark spark = new Spark(id, title, description, null, List.of(), now, now);

        when(sparkService.createSpark(any(String.class), any(String.class)))
                .thenReturn(spark);

        mockMvc.perform(post("/sparks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(id.toHexString())))
                .andExpect(jsonPath("$.title", is(title)))
                .andExpect(jsonPath("$.description", is(description)));
    }

    @Test
    void createSpark_returns409AndApiError_onDuplicateTitle() throws Exception {

        String duplicateTitle = "Duplicate";
        String description = "Description";

        CreateSparkRequestDTO request = new CreateSparkRequestDTO(duplicateTitle, description);

        when(sparkService.createSpark(any(String.class), any(String.class)))
                .thenThrow(new SparkAlreadyExistsException(duplicateTitle));

        mockMvc.perform(post("/sparks")
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

        mockMvc.perform(post("/sparks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void createChildSpark_returns201AndBody_onSuccess() throws Exception {

        ObjectId parentId = new ObjectId();
        String title = "Child Spark";
        String description = "Description";

        CreateSparkRequestDTO request = new CreateSparkRequestDTO(title, description);

        ObjectId childId = new ObjectId();
        Instant now = Instant.now();

        Spark child = new Spark (childId, title, description, parentId, List.of(), now, now);

        when(sparkService.createChildSpark(eq(parentId), any(String.class), any(String.class)))
                .thenReturn(child);

        mockMvc.perform(post("/sparks/{parentId}/children", parentId.toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(childId.toHexString())))
                .andExpect(jsonPath("$.title", is(title)))
                .andExpect(jsonPath("$.description", is(description)));
    }

    @Test
    void createChildSpark_returns404AndApiError_whenParentNotFound() throws Exception {

        ObjectId missingParentId = new ObjectId("000000000000000000000001");
        String title = "Child Spark";
        String description = "Description";

        CreateSparkRequestDTO request = new CreateSparkRequestDTO(title, description);

        when(sparkService.createChildSpark(eq(missingParentId), any(String.class), any(String.class)))
                .thenThrow(new SparkNotFoundException(missingParentId));

        mockMvc.perform(post("/sparks/{parentId}/children", missingParentId.toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("SPARK_NOT_FOUND")))
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void getSpark_returns200AndBody_onSuccess() throws Exception {

        ObjectId id = new ObjectId();
        String title = "My Spark";
        String description = "Description";
        Instant now = Instant.now();

        Spark spark = new Spark (id, title, description, null, List.of(), now, now);

        when(sparkService.getSparkById(id)).thenReturn(spark);

        mockMvc.perform(get("/sparks/{id}", id.toHexString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(id.toHexString())))
                .andExpect(jsonPath("$.title", is(title)))
                .andExpect(jsonPath("$.description", is(description)));
    }

    @Test
    void getSpark_returns404AndApiError_whenNotFound() throws Exception {

        ObjectId missingId = new ObjectId("000000000000000000000001");

        when(sparkService.getSparkById(missingId))
                .thenThrow(new SparkNotFoundException(missingId));

        mockMvc.perform(get("/sparks/{id}", missingId.toHexString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("SPARK_NOT_FOUND")))
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void getSparkChildren_returns200AndList_onSuccess() throws Exception {

        ObjectId parentId = new ObjectId();
        ObjectId child1Id = new ObjectId();
        String child1Title = "Child 1";
        ObjectId child2Id = new ObjectId();
        String child2Title = "Child 2";
        Instant now = Instant.now();


        Spark child1 = new Spark(child1Id, child1Title, "Desc 1", parentId, List.of(), now, now);
        Spark child2 = new Spark(child2Id, child2Title, "Desc 2", parentId, List.of(), now, now);

        when(sparkService.getChildren(parentId)).thenReturn(List.of(child1, child2));

        mockMvc.perform(get("/sparks/{id}/children", parentId.toHexString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id", is(child1Id.toHexString())))
                .andExpect(jsonPath("$[0].title", is(child1Title)))
                .andExpect(jsonPath("$[1].id", is(child2Id.toHexString())))
                .andExpect(jsonPath("$[1].title", is(child2Title)));
    }

    @Test
    void getSparkChildren_returns404AndApiError_whenParentNotFound() throws Exception {

        ObjectId missingParentId = new ObjectId("000000000000000000000001");

        when(sparkService.getChildren(missingParentId))
                .thenThrow(new SparkNotFoundException(missingParentId));

        mockMvc.perform(get("/sparks/{id}/children", missingParentId.toHexString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("SPARK_NOT_FOUND")))
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void getSparkTree_returns200AndBody_onSuccess() throws Exception {

        ObjectId rootId = new ObjectId();
        ObjectId child1Id = new ObjectId();
        ObjectId child2Id = new ObjectId();
        ObjectId childOfChild1Id = new ObjectId();
        String rootTitle = "Root";
        String child1Title = "Child 1";
        String child2Title = "Child 2";
        String childOfChild1Title = "Child 1 of child 1";
        Instant now = Instant.now();

        Spark childOfChild1 = new Spark(
                childOfChild1Id,
                childOfChild1Title,
                "Desc Child of Child 1",
                child1Id,
                List.of(),
                now,
                now
        );

        Spark child1 = new Spark(
                child1Id,
                child1Title,
                "Desc Child 1",
                rootId,
                List.of(),
                now,
                now
        );

        Spark child2 = new Spark(
                child2Id,
                child2Title,
                "Desc Child 2",
                rootId,
                List.of(),
                now,
                now
        );

        Spark root = new Spark(
                rootId,
                rootTitle,
                "Root desc",
                null,
                List.of(),
                now,
                now
        );

        SparkTree childOfChildTree1 = SparkTree.fromSpark(childOfChild1, 0, 0, new ArrayList<>());
        SparkTree childTree1 = SparkTree.fromSpark(child1, 0, 0, List.of(childOfChildTree1));
        SparkTree childTree2 = SparkTree.fromSpark(child2, 0, 0, new ArrayList<>());
        SparkTree tree = SparkTree.fromSpark(root, 0, 0, List.of(childTree1, childTree2));

        when(sparkService.getSparkTree(rootId)).thenReturn(tree);

        mockMvc.perform(get("/sparks/{id}/tree", rootId.toHexString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.id", is(rootId.toHexString())))
                .andExpect(jsonPath("$.title", is(rootTitle)))
                .andExpect(jsonPath("$.children").isArray())

                .andExpect(jsonPath("$.children[0].id", is(child1Id.toHexString())))
                .andExpect(jsonPath("$.children[0].title", is(child1Title)))
                .andExpect(jsonPath("$.children[0].children").isArray())
                .andExpect(jsonPath("$.children[0].children[0].id", is(childOfChild1Id.toHexString())))
                .andExpect(jsonPath("$.children[0].children[0].title", is(childOfChild1Title)))

                .andExpect(jsonPath("$.children[1].id", is(child2Id.toHexString())))
                .andExpect(jsonPath("$.children[1].title", is(child2Title)))
                .andExpect(jsonPath("$.children[1].children").isArray())
                .andExpect(jsonPath("$.children[1].children").isEmpty());
    }

    @Test
    void getSparkTree_returns404AndApiError_whenRootNotFound() throws Exception {

        ObjectId missingId = new ObjectId("000000000000000000000001");
        when(sparkService.getSparkTree(missingId))
                .thenThrow(new SparkNotFoundException(missingId));

        mockMvc.perform(get("/sparks/{id}/tree", missingId.toHexString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("SPARK_NOT_FOUND")))
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void updateSpark_returns200AndBody_onSuccess() throws Exception {

        ObjectId id = new ObjectId();
        String newTitle = "Updated title";
        String newDescription = "Updated description";
        Instant now = Instant.now();

        UpdateSparkRequestDTO request = new UpdateSparkRequestDTO(newTitle, newDescription);

        Spark updated = new Spark(
                id,
                newTitle,
                newDescription,
                null,
                List.of(),
                now.minusSeconds(3600),
                now
        );

        when(sparkService.updateSpark(eq(id), any(String.class), any(String.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/sparks/{id}", id.toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(id.toHexString())))
                .andExpect(jsonPath("$.title", is(newTitle)))
                .andExpect(jsonPath("$.description", is(newDescription)));
    }

    @Test
    void updateSpark_returns404AndApiError_whenNotFound() throws Exception {

        ObjectId missingId = new ObjectId("000000000000000000000001");
        UpdateSparkRequestDTO request = new UpdateSparkRequestDTO("Title", "Desc");

        when(sparkService.updateSpark(eq(missingId), any(String.class), any(String.class)))
                .thenThrow(new SparkNotFoundException(missingId));

        mockMvc.perform(put("/sparks/{id}", missingId.toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("SPARK_NOT_FOUND")))
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void updateSpark_returns409AndApiError_onDuplicateTitle() throws Exception {

        ObjectId id = new ObjectId();
        String duplicateTitle = "Duplicate title";
        UpdateSparkRequestDTO request = new UpdateSparkRequestDTO(duplicateTitle, "Desc");

        when(sparkService.updateSpark(eq(id), any(String.class), any(String.class)))
                .thenThrow(new SparkAlreadyExistsException(duplicateTitle));

        mockMvc.perform(put("/sparks/{id}", id.toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("SPARK_ALREADY_EXISTS")))
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    void updateSpark_returns400AndValidationError_onInvalidPayload() throws Exception {

        ObjectId id = new ObjectId();

        UpdateSparkRequestDTO request = new UpdateSparkRequestDTO(
                "",
                "Some description"
        );

        mockMvc.perform(put("/sparks/{id}", id.toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void patchSpark_returns200AndBody_onSuccess() throws Exception {

        ObjectId id = new ObjectId();
        String newTitle = "Updated title";
        String newDescription = "Updated description";
        Instant now = Instant.now();

        PatchSparkRequestDTO request = new PatchSparkRequestDTO(newTitle, newDescription);

        Spark updated = new Spark(
                id,
                newTitle,
                newDescription,
                null,
                List.of(),
                now.minusSeconds(3600),
                now
        );

        when(sparkService.partialUpdateSpark(eq(id), any(String.class), any(String.class)))
                .thenReturn(updated);

        mockMvc.perform(patch("/sparks/{id}", id.toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(id.toHexString())))
                .andExpect(jsonPath("$.title", is(newTitle)))
                .andExpect(jsonPath("$.description", is(newDescription)));
    }

    @Test
    void patchSpark_returns409AndApiError_onDuplicateTitle() throws Exception {

        ObjectId id = new ObjectId();
        String duplicateTitle = "Duplicate title";
        PatchSparkRequestDTO request = new PatchSparkRequestDTO(duplicateTitle, "Desc");

        when(sparkService.partialUpdateSpark(eq(id), any(String.class), any(String.class)))
                .thenThrow(new SparkAlreadyExistsException(duplicateTitle));

        mockMvc.perform(patch("/sparks/{id}", id.toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("SPARK_ALREADY_EXISTS")))
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    void patchSpark_returns400AndValidationError_onInvalidPayload() throws Exception {

        ObjectId id = new ObjectId();

        PatchSparkRequestDTO request = new PatchSparkRequestDTO(
                "",
                "Some description"
        );

        mockMvc.perform(patch("/sparks/{id}", id.toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void deleteSpark_returns204_onCascadeSuccess() throws Exception {

        ObjectId id = new ObjectId();

        // Service does not throw → success
        doNothing().when(sparkService).deleteSpark(id, SparkDeleteMode.CASCADE);

        mockMvc.perform(delete("/sparks/{id}", id.toHexString())
                        .param("mode", "cascade"))
                .andExpect(status().isNoContent());

        verify(sparkService).deleteSpark(id, SparkDeleteMode.CASCADE);
    }

    @Test
    void deleteSpark_returns204_onPromoteSuccess() throws Exception {

        ObjectId id = new ObjectId();

        doNothing().when(sparkService).deleteSpark(id, SparkDeleteMode.PROMOTE);

        mockMvc.perform(delete("/sparks/{id}", id.toHexString())
                        .param("mode", "promote"))
                .andExpect(status().isNoContent());

        verify(sparkService).deleteSpark(id, SparkDeleteMode.PROMOTE);
    }

    @Test
    void deleteSpark_returns404AndApiError_whenNotFound() throws Exception {

        ObjectId missingId = new ObjectId("000000000000000000000001");

        // Whenever deleteSpark is called with this id + CASCADE, throw not found
        doThrow(new SparkNotFoundException(missingId))
                .when(sparkService)
                .deleteSpark(missingId, SparkDeleteMode.CASCADE);

        mockMvc.perform(delete("/sparks/{id}", missingId.toHexString())
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

        ObjectId id1 = new ObjectId();
        ObjectId id2 = new ObjectId();
        String title1 = "First spark";
        String title2 = "Second spark";
        Instant now = Instant.now();

        Spark spark1 = new Spark(id1, title1, "Desc 1", null, List.of(), now, now);
        Spark spark2 = new Spark(id2, title2, "Desc 2", null, List.of(), now, now);

        var page = new PageImpl<>(
                List.of(spark1, spark2),
                PageRequest.of(0, 20),
                2
        );

        when(sparkService.searchSparks(null, ParentSearchScope.ANY, null, 0, 20)).thenReturn(page);

        mockMvc.perform(get("/sparks")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id", is(id1.toHexString())))
                .andExpect(jsonPath("$.content[0].title", is("First spark")))
                .andExpect(jsonPath("$.content[1].id", is(id2.toHexString())))
                .andExpect(jsonPath("$.content[1].title", is("Second spark")));
    }

    @Test
    void searchSparks_returns200AndPassesQueryParams_toService() throws Exception {

        when(sparkService.searchSparks("test", ParentSearchScope.ROOT, null,1, 10))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/sparks")
                        .param("title", "test")
                        .param("parentId", "ROOT")
                        .param("page", "1")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(sparkService).searchSparks("test", ParentSearchScope.ROOT, null, 1, 10);
    }
}
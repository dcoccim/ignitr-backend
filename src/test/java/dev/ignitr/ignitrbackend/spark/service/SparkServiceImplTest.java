package dev.ignitr.ignitrbackend.spark.service;

import dev.ignitr.ignitrbackend.score.service.SparkScoreService;
import dev.ignitr.ignitrbackend.spark.dto.CreateSparkRequestDTO;
import dev.ignitr.ignitrbackend.spark.dto.PatchSparkRequestDTO;
import dev.ignitr.ignitrbackend.spark.dto.UpdateSparkRequestDTO;
import dev.ignitr.ignitrbackend.spark.exception.SparkAlreadyExistsException;
import dev.ignitr.ignitrbackend.spark.exception.SparkNotFoundException;
import dev.ignitr.ignitrbackend.spark.model.Spark;
import dev.ignitr.ignitrbackend.spark.model.SparkDeleteMode;
import dev.ignitr.ignitrbackend.spark.repository.SparkRepository;
import dev.ignitr.ignitrbackend.spark.tree.SparkTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SparkServiceImplTest {

    @Mock
    private SparkRepository sparkRepository;

    @Mock
    private SparkScoreService sparkScoreService;

    private SparkService sparkService;


    @BeforeEach
    void setUp() {
        sparkService = new SparkServiceImpl(sparkRepository, sparkScoreService);
    }

    @Test
    void createSpark_savesAndReturnsSpark_whenTitleIsUnique() {

        String mockId = "mock-id-123";
        String uniqueTitle = "My unique spark";
        String description = "Description";

        CreateSparkRequestDTO dto = new CreateSparkRequestDTO(
                uniqueTitle,
                description
        );

        when(sparkRepository.existsByTitle(uniqueTitle)).thenReturn(false);

        when(sparkRepository.save(any(Spark.class)))
                .thenAnswer(invocation -> {
                    Spark arg = invocation.getArgument(0);
                    arg.setId(mockId);
                    return arg;
                });

        Spark result = sparkService.createSpark(dto);

        verify(sparkRepository).existsByTitle(uniqueTitle);

        ArgumentCaptor<Spark> captor = ArgumentCaptor.forClass(Spark.class);

        verify(sparkRepository).save(captor.capture());

        Spark savedSpark = captor.getValue();

        assertThat(savedSpark.getTitle()).isEqualTo(uniqueTitle);
        assertThat(savedSpark.getDescription()).isEqualTo(description);
        assertThat(savedSpark.getCreatedAt()).isNotNull();
        assertThat(savedSpark.getUpdatedAt()).isNotNull();

        assertThat(result.getId()).isEqualTo(savedSpark.getId());
        assertThat(result.getTitle()).isEqualTo(savedSpark.getTitle());
        assertThat(result.getDescription()).isEqualTo(savedSpark.getDescription());
        assertThat(result.getCreatedAt()).isEqualTo(savedSpark.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(savedSpark.getUpdatedAt());

    }

    @Test
    void createSpark_throws_whenTitleAlreadyExists() {

        String duplicateTitle = "Duplicate title";
        String description = "Description";

        CreateSparkRequestDTO dto = new CreateSparkRequestDTO(
                duplicateTitle,
                description
        );

        when(sparkRepository.existsByTitle(duplicateTitle)).thenReturn(true);

        assertThatThrownBy(() -> sparkService.createSpark(dto))
                .isInstanceOf(SparkAlreadyExistsException.class)
                .hasMessageContaining(duplicateTitle);

        verify(sparkRepository).existsByTitle(duplicateTitle);
        verify(sparkRepository, never()).save(any(Spark.class));
    }

    @Test
    void createChildSpark_savesAndReturnsDto_whenParentExistsAndTitleUnique() {

        String parentId = "parent-123";
        String mockChildId = "child-456";
        String title = "Child spark";
        String description = "Child description";

        Instant now = Instant.now();

        Spark parent = new Spark(parentId, "Parent", "Desc", null, List.of(), now, now);

        CreateSparkRequestDTO dto = new CreateSparkRequestDTO(title, description);

        when(sparkRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(sparkRepository.existsByTitle(title)).thenReturn(false);

        when(sparkRepository.save(any(Spark.class)))
                .thenAnswer(invocation -> {
                    Spark saved = invocation.getArgument(0);
                    saved.setId(mockChildId);
                    return saved;
                });

        Spark result = sparkService.createChildSpark(parentId, dto);

        verify(sparkRepository).findById(parentId);
        verify(sparkRepository).existsByTitle(title);

        ArgumentCaptor<Spark> captor = ArgumentCaptor.forClass(Spark.class);
        verify(sparkRepository).save(captor.capture());

        Spark savedChildSpark = captor.getValue();

        assertThat(savedChildSpark.getParentId()).isEqualTo(parentId);
        assertThat(savedChildSpark.getTitle()).isEqualTo(title);
        assertThat(savedChildSpark.getDescription()).isEqualTo(description);

        assertThat(result.getId()).isEqualTo(savedChildSpark.getId());
        assertThat(result.getTitle()).isEqualTo(savedChildSpark.getTitle());
        assertThat(result.getDescription()).isEqualTo(savedChildSpark.getDescription());
        assertThat(result.getCreatedAt()).isEqualTo(savedChildSpark.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(savedChildSpark.getUpdatedAt());
    }

    @Test
    void createChildSpark_throws_whenParentNotFound() {

        String parentId = "missing-parent";
        CreateSparkRequestDTO dto = new CreateSparkRequestDTO("Title", "Desc");

        when(sparkRepository.findById(parentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sparkService.createChildSpark(parentId, dto))
                .isInstanceOf(SparkNotFoundException.class)
                .hasMessageContaining(parentId);

        verify(sparkRepository).findById(parentId);
        verify(sparkRepository, never()).existsByTitle(anyString());
        verify(sparkRepository, never()).save(any(Spark.class));
    }

    @Test
    void createChildSpark_throws_whenTitleAlreadyExists() {

        String parentId = "parent-123";
        String duplicateTitle = "Duplicate";
        String description = "Desc";

        Instant now = Instant.now();

        Spark parent = new Spark(parentId, "Parent", "Desc", null, List.of(),now, now);

        CreateSparkRequestDTO dto = new CreateSparkRequestDTO(duplicateTitle, description);

        when(sparkRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(sparkRepository.existsByTitle(duplicateTitle)).thenReturn(true);

        assertThatThrownBy(() -> sparkService.createChildSpark(parentId, dto))
                .isInstanceOf(SparkAlreadyExistsException.class)
                .hasMessageContaining(duplicateTitle);

        verify(sparkRepository).findById(parentId);
        verify(sparkRepository).existsByTitle(duplicateTitle);
        verify(sparkRepository, never()).save(any(Spark.class));
    }

    @Test
    void getSparkById_returnsDto_whenExists() {

        String id = "mock-id-123";
        String title = "Title";
        String description = "Description";
        Instant now = Instant.now();
        Spark spark = new Spark(id, title, description, null, List.of(), now, now);

        when(sparkRepository.findById(id)).thenReturn(Optional.of(spark));

        Spark result = sparkService.getSparkById(id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getTitle()).isEqualTo(title);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);

        verify(sparkRepository).findById(id);
    }

    @Test
    void getSparkById_throws_whenNotFound() {

        String missingId = "missing-id";

        when(sparkRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sparkService.getSparkById(missingId))
                .isInstanceOf(SparkNotFoundException.class)
                .hasMessageContaining(missingId);

        verify(sparkRepository).findById(missingId);
    }

    @Test
    void getChildren_returnsSparkList_whenParentExists() {

        String parentId = "parent-123";
        String child1Id = "child-1";
        String child2Id = "child-2";
        Instant now = Instant.now();

        Spark parent = new Spark(parentId, "Parent", "Parent desc", null, List.of(), now, now);
        Spark child1 = new Spark(child1Id, "Child 1", "Desc 1", parentId, List.of(), now, now);
        Spark child2 = new Spark(child2Id, "Child 2", "Desc 2", parentId, List.of(), now, now);

        when(sparkRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(sparkRepository.findByParentId(parentId)).thenReturn(List.of(child1, child2));

        List<Spark> result = sparkService.getChildren(parentId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Spark::getId).containsExactly(child1Id, child2Id);

        verify(sparkRepository).findById(parentId);
        verify(sparkRepository).findByParentId(parentId);
    }

    @Test
    void getChildren_throws_whenParentNotFound() {

        String missingParentId = "missing-parent";

        when(sparkRepository.findById(missingParentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sparkService.getChildren(missingParentId))
                .isInstanceOf(SparkNotFoundException.class)
                .hasMessageContaining(missingParentId);

        verify(sparkRepository).findById(missingParentId);
        verify(sparkRepository, never()).findByParentId(anyString());
    }


    @Test
    void getSparkTreeList_returnsTreeList_whenRootExists() {

        String rootId = "root-1";
        String rootTitle = "Root";
        String child1Id = "child-1";
        String child2Id = "child-2";
        Instant now = Instant.now();

        Spark root = new Spark(rootId, rootTitle, "Root desc", null, List.of(), now, now);
        Spark child1 = new Spark(child1Id, "Child 1", "Child 1 desc", rootId, List.of(), now, now);
        Spark child2 = new Spark(child2Id, "Child 2", "Child 2 desc", rootId, List.of(), now, now);

        when(sparkRepository.findById(rootId)).thenReturn(Optional.of(root));
        when(sparkRepository.findByParentId(rootId)).thenReturn(List.of(child1, child2));
        when(sparkRepository.findByParentId(child1Id)).thenReturn(List.of());
        when(sparkRepository.findByParentId(child2Id)).thenReturn(List.of());

        SparkTree result = sparkService.getSparkTree(rootId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(rootId);
        assertThat(result.getChildren()).hasSize(2);
        assertThat(result.getChildren())
                .extracting(SparkTree::getId)
                .containsExactlyInAnyOrder(child1Id, child2Id);
    }

    @Test
    void getSparkTreeList_returnsOneElement_whenNoChildren() {

        String rootId = "leaf-1";
        Instant now = Instant.now();

        Spark root = new Spark(rootId, "Leaf", "Leaf desc", null, List.of(), now, now);

        when(sparkRepository.findById(rootId)).thenReturn(Optional.of(root));
        when(sparkRepository.findByParentId(rootId)).thenReturn(List.of());

        SparkTree result = sparkService.getSparkTree(rootId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(rootId);

        verify(sparkRepository).findById(rootId);
        verify(sparkRepository).findByParentId(rootId);
    }

    @Test
    void getSparkTreeList_throws_whenRootNotFound() {

        String missingRootId = "missing-id";

        when(sparkRepository.findById(missingRootId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sparkService.getSparkTree(missingRootId))
                .isInstanceOf(SparkNotFoundException.class)
                .hasMessageContaining(missingRootId);

        verify(sparkRepository).findById(missingRootId);
        verify(sparkRepository, never()).findByParentId(anyString());
    }

    @Test
    void updateSpark_updatesEntityAndReturnsUpdatedSpark_whenTitleIsUniqueOrUnchanged() {

        String id = "spark-1";
        String oldTitle = "Old title";
        String oldDescription = "Old desc";
        Instant createdAt = Instant.now().minusSeconds(3600);

        Spark existing = new Spark(id, oldTitle, oldDescription, null, List.of(), createdAt, createdAt);

        String newTitle = "New title";
        String newDescription = "New desc";

        UpdateSparkRequestDTO dto = new UpdateSparkRequestDTO(newTitle, newDescription);

        when(sparkRepository.findById(id)).thenReturn(Optional.of(existing));
        when(sparkRepository.existsByTitle(newTitle)).thenReturn(false);
        when(sparkRepository.save(any(Spark.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Spark result = sparkService.updateSpark(id, dto);

        ArgumentCaptor<Spark> captor = ArgumentCaptor.forClass(Spark.class);
        verify(sparkRepository).save(captor.capture());
        Spark saved = captor.getValue();

        assertThat(saved.getId()).isEqualTo(id);
        assertThat(saved.getTitle()).isEqualTo(newTitle);
        assertThat(saved.getDescription()).isEqualTo(newDescription);
        assertThat(saved.getCreatedAt()).isEqualTo(createdAt);
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isAfter(createdAt);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getTitle()).isEqualTo(newTitle);
        assertThat(result.getDescription()).isEqualTo(newDescription);

        verify(sparkRepository).findById(id);
        verify(sparkRepository).existsByTitle(newTitle);
    }

    @Test
    void updateSpark_throwsWhenSparkNotFound() {

        String missingId = "missing-id";
        UpdateSparkRequestDTO dto = new UpdateSparkRequestDTO("New title", "New desc");

        when(sparkRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sparkService.updateSpark(missingId, dto))
                .isInstanceOf(SparkNotFoundException.class)
                .hasMessageContaining(missingId);

        verify(sparkRepository).findById(missingId);
        verify(sparkRepository, never()).existsByTitle(anyString());
        verify(sparkRepository, never()).save(any(Spark.class));
    }

    @Test
    void updateSpark_throwsWhenNewTitleAlreadyExists() {

        String id = "spark-1";
        String oldTitle = "Old title";
        String oldDescription = "Old desc";
        Instant now = Instant.now();

        Spark existing = new Spark(id, oldTitle, oldDescription, null, List.of(), now, now);

        String duplicateTitle = "Duplicate title";
        UpdateSparkRequestDTO dto = new UpdateSparkRequestDTO(duplicateTitle, "New desc");

        when(sparkRepository.findById(id)).thenReturn(Optional.of(existing));
        when(sparkRepository.existsByTitle(duplicateTitle)).thenReturn(true);

        assertThatThrownBy(() -> sparkService.updateSpark(id, dto))
                .isInstanceOf(SparkAlreadyExistsException.class)
                .hasMessageContaining(duplicateTitle);

        verify(sparkRepository).findById(id);
        verify(sparkRepository).existsByTitle(duplicateTitle);
        verify(sparkRepository, never()).save(any(Spark.class));
    }

    @Test
    void partialUpdateSpark_updatesProvidedFields_whenSparkExists() {

        String id = "spark-1";
        Instant createdAt = Instant.now().minusSeconds(7200);

        Spark existing = new Spark(id, "Original title", "Original desc", null, List.of(), createdAt, createdAt);

        PatchSparkRequestDTO dto = new PatchSparkRequestDTO(null, "New partial desc");

        when(sparkRepository.findById(id)).thenReturn(Optional.of(existing));
        when(sparkRepository.save(any(Spark.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Spark result = sparkService.partialUpdateSpark(id, dto);

        ArgumentCaptor<Spark> captor = ArgumentCaptor.forClass(Spark.class);
        verify(sparkRepository).save(captor.capture());
        Spark saved = captor.getValue();

        assertThat(saved.getTitle()).isEqualTo("Original title");
        assertThat(saved.getDescription()).isEqualTo("New partial desc");
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isAfter(createdAt);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getTitle()).isEqualTo("Original title");
        assertThat(result.getDescription()).isEqualTo("New partial desc");

        verify(sparkRepository).findById(id);
        verify(sparkRepository, never()).existsByTitle(anyString());
    }

    @Test
    void partialUpdateSpark_throwsWhenSparkNotFound() {

        String missingId = "missing-id";
        PatchSparkRequestDTO dto = new PatchSparkRequestDTO("Some title", null);

        when(sparkRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sparkService.partialUpdateSpark(missingId, dto))
                .isInstanceOf(SparkNotFoundException.class)
                .hasMessageContaining(missingId);

        verify(sparkRepository).findById(missingId);
        verify(sparkRepository, never()).existsByTitle(anyString());
        verify(sparkRepository, never()).save(any(Spark.class));
    }

    @Test
    void partialUpdateSpark_throwsWhenNewTitleAlreadyExists() {

        String id = "spark-1";
        Instant now = Instant.now();

        Spark existing = new Spark(id, "Original", "Desc", null, List.of(), now, now);

        String duplicateTitle = "Duplicate title";
        PatchSparkRequestDTO dto = new PatchSparkRequestDTO(duplicateTitle, null);

        when(sparkRepository.findById(id)).thenReturn(Optional.of(existing));
        when(sparkRepository.existsByTitle(duplicateTitle)).thenReturn(true);

        assertThatThrownBy(() -> sparkService.partialUpdateSpark(id, dto))
                .isInstanceOf(SparkAlreadyExistsException.class)
                .hasMessageContaining(duplicateTitle);

        verify(sparkRepository).findById(id);
        verify(sparkRepository).existsByTitle(duplicateTitle);
        verify(sparkRepository, never()).save(any(Spark.class));
    }

    @Test
    void deleteSpark_cascade_deletesSubtree() {

        String rootId = "root-1";
        String child1Id = "child-1";
        String child2Id = "child-2";
        String grandchildId = "grandchild-1";

        Instant now = Instant.now();

        Spark root = new Spark(rootId, "Root", "Root desc", null, List.of(), now, now);
        Spark child1 = new Spark(child1Id, "Child 1", "Desc 1", rootId, List.of(), now, now);
        Spark child2 = new Spark(child2Id, "Child 2", "Desc 2", rootId, List.of(), now, now);
        Spark grandchild = new Spark(grandchildId, "Grandchild", "Desc gc", child1Id, List.of(), now, now);

        when(sparkRepository.findById(rootId)).thenReturn(Optional.of(root));
        when(sparkRepository.findByParentId(rootId)).thenReturn(List.of(child1, child2));
        when(sparkRepository.findByParentId(child1Id)).thenReturn(List.of(grandchild));
        when(sparkRepository.findByParentId(child2Id)).thenReturn(List.of());
        when(sparkRepository.findByParentId(grandchildId)).thenReturn(List.of());

        sparkService.deleteSpark(rootId, SparkDeleteMode.CASCADE);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<String>> captor =
                (ArgumentCaptor<Iterable<String>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Iterable.class);

        verify(sparkRepository).deleteAllById(captor.capture());

        List<String> deletedIds = new ArrayList<>();
        for (String deletedId : captor.getValue()) {
            deletedIds.add(deletedId);
        }

        assertThat(deletedIds)
                .containsExactlyInAnyOrder(rootId, child1Id, child2Id, grandchildId);

        verify(sparkRepository).findById(rootId);
        verify(sparkRepository, atLeastOnce()).findByParentId(anyString());
        verify(sparkRepository, never()).save(any(Spark.class));
        verify(sparkRepository, never()).saveAll(anyList());
    }

    @Test
    void deleteSpark_promote_reparentsChildrenAndDeletesNode() {

        String parentId = "parent-0";
        String rootId = "root-1";
        String child1Id = "child-1";
        String child2Id = "child-2";

        Instant now = Instant.now();

        Spark root = new Spark(rootId, "Root", "Root desc", parentId, List.of(), now, now);
        Spark child1 = new Spark(child1Id, "Child 1", "Desc 1", rootId, List.of(), now, now);
        Spark child2 = new Spark(child2Id, "Child 2", "Desc 2", rootId, List.of(), now, now);

        when(sparkRepository.findById(rootId)).thenReturn(Optional.of(root));
        when(sparkRepository.findByParentId(rootId)).thenReturn(List.of(child1, child2));

        sparkService.deleteSpark(rootId, SparkDeleteMode.PROMOTE);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Spark>> saveAllCaptor =
                (ArgumentCaptor<List<Spark>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);

        verify(sparkRepository).saveAll(saveAllCaptor.capture());
        List<Spark> savedChildren = saveAllCaptor.getValue();

        assertThat(savedChildren).hasSize(2);
        assertThat(savedChildren)
                .extracting(Spark::getParentId)
                .containsOnly(parentId);

        verify(sparkRepository).deleteById(rootId);
        verify(sparkRepository).findById(rootId);
        verify(sparkRepository).findByParentId(rootId);
        verify(sparkRepository, never()).deleteAllById(anyIterable());
    }

    @Test
    void deleteSpark_throws_whenNotFound() {

        String missingId = "missing-id";

        when(sparkRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sparkService.deleteSpark(missingId, SparkDeleteMode.CASCADE))
                .isInstanceOf(SparkNotFoundException.class)
                .hasMessageContaining(missingId);

        verify(sparkRepository).findById(missingId);
        verify(sparkRepository, never()).deleteAllById(anyIterable());
        verify(sparkRepository, never()).deleteById(anyString());
        verify(sparkRepository, never()).saveAll(anyList());
    }

    @Test
    void searchSparks_noFilters_callsFindAll() {

        Instant now = Instant.now();
        Spark s1 = new Spark("id1", "Title 1", "D1", null, List.of(), now, now);
        Spark s2 = new Spark("id2", "Title 2", "D2", "parent-1", List.of(), now, now);

        var page = new PageImpl<>(List.of(s1, s2), PageRequest.of(0, 20), 2);

        when(sparkRepository.findAll(any(Pageable.class))).thenReturn(page);

        var result = sparkService.searchSparks(null, null, 0, 20);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Spark::getId).containsExactly("id1", "id2");

        verify(sparkRepository).findAll(any(Pageable.class));
    }

    @Test
    void searchSparks_withParentIdRoot_callsFindByParentIdIsNull() {

        when(sparkRepository.findByParentIdIsNull(any(Pageable.class)))
                .thenReturn(Page.empty());

        var result = sparkService.searchSparks(null, "ROOT", 0, 20);

        assertThat(result.getContent()).isEmpty();

        verify(sparkRepository).findByParentIdIsNull(any(Pageable.class));
    }

    @Test
    void searchSparks_withTitleAndParent_callsCombinedMethod() {

        String parentId = "parent-123";

        when(sparkRepository.findByParentIdAndTitleContainingIgnoreCase(
                eq(parentId),
                eq("test"),
                any(Pageable.class)))
                .thenReturn(Page.empty());

        var result = sparkService.searchSparks("test", parentId, 0, 20);

        assertThat(result.getContent()).isEmpty();

        verify(sparkRepository)
                .findByParentIdAndTitleContainingIgnoreCase(eq(parentId), eq("test"), any(Pageable.class));
    }
}

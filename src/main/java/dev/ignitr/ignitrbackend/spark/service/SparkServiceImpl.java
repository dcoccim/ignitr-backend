package dev.ignitr.ignitrbackend.spark.service;

import dev.ignitr.ignitrbackend.spark.dto.*;
import dev.ignitr.ignitrbackend.spark.exception.SparkAlreadyExistsException;
import dev.ignitr.ignitrbackend.spark.exception.SparkNotFoundException;
import dev.ignitr.ignitrbackend.spark.mapper.SparkMapper;
import dev.ignitr.ignitrbackend.spark.model.Spark;
import dev.ignitr.ignitrbackend.spark.model.SparkDeleteMode;
import dev.ignitr.ignitrbackend.spark.repository.SparkRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Service
public class SparkServiceImpl implements SparkService {

    private static final Logger log = LoggerFactory.getLogger(SparkServiceImpl.class);

    private final SparkRepository sparkRepository;

    public SparkServiceImpl(SparkRepository sparkRepository) {
        this.sparkRepository = sparkRepository;
    }

    private void checkExistingTitle(String operation, String title) {
        if (sparkRepository.existsByTitle(title)) {
            log.warn("'{}': a Spark with title '{}' already exists", operation, title);
            throw new SparkAlreadyExistsException(title);
        }
    }

    @Override
    public SparkDTO createSpark(CreateSparkRequestDTO dto) {

        String title = dto.title();

        checkExistingTitle("createSpark", title);

        Instant now = Instant.now();
        Spark newSpark = SparkMapper.toNewEntity(dto, now);

        log.debug("Creating new Spark with title='{}'", newSpark.getTitle());
        Spark savedSpark = sparkRepository.save(newSpark);
        log.info("Created Spark with id='{}'", savedSpark.getId());

        return SparkMapper.toSparkDto(savedSpark);
    }

    @Override
    public SparkDTO createChildSpark(String parentId, CreateSparkRequestDTO dto) {

        Spark parent = sparkRepository.findById(parentId)
                .orElseThrow(() -> {
                    log.warn("Cannot find parent Spark with id='{}' to append child Spark", parentId);
                    return new SparkNotFoundException(parentId);
                });

        String title = dto.title();

        checkExistingTitle("createChildSpark", title);

        log.debug("Creating new Spark with title='{}' and parentId='{}'", title, parent.getId());
        Instant now = Instant.now();
        Spark childSpark = SparkMapper.toNewChildEntity(dto, parent.getId(),now);

        Spark savedSpark = sparkRepository.save(childSpark);
        log.info("Created Spark with id='{}' and parentId='{}'", savedSpark.getId(), savedSpark.getParentId());

        return SparkMapper.toSparkDto(savedSpark);
    }

    @Override
    public SparkDTO getSparkById(String id) {
        log.debug("Fetching Spark with id='{}'", id);
        Spark spark = sparkRepository.findById(id).orElseThrow(() -> new SparkNotFoundException(id));
        log.info("Retrieved Spark with id='{}'", id);
        return SparkMapper.toSparkDto(spark);
    }

    @Override
    public List<SparkDTO> getChildren(String parentId) {
        log.debug("Fetching children for Spark with id='{}'", parentId);

        Spark parent = sparkRepository.findById(parentId)
                .orElseThrow(() -> {
                    log.warn("Cannot fetch children: parent Spark with id='{}' not found", parentId);
                    return new SparkNotFoundException(parentId);
                });

        List<Spark> children = sparkRepository.findByParentId(parent.getId());

        log.info("Found {} children for Spark with id='{}'", children.size(), parentId);

        return children.stream().map(SparkMapper::toSparkDto).toList();
    }

    private SparkTreeDTO buildTree(Spark spark) {
        List<Spark> children = sparkRepository.findByParentId(spark.getId());

        List<SparkTreeDTO> childTrees = children.stream().map(this::buildTree).toList();

        return SparkMapper.toTreeDto(spark, childTrees);
    }

    @Override
    public SparkTreeDTO getSparkTree(String rootId) {

        log.debug("Building Spark tree from rootId='{}'", rootId);

        Spark root = sparkRepository.findById(rootId).orElseThrow(() -> {
            log.warn("Could not find root Spark with id='{}'", rootId);
            return new SparkNotFoundException(rootId);
        });

        return buildTree(root);
    }

    @Override
    public SparkDTO updateSpark(String id, UpdateSparkRequestDTO dto) {

        Spark spark = sparkRepository.findById(id).orElseThrow(() -> {
            log.warn("Cannot find Spark with id='{}' to update", id);
            return new SparkNotFoundException(id);
        });

        String newTitle = dto.title();

        if(!spark.getTitle().equals(newTitle)) {
            checkExistingTitle("updateSpark", newTitle);
        }

        log.debug("Updating Spark with id='{}'", spark.getId());
        Instant now = Instant.now();
        SparkMapper.updateEntity(spark, dto, now);
        Spark savedSpark = sparkRepository.save(spark);
        log.info("Updated Spark with id='{}'", savedSpark.getId());

        return SparkMapper.toSparkDto(savedSpark);
    }

    @Override
    public SparkDTO partialUpdateSpark(String id, PatchSparkRequestDTO dto) {

        Spark spark = sparkRepository.findById(id).orElseThrow(() -> {
            log.warn("Cannot find Spark with id='{}' for partial update", id);
            return new SparkNotFoundException(id);
        });

        String newTitle = dto.title();

        if(newTitle != null && !spark.getTitle().equals(newTitle)) {
            checkExistingTitle("partialUpdateSpark", newTitle);
        }

        log.debug("Partially updating Spark with id='{}'", spark.getId());
        Instant now = Instant.now();
        SparkMapper.partialUpdateEntity(spark, dto, now);
        Spark savedSpark = sparkRepository.save(spark);
        log.info("Partial updated Spark with id='{}'", savedSpark.getId());

        return SparkMapper.toSparkDto(savedSpark);
    }

    private void deleteCascade(String rootId) {

        log.debug("Deleting Spark subtree rooted at id='{}' with CASCADE mode", rootId);

        List<String> idsToDelete = new ArrayList<>();
        Deque<String> stack = new ArrayDeque<>();

        stack.push(rootId);

        while(!stack.isEmpty()) {
            String currentId = stack.pop();
            idsToDelete.add(currentId);

            List<Spark> children = sparkRepository.findByParentId(currentId);
            for(Spark child : children) {
                if(child.getId() != null) {
                    stack.push(child.getId());
                }
            }
        }

        sparkRepository.deleteAllById(idsToDelete);

        log.info("Deleted {} Spark(s) in CASCADE mode for rootId='{}'", idsToDelete.size(), rootId);
    }

    private void deletePromote(Spark spark) {

        String id = spark.getId();
        String parentId = spark.getParentId();
        Instant now = Instant.now();

        log.debug("Deleting Spark with id='{}' in PROMOTE mode. Reparenting children to parentId='{}'", id, parentId);

        List<Spark> children = sparkRepository.findByParentId(id);

        for (Spark child : children) {
            child.setParentId(parentId);
            child.setUpdatedAt(now);
        }

        if(!children.isEmpty()) {
            sparkRepository.saveAll(children);
            log.debug("Reparented {} child Spark(s) of id='{}' to parentId='{}'", children.size(), id, parentId);
        }

        sparkRepository.deleteById(id);

        log.info("Deleted Spark with id='{}' in PROMOTE mode", id);
    }

    @Override
    public void deleteSpark(String id, SparkDeleteMode mode) {

        Spark spark = sparkRepository.findById(id).orElseThrow(() -> {
            log.warn("Cannot find Spark with id='{}' to delete", id);
            return new SparkNotFoundException(id);
        });

        if(mode == SparkDeleteMode.CASCADE) {
            deleteCascade(id);
        } else if (mode == SparkDeleteMode.PROMOTE) {
            deletePromote(spark);
        } else {
            throw new IllegalArgumentException("Unsupported delete mode: " + mode);
        }
    }

    @Override
    public Page<SparkDTO> searchSparks(String title, String parentId, int page, int size) {

        if(page < 0) {
            page = 0;
        }
        if(size <=0) {
            size = 20;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Spark> sparksPage;

        boolean hasTitle = title != null && !title.isBlank();
        boolean hasParent = parentId != null && !parentId.isBlank();

        if(hasTitle && hasParent) {
            log.debug("Searching Sparks by parentId='{}' and title containing '{}', page={}, size={}",
                    parentId, title, page, size);
            sparksPage = sparkRepository.findByParentIdAndTitleContainingIgnoreCase(parentId, title, pageable);
        } else if (hasParent) {
            if("ROOT".equalsIgnoreCase(parentId)) {
                log.debug("Searching top-level Sparks (parentId null), page={}, size={}", page, size);
                sparksPage = sparkRepository.findByParentIdIsNull(pageable);
            } else {
                log.debug("Searching Sparks by parentId='{}', page={}, size={}", parentId, page, size);
                sparksPage = sparkRepository.findByParentId(parentId, pageable);
            }
        } else if (hasTitle) {
            log.debug("Searching Sparks by title containing '{}', page={}, size={}", title, page, size);
            sparksPage = sparkRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else {
            log.debug("Searching all Sparks, page={}, size={}", page, size);
            sparksPage = sparkRepository.findAll(pageable);
        }

        log.info("Found {} Sparks matching search criteria", sparksPage.getTotalElements());
        return sparksPage.map(SparkMapper::toSparkDto);
    }
}
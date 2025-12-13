package dev.ignitr.ignitrbackend.spark.service;

import dev.ignitr.ignitrbackend.common.utils.LoggingUtils;
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

import static dev.ignitr.ignitrbackend.common.utils.StringUtils.isNotNullOrEmpty;

@Service
public class SparkServiceImpl implements SparkService {

    private static final Logger logger = LoggerFactory.getLogger(SparkServiceImpl.class);

    private final SparkRepository sparkRepository;

    public SparkServiceImpl(SparkRepository sparkRepository) {
        this.sparkRepository = sparkRepository;
    }

    private void checkExistingTitle(String operation, String title) throws SparkAlreadyExistsException {
        if (sparkRepository.existsByTitle(title)) {
            SparkAlreadyExistsException exception = new SparkAlreadyExistsException(title);
            LoggingUtils.warn(logger, operation, "",
                    "Spark already exists", exception);
            throw exception;
        }
    }

    @Override
    public Spark saveSpark(Spark spark) {
        return sparkRepository.save(spark);
    }

    @Override
    public Spark createSpark(CreateSparkRequestDTO dto) {

        LoggingUtils.debug(logger, "createSpark", "",
                "Creating new Spark...");

        String title = dto.title();

        checkExistingTitle("createSpark", title);

        Instant now = Instant.now();
        Spark newSpark = SparkMapper.toNewEntity(dto, now);

        Spark savedSpark = saveSpark(newSpark);

        LoggingUtils.info(logger, "createSpark", savedSpark.getId(),
                "Spark created.");

        return savedSpark;
    }

    @Override
    public Spark createChildSpark(String parentId, CreateSparkRequestDTO dto) {

        String title = dto.title();

        LoggingUtils.debug(logger, "createChildSpark", parentId,
                "Creating child Spark with title='{}'...", title);

        Spark parent = getSparkById(parentId);

        checkExistingTitle("createChildSpark", title);

        Instant now = Instant.now();
        Spark childSpark = SparkMapper.toNewChildEntity(dto, parent.getId(),now);

        Spark savedSpark = saveSpark(childSpark);

        LoggingUtils.info(logger, "createChildSpark", savedSpark.getId(),
                "Created child Spark under parentId='{}'.", parent.getId());

        return savedSpark;
    }

    @Override
    public Spark getSparkById(String id) {

        LoggingUtils.debug(logger,
                "getSparkById", id,
                "Fetching Spark...");

        Spark spark = sparkRepository.findById(id).orElseThrow(() -> {
            SparkNotFoundException exception = new SparkNotFoundException(id);
            LoggingUtils.warn(logger,
                    "getSparkById", id,
                    "Spark not found.", exception);
            return exception;
        });

        LoggingUtils.info(logger,
                "getSparkById", spark.getId(),
                "Spark fetched.");

        return spark;
    }

    @Override
    public List<Spark> getChildren(String parentId) throws SparkNotFoundException {

        LoggingUtils.debug(logger, "getChildren", parentId,
                "Fetching children Sparks...");

        Spark parent = getSparkById(parentId);

        List<Spark> children = sparkRepository.findByParentId(parent.getId());

        LoggingUtils.info(logger, "getChildren", parent.getId(),
                "Found {} children Sparks.", children.size());

        return children;
    }

    @Override
    public List<Spark> getSparkTreeList(String rootId) {

        LoggingUtils.debug(logger, "getSparkTreeList", rootId,
                "Fetching Spark subtree...");

        Spark root = getSparkById(rootId);

        List<Spark> sparkList = new ArrayList<>();
        Deque<Spark> stack = new ArrayDeque<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            Spark current = stack.pop();
            sparkList.add(current);

            List<Spark> children = sparkRepository.findByParentId(current.getId());
            for (Spark child : children) {
                if(child != null) {
                    stack.push(child);
                }
            }
        }

        LoggingUtils.info(logger, "getSparkTreeList", root.getId(),
                "Fetched Spark subtree with {} Sparks.", sparkList.size());

        return sparkList;
    }

    @Override
    public Spark updateSpark(String id, UpdateSparkRequestDTO dto) {

        LoggingUtils.debug(logger, "updateSpark", id,
                "Updating Spark...");

        Spark spark = getSparkById(id);

        String newTitle = dto.title();

        if(!spark.getTitle().equals(newTitle)) {
            checkExistingTitle("updateSpark", newTitle);
        }
        Instant now = Instant.now();
        SparkMapper.updateEntity(spark, dto, now);
        Spark savedSpark = saveSpark(spark);
        LoggingUtils.info(logger, "updateSpark", savedSpark.getId(),
                "Spark updated.");

        return savedSpark;
    }

    @Override
    public Spark partialUpdateSpark(String id, PatchSparkRequestDTO dto) {

        LoggingUtils.debug(logger, "partialUpdateSpark", id,
                "Partially updating Spark...");

        Spark spark = getSparkById(id);

        String newTitle = dto.title();

        if(newTitle != null && !spark.getTitle().equals(newTitle)) {
            checkExistingTitle("partialUpdateSpark", newTitle);
        }

        Instant now = Instant.now();
        SparkMapper.partialUpdateEntity(spark, dto, now);
        Spark savedSpark = saveSpark(spark);
        LoggingUtils.info(logger, "partialUpdateSpark", savedSpark.getId(),
                "Spark partially updated.");

        return savedSpark;
    }

    private void deleteCascade(String rootId) {

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

        LoggingUtils.info(logger, "deleteCascade", rootId,
                "Deleted Spark subtree with {} Sparks in CASCADE mode.", idsToDelete.size());
    }

    private void deletePromote(Spark spark) {

        String id = spark.getId();
        String parentId = spark.getParentId();
        Instant now = Instant.now();

        List<Spark> children = sparkRepository.findByParentId(id);

        for (Spark child : children) {
            child.setParentId(parentId);
            child.setUpdatedAt(now);
        }

        if(!children.isEmpty()) {
            sparkRepository.saveAll(children);
            LoggingUtils.debug(logger, "deletePromote", id,
                    "Promoted {} children of deleted Spark to parentId='{}'.",
                    children.size(), parentId);
        }

        sparkRepository.deleteById(id);

        LoggingUtils.info(logger, "deletePromote", id,
                "Deleted Spark in PROMOTE mode.");
    }

    @Override
    public void deleteSpark(String id, SparkDeleteMode mode) {

        LoggingUtils.debug(logger, "deleteSpark", id,
                "Deleting Spark in '{}' mode...", mode);

        Spark spark = getSparkById(id);

        if(mode == SparkDeleteMode.CASCADE) {
            deleteCascade(id);
        } else if (mode == SparkDeleteMode.PROMOTE) {
            deletePromote(spark);
        } else {
            throw new IllegalArgumentException("Unsupported delete mode: " + mode);
        }
    }

    @Override
    public Page<Spark> searchSparks(String title, String parentId, int page, int size) {

        if(page < 0) {
            page = 0;
        }
        if(size <=0) {
            size = 20;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        Page<Spark> sparksPage;

        boolean hasTitle = isNotNullOrEmpty(title);
        boolean hasParent = isNotNullOrEmpty(parentId);

        LoggingUtils.debug(logger, "searchSparks", "",
                "Searching Sparks with criteria: [{}, page={}, size={}]...",
                String.format("title='%s', parentId='%s'", title, parentId), page, size);

        if(hasTitle && hasParent) {
            sparksPage = sparkRepository.findByParentIdAndTitleContainingIgnoreCase(parentId, title, pageable);
        } else if (hasParent) {
            if("ROOT".equalsIgnoreCase(parentId)) {
                sparksPage = sparkRepository.findByParentIdIsNull(pageable);
            } else {
                sparksPage = sparkRepository.findByParentId(parentId, pageable);
            }
        } else if (hasTitle) {
            sparksPage = sparkRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else {
            sparksPage = sparkRepository.findAll(pageable);
        }

        LoggingUtils.info(logger, "searchSparks", "",
                "Found {} Sparks matching criteria: [{}, page={}, size={}].",
                sparksPage.getTotalElements(),
                String.format("title='%s', parentId='%s'", title, parentId), page, size);

        return sparksPage;
    }
}
package dev.ignitr.ignitrbackend.spark.service;

import dev.ignitr.ignitrbackend.common.utils.LoggingUtils;
import dev.ignitr.ignitrbackend.score.exception.ScoringException;
import dev.ignitr.ignitrbackend.score.service.SparkScoreService;
import dev.ignitr.ignitrbackend.spark.exception.SparkAlreadyExistsException;
import dev.ignitr.ignitrbackend.spark.exception.SparkNotFoundException;
import dev.ignitr.ignitrbackend.spark.mapper.SparkMapper;
import dev.ignitr.ignitrbackend.spark.model.Spark;
import dev.ignitr.ignitrbackend.spark.repository.SparkRepository;

import dev.ignitr.ignitrbackend.spark.tree.SparkTree;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

import static dev.ignitr.ignitrbackend.common.utils.StringUtils.isNotNullOrEmpty;

@Service
public class SparkServiceImpl implements SparkService {

    private static final Logger logger = LoggerFactory.getLogger(SparkServiceImpl.class);

    private final SparkScoreService sparkScoreService;
    private final SparkRepository sparkRepository;

    public SparkServiceImpl(SparkRepository sparkRepository, SparkScoreService sparkScoreService) {
        this.sparkScoreService = sparkScoreService;
        this.sparkRepository = sparkRepository;
    }

    private void checkExistingTitle(String operation, String title) throws SparkAlreadyExistsException {
        if (sparkRepository.existsByTitle(title)) {
            SparkAlreadyExistsException exception = new SparkAlreadyExistsException(title);
            LoggingUtils.warn(logger, operation, null,
                    "Spark already exists", exception);
            throw exception;
        }
    }

    private Map<ObjectId, Spark> getSparkMap(List<Spark> rootSparks) {

        Map<ObjectId, Spark> sparkMap = new HashMap<>();
        for(Spark root : rootSparks) {
            if (root != null) {
                sparkMap.put(root.getId(), root);
            }
        }

        List<ObjectId> currentLevel = rootSparks.stream().map(Spark::getId)
                .toList();

        while (!currentLevel.isEmpty()) {
            List<Spark> children = sparkRepository.findByParentIdIn(currentLevel);

            if (children.isEmpty()) break;

            List<ObjectId> nextLevel = new ArrayList<>(children.size());

            for (Spark child : children) {
                if (child == null) continue;
                if (sparkMap.putIfAbsent(child.getId(), child) == null) {
                    nextLevel.add(child.getId());
                }
            }

            currentLevel = nextLevel;
        }

        return sparkMap;
    }

    @Override
    public Spark saveSpark(Spark spark) {
        return sparkRepository.save(spark);
    }

    @Override
    public Spark createSpark(String title, String description) {

        LoggingUtils.debug(logger, "createSpark", null,
                "Creating new Spark...");

        checkExistingTitle("createSpark", title);

        Instant now = Instant.now();
        Spark newSpark = SparkMapper.toNewEntity(title, description, now);

        Spark savedSpark = saveSpark(newSpark);

        LoggingUtils.info(logger, "createSpark", savedSpark.getId(),
                "Spark created.");

        return savedSpark;
    }

    @Override
    public Spark createChildSpark(ObjectId parentId, String title, String description) {

        LoggingUtils.debug(logger, "createChildSpark", parentId,
                "Creating child Spark with title='{}'...", title);

        Spark parent = getSparkById(parentId);

        checkExistingTitle("createChildSpark", title);

        Instant now = Instant.now();
        Spark childSpark = SparkMapper.toNewChildEntity(title, description, parent.getId(),now);

        Spark savedSpark = saveSpark(childSpark);

        LoggingUtils.info(logger, "createChildSpark", savedSpark.getId(),
                "Created child Spark under parentId='{}'.", parent.getId());

        return savedSpark;
    }

    @Override
    public Spark getSparkById(ObjectId id) {

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
    public List<Spark> getChildren(ObjectId parentId) {

        LoggingUtils.debug(logger, "getChildren", parentId,
                "Fetching children Sparks...");

        Spark parent = getSparkById(parentId);

        List<Spark> children = sparkRepository.findByParentId(parent.getId());

        LoggingUtils.info(logger, "getChildren", parent.getId(),
                "Found {} children Sparks.", children.size());

        return children;
    }

    @Override
    public SparkTree getSparkTree(ObjectId rootId) {

        LoggingUtils.debug(logger, "getSparkTree", rootId,
                "Fetching Spark subtree...");

        Spark root = getSparkById(rootId);

        Map<ObjectId, Spark> sparkMap = getSparkMap(List.of(root));

        LoggingUtils.info(logger, "getSparkTree", root.getId(),
                "Fetched Spark subtree with {} Sparks.", sparkMap.size());

        try {
            return sparkScoreService.scoreTree(root.getId(), sparkMap);
        } catch (ScoringException e) {
            LoggingUtils.warn(logger, "getSparkTree", root.getId(),
                    "Error scoring Spark tree, returning unscored tree.", e);
            return SparkMapper.toSparkTree(sparkMap, root.getId());
        }
    }

    @Override
    public Page<SparkTree> getSparkTrees(ObjectId parentId, int page, int size) {

        LoggingUtils.debug(logger, "getSparkTrees", parentId,
                "Fetching paged Spark trees...");

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        Page<Spark> rootPage = (parentId != null)
                ? sparkRepository.findByParentId(parentId, pageable)
                : sparkRepository.findByParentIdIsNull(pageable);

        if(rootPage == null) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<Spark> rootList = rootPage.getContent();
        if (rootList.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, rootPage.getTotalElements());
        }

        Map<ObjectId, Spark> sparkMap = getSparkMap(rootList);

        List<ObjectId> rootIds = rootList.stream().map(Spark::getId).toList();

        try {

            List<SparkTree> scoredTrees = sparkScoreService.scoreTrees(rootIds, sparkMap);

            return new PageImpl<>(scoredTrees, pageable, rootPage.getTotalElements());

        } catch (ScoringException e) {
            LoggingUtils.warn(logger, "getSparkTrees", parentId,
                    "Error scoring Spark trees, returning unscored trees.", e);

            List<SparkTree> sparkTrees = SparkMapper.toSparkTreeList(
                    sparkMap, rootIds
            );

            return new PageImpl<>(sparkTrees, pageable, rootPage.getTotalElements());
        }
    }

    @Override
    public Spark updateSpark(ObjectId id, String title, String description) {

        LoggingUtils.debug(logger, "updateSpark", id,
                "Updating Spark...");

        Spark spark = getSparkById(id);

        if(!spark.getTitle().equals(title)) {
            checkExistingTitle("updateSpark", title);
        }
        Instant now = Instant.now();
        SparkMapper.updateEntity(spark, title, description, now);
        Spark savedSpark = saveSpark(spark);
        LoggingUtils.info(logger, "updateSpark", savedSpark.getId(),
                "Spark updated.");

        return savedSpark;
    }

    @Override
    public Spark partialUpdateSpark(ObjectId id, String title, String description) {

        LoggingUtils.debug(logger, "partialUpdateSpark", id,
                "Partially updating Spark...");

        Spark spark = getSparkById(id);

        if(title != null && !spark.getTitle().equals(title)) {
            checkExistingTitle("partialUpdateSpark", title);
        }

        Instant now = Instant.now();
        SparkMapper.partialUpdateEntity(spark, title, description, now);
        Spark savedSpark = saveSpark(spark);
        LoggingUtils.info(logger, "partialUpdateSpark", savedSpark.getId(),
                "Spark partially updated.");

        return savedSpark;
    }

    private void deleteCascade(ObjectId rootId) {

        List<ObjectId> idsToDelete = new ArrayList<>();
        Deque<ObjectId> stack = new ArrayDeque<>();

        stack.push(rootId);

        while(!stack.isEmpty()) {
            ObjectId currentId = stack.pop();
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

        ObjectId id = spark.getId();
        ObjectId parentId = spark.getParentId();
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
    public void deleteSpark(ObjectId id, SparkDeleteMode mode) {

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
    public Page<Spark> searchSparks(String title, ParentSearchScope parentScope, ObjectId parentId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        boolean byTitle = isNotNullOrEmpty(title);

        LoggingUtils.debug(logger, "searchSparks", null,
                "Searching Sparks with criteria: [title='{}', parentScope={}, parentId={}, page={}, size={}]...",
                title, parentScope, parentId, page, size);

        Page<Spark> sparksPage;

        if (byTitle) {
            sparksPage = switch (parentScope) {
                case ANY  -> sparkRepository.findByTitleContainingIgnoreCase(title, pageable);
                case ROOT -> sparkRepository.findByParentIdIsNullAndTitleContainingIgnoreCase(title, pageable);
                case ID   -> sparkRepository.findByParentIdAndTitleContainingIgnoreCase(parentId, title, pageable);
            };
        } else {
            sparksPage = switch (parentScope) {
                case ANY  -> sparkRepository.findAll(pageable);
                case ROOT -> sparkRepository.findByParentIdIsNull(pageable);
                case ID   -> sparkRepository.findByParentId(parentId, pageable);
            };
        }

        LoggingUtils.info(logger, "searchSparks", null,
                "Found {} Sparks matching criteria: [title='{}', parentScope={}, parentId={}, page={}, size={}].",
                sparksPage.getTotalElements(), title, parentScope, parentId, page, size);

        return sparksPage;
    }
}
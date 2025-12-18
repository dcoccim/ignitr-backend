package dev.ignitr.ignitrbackend.score.service;

import dev.ignitr.ignitrbackend.common.utils.LoggingUtils;
import dev.ignitr.ignitrbackend.score.client.ScoringServiceClient;
import dev.ignitr.ignitrbackend.score.tree.ScoredSparkTree;
import dev.ignitr.ignitrbackend.spark.model.Spark;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SparkScoreServiceImpl implements SparkScoreService {

    private final Logger logger = LoggerFactory.getLogger(SparkScoreServiceImpl.class);

    private final ScoringServiceClient scoringServiceClient;

    public SparkScoreServiceImpl(ScoringServiceClient scoringServiceClient) {
        this.scoringServiceClient = scoringServiceClient;
    }

    @Override
    public ScoredSparkTree scoreTree(ObjectId rootId,
                                      Map<ObjectId, Spark> sparkMap) {
        LoggingUtils.debug(logger, "scoreTree", rootId, "Scoring Spark tree...");
        ScoredSparkTree scoredTree = scoringServiceClient.postSparkTreeScore(sparkMap, rootId);
        LoggingUtils.info(logger, "scoreTree", rootId, "Spark tree scored.");
        return scoredTree;
    }
}

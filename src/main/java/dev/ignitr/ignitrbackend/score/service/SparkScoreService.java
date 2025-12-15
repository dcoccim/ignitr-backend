package dev.ignitr.ignitrbackend.score.service;

import dev.ignitr.ignitrbackend.common.utils.LoggingUtils;
import dev.ignitr.ignitrbackend.score.client.ScoringServiceClient;
import dev.ignitr.ignitrbackend.score.dto.SparkScoreRequestDTO;
import dev.ignitr.ignitrbackend.score.dto.SparkTreeScoreResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SparkScoreService {

    private final Logger logger = LoggerFactory.getLogger(SparkScoreService.class);

    private final ScoringServiceClient scoringServiceClient;

    public SparkScoreService(ScoringServiceClient scoringServiceClient) {
        this.scoringServiceClient = scoringServiceClient;
    }

    public SparkTreeScoreResponseDTO scoreTree (String rootId,
                                               Map<String, SparkScoreRequestDTO> request) {
        LoggingUtils.debug(logger, "scoreTree", rootId, "Scoring Spark tree...");
        SparkTreeScoreResponseDTO response =
                scoringServiceClient.postSparkTreeScore(request, rootId);
        LoggingUtils.info(logger, "scoreTree", rootId, "Spark tree scored.");
        return response;
    }
}

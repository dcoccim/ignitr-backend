package dev.ignitr.ignitrbackend.score.client;

import dev.ignitr.ignitrbackend.score.dto.SparkScoreRequestDTO;
import dev.ignitr.ignitrbackend.score.dto.SparkTreeScoreResponseDTO;
import dev.ignitr.ignitrbackend.score.exception.ScoringException;
import dev.ignitr.ignitrbackend.score.mapper.SparkScoreMapper;
import dev.ignitr.ignitrbackend.score.tree.ScoredSparkTree;
import dev.ignitr.ignitrbackend.spark.model.Spark;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class ScoringServiceClient {

    private final RestClient scoringRestClient;

    public ScoringServiceClient(@Qualifier("scoringRestClient") RestClient scoringRestClient) {
        this.scoringRestClient = scoringRestClient;
    }

    public ScoredSparkTree postSparkTreeScore(
            Map<ObjectId, Spark> sparkMap,
            ObjectId rootId
    ) {
        try {
            Map<String, SparkScoreRequestDTO> request = SparkScoreMapper.toDtoMap(sparkMap);
            SparkTreeScoreResponseDTO response = scoringRestClient
                    .post()
                    .uri("/score/{rootId}", rootId.toHexString())
                    .body(request)
                    .retrieve()
                    .body(SparkTreeScoreResponseDTO.class);
            return SparkScoreMapper.toScoredSparkTree(sparkMap, response);
        } catch (Exception e) {
            throw new ScoringException(rootId);
        }
    }
}

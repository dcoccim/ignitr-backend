package dev.ignitr.ignitrbackend.score.client;

import dev.ignitr.ignitrbackend.score.dto.SparkScoreRequestDTO;
import dev.ignitr.ignitrbackend.score.dto.SparkTreeScoreResponseDTO;
import dev.ignitr.ignitrbackend.score.exception.ScoringException;
import dev.ignitr.ignitrbackend.score.mapper.SparkScoreMapper;
import dev.ignitr.ignitrbackend.spark.model.Spark;
import dev.ignitr.ignitrbackend.spark.tree.SparkTree;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class ScoringServiceClient {

    private final RestClient scoringRestClient;

    public ScoringServiceClient(@Qualifier("scoringRestClient") RestClient scoringRestClient) {
        this.scoringRestClient = scoringRestClient;
    }

    public SparkTree postSparkScoreTree(
            Map<ObjectId, Spark> sparkMap,
            ObjectId rootId
    ) {
        try {
            Map<String, SparkScoreRequestDTO> request = SparkScoreMapper.toDtoMap(sparkMap);
            var response = scoringRestClient
                    .post()
                    .uri("/{rootId}/scoreTree", rootId.toHexString())
                    .body(request)
                    .retrieve()
                    .body(SparkTreeScoreResponseDTO.class);
            return SparkScoreMapper.toScoredSparkTree(sparkMap, response);
        } catch (Exception e) {
            throw new ScoringException("Error on scorer service invocation", rootId, e);
        }
    }

    public List<SparkTree> postSparkScoreTrees(
            Map<ObjectId, Spark> sparkMap,
            List<ObjectId> rootIds
    ) {
        try {
            Map<String, SparkScoreRequestDTO> request = SparkScoreMapper.toDtoMap(sparkMap);
            var response = scoringRestClient
                    .post()
                    .uri("/scoreTrees")
                    .body(Map.of(
                            "rootIds", rootIds.stream().map(ObjectId::toHexString).toList(),
                            "sparks", request
                    ))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<SparkTreeScoreResponseDTO>>() {});
            if(response == null) {
                throw new ScoringException("Null response from scorer server.", rootIds);
            }
            return SparkScoreMapper.toScoredSparkTrees(sparkMap, response);
        } catch (Exception e) {
            throw new ScoringException("Error on scorer server invocation", rootIds, e);
        }
    }
}

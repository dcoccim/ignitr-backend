package dev.ignitr.ignitrbackend.score.client;

import dev.ignitr.ignitrbackend.score.dto.SparkScoreRequestDTO;
import dev.ignitr.ignitrbackend.score.dto.SparkTreeScoreResponseDTO;
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

    public SparkTreeScoreResponseDTO postSparkTreeScore(
            Map<String, SparkScoreRequestDTO> request,
            String rootId
    ) {
        return scoringRestClient
                .post()
                .uri("/score/{rootId}", rootId)
                .body(request)
                .retrieve()
                .body(SparkTreeScoreResponseDTO.class);
    }
}

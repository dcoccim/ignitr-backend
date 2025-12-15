package dev.ignitr.ignitrbackend.score.client;

import dev.ignitr.ignitrbackend.score.dto.SparkScoreRequestDTO;
import dev.ignitr.ignitrbackend.score.dto.SparkTreeScoreResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class ScoringServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ScoringServiceClient(
            RestTemplate restTemplate,
            @Value("${scoring.service.base-url}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public SparkTreeScoreResponseDTO postSparkTreeScore(Map<String, SparkScoreRequestDTO> request, String rootId) {
        String url = String.format("%s/score/%s", baseUrl, rootId);
        return restTemplate.postForObject(url, request, SparkTreeScoreResponseDTO.class);
    }
}

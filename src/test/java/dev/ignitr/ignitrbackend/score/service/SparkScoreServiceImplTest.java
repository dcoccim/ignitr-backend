package dev.ignitr.ignitrbackend.score.service;

import dev.ignitr.ignitrbackend.score.client.ScoringServiceClient;
import dev.ignitr.ignitrbackend.score.exception.ScoringException;
import dev.ignitr.ignitrbackend.score.tree.ScoredSparkTree;
import dev.ignitr.ignitrbackend.spark.model.Spark;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SparkScoreServiceImplTest {

    @Mock
    private ScoringServiceClient scoringServiceClient;

    private SparkScoreService sparkScoreService;

    @BeforeEach
    void setUp() {
        sparkScoreService = new SparkScoreServiceImpl(scoringServiceClient);
    }

    @Test
    void scoreTree_returnsScoredTree_whenClientSucceeds() {

        ObjectId rootId = new ObjectId();
        Instant now = Instant.now();
        Spark spark = new Spark(rootId, "Root", "Desc", null, List.of(), now, now);
        Map<ObjectId, Spark> sparkMap = new HashMap<>();
        sparkMap.put(rootId, spark);

        ScoredSparkTree expected = new ScoredSparkTree(
                rootId,
                "Root",
                "Desc",
                null,
                1,
                0,
                new ArrayList<>(),
                now,
                now,
                10
        );

        when(scoringServiceClient.postSparkTreeScore(sparkMap, rootId)).thenReturn(expected);

        ScoredSparkTree result = sparkScoreService.scoreTree(rootId, sparkMap);

        assertThat(result).isSameAs(expected);
        verify(scoringServiceClient).postSparkTreeScore(sparkMap, rootId);
    }

    @Test
    void scoreTree_propagatesException_whenClientFails() {

        ObjectId rootId = new ObjectId();
        Map<ObjectId, Spark> sparkMap = Map.of(rootId, new Spark());

        when(scoringServiceClient.postSparkTreeScore(sparkMap, rootId))
                .thenThrow(new ScoringException(rootId));

        assertThatThrownBy(() -> sparkScoreService.scoreTree(rootId, sparkMap))
                .isInstanceOf(ScoringException.class)
                .hasMessageContaining(rootId.toHexString());

        verify(scoringServiceClient).postSparkTreeScore(sparkMap, rootId);
    }
}

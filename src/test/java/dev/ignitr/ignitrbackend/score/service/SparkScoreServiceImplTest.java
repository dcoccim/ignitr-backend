package dev.ignitr.ignitrbackend.score.service;

import dev.ignitr.ignitrbackend.score.client.ScoringServiceClient;
import dev.ignitr.ignitrbackend.score.exception.ScoringException;
import dev.ignitr.ignitrbackend.spark.model.Spark;
import dev.ignitr.ignitrbackend.spark.tree.SparkTree;
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

        SparkTree expected = SparkTree.fromSpark(spark, 1, 2, 1, new ArrayList<>());

        when(scoringServiceClient.postSparkScoreTree(sparkMap, rootId)).thenReturn(expected);

        SparkTree result = sparkScoreService.scoreTree(rootId, sparkMap);

        assertThat(result).isSameAs(expected);
        verify(scoringServiceClient).postSparkScoreTree(sparkMap, rootId);
    }

    @Test
    void scoreTree_propagatesException_whenClientFails() {

        ObjectId rootId = new ObjectId();
        Map<ObjectId, Spark> sparkMap = Map.of(rootId, new Spark());

        when(scoringServiceClient.postSparkScoreTree(sparkMap, rootId))
                .thenThrow(new ScoringException(rootId));

        assertThatThrownBy(() -> sparkScoreService.scoreTree(rootId, sparkMap))
                .isInstanceOf(ScoringException.class)
                .hasMessageContaining(rootId.toHexString());

        verify(scoringServiceClient).postSparkScoreTree(sparkMap, rootId);
    }

    @Test
    void scoreTrees_returnsScoredTrees_whenClientSucceeds() {

        ObjectId sparkId1 = new ObjectId();
        ObjectId sparkId2 = new ObjectId();
        Instant now = Instant.now();
        Spark spark1 = new Spark(sparkId1, "Spark 1", "Desc 1", null, List.of(), Instant.now(), now);
        Spark spark2 = new Spark(sparkId2, "Spark 2", "Desc 2", null, List.of(), Instant.now(), now);

        SparkTree sparkTree1 = SparkTree.fromSpark(spark1, 1, 1, 0, new ArrayList<>());
        SparkTree sparkTree2 = SparkTree.fromSpark(spark2, 1, 2, 1, new ArrayList<>());

        List<SparkTree> expected = List.of(sparkTree1, sparkTree2);

        Map <ObjectId, Spark> sparkMap = new HashMap<>();
        List<ObjectId> rootIds = List.of(sparkId1, sparkId2);
        sparkMap.put(sparkId1, spark1);
        sparkMap.put(sparkId2, spark2);
        when(scoringServiceClient.postSparkScoreTrees(sparkMap, rootIds)).thenReturn(expected);

        List<SparkTree> result = sparkScoreService.scoreTrees(rootIds, sparkMap);

        assertThat(result).isSameAs(expected);
        verify(scoringServiceClient).postSparkScoreTrees(sparkMap, rootIds);
    }

    @Test
    void scoreTrees_propagatesException_whenClientFails() {

        ObjectId root1Id = new ObjectId();
        ObjectId root2Id = new ObjectId();

        Map<ObjectId, Spark> sparkMap = new HashMap<>();
        sparkMap.put(root1Id, new Spark());
        sparkMap.put(root2Id, new Spark());
        List<ObjectId> rootIds = List.of(root1Id, root2Id);

        when(scoringServiceClient.postSparkScoreTrees(sparkMap, rootIds))
                .thenThrow(new ScoringException(rootIds));

        assertThatThrownBy(() -> sparkScoreService.scoreTrees(rootIds, sparkMap))
                .isInstanceOf(ScoringException.class)
                .hasMessageContaining(root1Id.toHexString())
                .hasMessageContaining(root2Id.toHexString());

        verify(scoringServiceClient).postSparkScoreTrees(sparkMap, rootIds);
    }
}

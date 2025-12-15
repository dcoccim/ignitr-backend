package dev.ignitr.ignitrbackend.score.mapper;

import dev.ignitr.ignitrbackend.reason.model.ReasonType;
import dev.ignitr.ignitrbackend.score.dto.SparkScoreRequestDTO;
import dev.ignitr.ignitrbackend.score.dto.SparkTreeScoreResponseDTO;
import dev.ignitr.ignitrbackend.score.tree.ScoredSparkTree;
import dev.ignitr.ignitrbackend.spark.model.Spark;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SparkScoreMapper {

    private SparkScoreMapper() {}

    public static Map<String, SparkScoreRequestDTO> toDtoMap(Map<String, Spark> sparkMap) {
        return sparkMap.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> {
                            Spark s = e.getValue();
                            List<SparkScoreRequestDTO.ReasonScoreRequestDTO> reasonsDtoList =
                                    s.getReasons() != null
                                            ? s.getReasons().stream()
                                            .map(r -> new SparkScoreRequestDTO.ReasonScoreRequestDTO(r.getType(), r.getVotes()))
                                            .toList()
                                            : new ArrayList<>();
                            String id = s.getId() != null ? s.getId() : e.getKey();
                            return new SparkScoreRequestDTO(id, s.getParentId(), reasonsDtoList);
                        }
                ));
    }

    public static ScoredSparkTree toScoredSparkTree(Map<String, Spark> sparkMap, SparkTreeScoreResponseDTO dto) {
        return mapNode(sparkMap, dto);
    }

    private static ScoredSparkTree mapNode(Map<String, Spark> sparkMap, SparkTreeScoreResponseDTO dto) {

        Spark spark = sparkMap.get(dto.id());

        if(spark == null) {
            throw new IllegalArgumentException("Spark not found for id: " + dto.id());
        }

        int goodReasonsCount = 0;
        int badReasonsCount = 0;

        for(var reason : spark.getReasons()) {
            if(reason.getType() == ReasonType.GOOD){
                goodReasonsCount++;
            } else if (reason.getType() == ReasonType.BAD){
                badReasonsCount++;
            }
        }

        ScoredSparkTree node = ScoredSparkTree.fromSpark(
                spark,
                goodReasonsCount,
                badReasonsCount,
                new ArrayList<>(),
                dto.score()
        );

        if (dto.children() != null && !dto.children().isEmpty()) {
            List<ScoredSparkTree> children = dto.children().stream()
                    .map(childDto -> mapNode(sparkMap, childDto))
                    .toList();
            node.getChildren().addAll(children);
        }

        return node;
    }
}

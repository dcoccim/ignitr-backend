package dev.ignitr.ignitrbackend.score.mapper;

import dev.ignitr.ignitrbackend.reason.model.ReasonType;
import dev.ignitr.ignitrbackend.score.dto.SparkScoreRequestDTO;
import dev.ignitr.ignitrbackend.score.dto.SparkTreeScoreResponseDTO;
import dev.ignitr.ignitrbackend.spark.model.Spark;
import dev.ignitr.ignitrbackend.spark.tree.SparkTree;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SparkScoreMapper {

    private SparkScoreMapper() {}

    public static Map<String, SparkScoreRequestDTO> toDtoMap(Map<ObjectId, Spark> sparkMap) {
        return sparkMap.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(
                        e -> e.getKey().toHexString(),
                        e -> {
                            Spark s = e.getValue();
                            List<SparkScoreRequestDTO.ReasonScoreRequestDTO> reasonsDtoList =
                                    s.getReasons() != null
                                            ? s.getReasons().stream()
                                            .map(r -> new SparkScoreRequestDTO.ReasonScoreRequestDTO(r.getType(), r.getVotes()))
                                            .toList()
                                            : new ArrayList<>();
                            String id = s.getId() != null ? s.getId().toHexString() : e.getKey().toHexString();
                            return new SparkScoreRequestDTO(id, s.getParentId() != null ? s.getParentId().toHexString() : null, reasonsDtoList, s.getCreatedAt());
                        }
                ));
    }

    public static SparkTree toScoredSparkTree(Map<ObjectId, Spark> sparkMap, SparkTreeScoreResponseDTO dto) {
        return mapNode(sparkMap, dto);
    }

    public static List<SparkTree> toScoredSparkTrees(Map<ObjectId, Spark> sparkMap, List<SparkTreeScoreResponseDTO> dtoList) {
        return dtoList.stream()
                .map(dto -> mapNode(sparkMap, dto))
                .toList();
    }

    private static SparkTree mapNode(Map<ObjectId, Spark> sparkMap, SparkTreeScoreResponseDTO dto) {

        Spark spark = sparkMap.get(new ObjectId(dto.id()));

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

        SparkTree node = SparkTree.fromSpark(
                spark,
                dto.score(),
                goodReasonsCount,
                badReasonsCount,
                new ArrayList<>()
        );

        if (dto.children() != null && !dto.children().isEmpty()) {
            List<SparkTree> children = dto.children().stream()
                    .map(childDto -> mapNode(sparkMap, childDto))
                    .toList();
            node.getChildren().addAll(children);
        }

        return node;
    }
}

package dev.ignitr.ignitrbackend.spark.mapper;

import dev.ignitr.ignitrbackend.reason.model.Reason;
import dev.ignitr.ignitrbackend.reason.model.ReasonType;
import dev.ignitr.ignitrbackend.spark.dto.SparkTreeDTO;
import dev.ignitr.ignitrbackend.spark.model.Spark;
import dev.ignitr.ignitrbackend.spark.tree.SparkTree;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class SparkMapperTest {

    @Test
    void toSparkTree_countsReasonsCorrectly() {
        Instant now = Instant.now();
        ObjectId rootId = new ObjectId();
        Spark root = new Spark(rootId, "Root", "Desc", null, new ArrayList<>(), now, now);
        root.getReasons().add(new Reason(ReasonType.GOOD, "good", now, now));
        root.getReasons().add(new Reason(ReasonType.BAD, "bad", now, now));
        root.getReasons().add(new Reason(ReasonType.GOOD, "good-2", now, now));

        Map<ObjectId, Spark> map = Map.of(rootId, root);

        SparkTree tree = SparkMapper.toSparkTree(map, rootId);

        assertThat(tree).isNotNull();
        assertThat(tree.getGoodReasonsCount()).isEqualTo(2);
        assertThat(tree.getBadReasonsCount()).isEqualTo(1);
    }

    @Test
    void toSparkTreeDto_respectsDepthAndChildLimits() {
        Instant now = Instant.now();
        ObjectId rootId = new ObjectId();
        ObjectId child1Id = new ObjectId();
        ObjectId child2Id = new ObjectId();
        ObjectId grandchildId = new ObjectId();

        Spark root = new Spark(rootId, "Root", "Desc", null, List.of(), now, now);
        Spark child1 = new Spark(child1Id, "Child1", "Desc", rootId, List.of(), now, now);
        Spark child2 = new Spark(child2Id, "Child2", "Desc", rootId, List.of(), now, now);
        Spark grandchild = new Spark(grandchildId, "Grand", "Desc", child1Id, List.of(), now, now);

        Map<ObjectId, Spark> map = new LinkedHashMap<>();
        map.put(rootId, root);
        map.put(child1Id, child1);
        map.put(child2Id, child2);
        map.put(grandchildId, grandchild);

        SparkTree tree = SparkMapper.toSparkTree(map, rootId);

        SparkTreeDTO dto = SparkMapper.toSparkTreeDto(tree, 1, 1);

        assertThat(dto.children()).hasSize(1);
        SparkTreeDTO keptChild = dto.children().getFirst();
        assertThat(keptChild.id()).isEqualTo(child1Id.toHexString());
        assertThat(keptChild.children()).isEmpty();
        assertThat(dto.depthBelow()).isEqualTo(2);
        assertThat(keptChild.depthBelow()).isEqualTo(1);
    }

    @Test
    void toSparkTreeDtoList_computesPerNodeChildrenCounts() {
        Instant now = Instant.now();
        ObjectId rootId = new ObjectId();
        ObjectId child1Id = new ObjectId();
        ObjectId child2Id = new ObjectId();
        Spark root = new Spark(rootId, "Root", "Desc", null, List.of(), now, now);
        Spark child1 = new Spark(child1Id, "Child1", "Desc", rootId, List.of(), now, now);
        Spark child2 = new Spark(child2Id, "Child2", "Desc", rootId, List.of(), now, now);

        Map<ObjectId, Spark> map = Map.of(
                rootId, root,
                child1Id, child1,
                child2Id, child2
        );

        SparkTree tree = SparkMapper.toSparkTree(map, rootId);

        assertThat(tree).isNotNull();

        List<SparkTreeDTO> dtos = SparkMapper.toSparkTreeDtoList(List.of(tree), 2, 5);

        assertThat(dtos).hasSize(1);
        SparkTreeDTO dto = dtos.getFirst();
        assertThat(dto.childrenCount()).isEqualTo(2);
        assertThat(dto.children()).hasSize(2);
        assertThat(dto.children().stream().map(SparkTreeDTO::childrenCount)).allMatch(count -> count == 0);
    }
}

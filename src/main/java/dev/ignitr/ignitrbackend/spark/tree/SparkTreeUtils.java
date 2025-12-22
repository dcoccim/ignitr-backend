package dev.ignitr.ignitrbackend.spark.tree;

import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SparkTreeUtils {

    private SparkTreeUtils() {}

    public static Map<ObjectId, Integer> computeSubtreeDepth(SparkTree root) {
        Map<ObjectId, Integer> out = new HashMap<>();
        dfsDepth(root, out);
        return out;
    }

    public static Map<ObjectId, Integer> computeSubtreeDepth(List<SparkTree> roots) {
        Map<ObjectId, Integer> out = new HashMap<>();
        for (SparkTree root : roots) {
            dfsDepth(root, out);
        }
        return out;
    }

    private static int dfsDepth(SparkTree node, Map<ObjectId, Integer> out) {
        List<SparkTree> children =
                node.getChildren() != null ? node.getChildren() : List.of();

        int best = 0;
        for (SparkTree c : children) {
            int d = 1 + dfsDepth(c, out);
            if (d > best) best = d;
        }

        out.put(node.getId(), best);
        return best;
    }

}

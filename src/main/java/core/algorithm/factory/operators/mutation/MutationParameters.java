package core.algorithm.factory.operators.mutation;
import java.util.List;
import java.util.Map;

public class MutationParameters {
    public String operator;
    public Double probability;
    public Double distributionIndex;
    public Double delta;
    public String alphabet;
    public Double perturbation;
    public Integer maxIterations;
    public GroupingParameters grouping;
    // Only used for CompositeMutation
    public List<Map<String, Object>> mutations;

    public static class GroupingParameters {
        public String type; // ListLinearGrouping, ListOrderedGrouping
        public Integer numberOfGroups;
    }
}

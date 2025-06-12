package core.algorithm.factory.operators;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.comparator.*;
import org.uma.jmetal.util.comparator.constraintcomparator.impl.NumberOfViolatedConstraintsComparator;
import org.uma.jmetal.util.comparator.constraintcomparator.impl.OverallConstraintViolationDegreeComparator;
import org.uma.jmetal.util.comparator.dominanceComparator.impl.DefaultDominanceComparator;
import org.uma.jmetal.util.comparator.dominanceComparator.impl.DominanceWithConstraintsComparator;
import org.uma.jmetal.util.comparator.dominanceComparator.impl.EpsilonDominanceComparator;

import java.util.Comparator;

public class ComparatorFactory {
    public static Comparator<?> create(String name) {
        // DoubleSolution comparators
        if ("DoubleVariable".equals(name)) {
            return new DoubleVariableComparator();
        }

        // IntegerSolution comparators
        if ("IntegerVariable".equals(name)) {
            return new IntegerVariableComparator();
        }

        // double[] comparators
        if ("LexicographicalVector".equals(name)) {
            return new LexicographicalVectorComparator();
        }

        // Generic Solution<?> comparators
        return switch (name) {
            case "EqualSolutions" -> new EqualSolutionsComparator<>();
            case "Fitness" -> new FitnessComparator<>();
            case "HypervolumeContribution" -> new HypervolumeContributionComparator<>();
            case "RankingAndCrowdingDistance" -> new RankingAndCrowdingDistanceComparator<>();
            case "RankingAndDirScoreDistance" -> new RankingAndDirScoreDistanceComparator<>();
            case "RankingAndSSD" -> new RankingAndSSDComparator<>();
            case "SpatialSpreadDeviation" -> new SpatialSpreadDeviationComparator<>();
            case "DefaultDominance" -> new DefaultDominanceComparator<>();
            case "DominanceWithConstraints" -> new DominanceWithConstraintsComparator<>();
            case "EpsilonDominanceComparator" -> new EpsilonDominanceComparator<>();
            case "NumberOfViolatedConstraints" -> new NumberOfViolatedConstraintsComparator<>();
            case "OverallConstraintViolationDegree" -> new OverallConstraintViolationDegreeComparator<>();
            case "Multi" -> new MultiComparator<>();
            case "Objective" -> new ObjectiveComparator<>(0);
            case "DirScore" -> new DirScoreComparator<>();
            case "GDominanceComparator" -> throw new IllegalArgumentException("GDominanceComparator is not implemented.");
            default -> throw new IllegalArgumentException("Unknown comparator: " + name);
        };
    }
}

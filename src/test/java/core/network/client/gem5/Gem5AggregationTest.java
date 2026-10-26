package core.network.client.gem5;

import core.model.individual.FadseIndividual;
import core.model.objectives.Objective;
import core.problem.adapters.Gem5Problem;
import core.problem.ports.MicroArchitectureProblem;
import input.adapters.parameter.numerics.type.IntegerParameter;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import input.ports.parameter.problem.ProblemParameter;
import org.junit.Test;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Gem5AggregationTest {
    private double aggregate(double... values) throws Exception {
        return aggregate("cpi", true, values);
    }

    private double aggregate(String name, boolean minimized, double... values) throws Exception {
        InputData input = new InputData();
        input.set(CommonSetupParameters.NAME, "gem5");
        input.set(CommonSetupParameters.PROBLEM_CONFIG, Map.of());
        IntegerParameter width = new IntegerParameter(2, 8);
        width.setName("width");
        input.set(CommonSetupParameters.DESIGN_VARIABLES, new ProblemParameter<?>[]{width});
        input.set(CommonSetupParameters.OBJECTIVES, Map.of(name, new Objective(name, "float", minimized)));
        input.set(CommonSetupParameters.BENCHMARKS, List.of("bfs", "pr"));
        Gem5Problem problem = new Gem5Problem(input);
        IntegerSolution solution = problem.createSolution();
        List<FadseIndividual> individuals = new LinkedList<>();
        for (double value : values) {
            FadseIndividual individual = new FadseIndividual(input, "workload");
            Objective objective = new Objective(name, "float", minimized);
            objective.setValue(value);
            individual.setObjectives(new LinkedList<>(List.of(objective)));
            individuals.add(individual);
        }
        // Seed completed network results without creating a clients repository.
        var tracking = MicroArchitectureProblem.class.getDeclaredField("solutionIndividualsMap");
        tracking.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<IntegerSolution, List<FadseIndividual>> results =
                (Map<IntegerSolution, List<FadseIndividual>>) tracking.get(problem);
        results.put(solution, individuals);
        problem.aggregateObjectives(solution);
        for (int index = 0; index < values.length; index++) {
            assertEquals("Raw workload measurements must remain unchanged", values[index],
                    individuals.get(index).getObjectives().getFirst().getValue(), 0);
        }
        double first = solution.objectives()[0];
        problem.aggregateObjectives(solution);
        assertEquals("Repeated aggregation must not apply a second transform", first,
                solution.objectives()[0], 0);
        return solution.objectives()[0];
    }

    @Test
    public void averagesOnlyACompleteValidSuite() throws Exception {
        assertEquals(3, aggregate(2, 4), 0);
    }

    @Test
    public void missingWorkloadRejectsTheWholeArchitecture() throws Exception {
        assertEquals(Double.MAX_VALUE, aggregate(2), 0);
    }

    @Test
    public void failedOrNonfiniteWorkloadRejectsTheWholeArchitecture() throws Exception {
        for (double invalid : new double[]{Double.MAX_VALUE, Double.MIN_VALUE, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(Double.MAX_VALUE, aggregate(2, invalid), 0);
        }
    }

    @Test
    public void maximizingIpcUsesMinimizationScoresWithoutChangingMeasurements() throws Exception {
        assertEquals(-3, aggregate("ipc", false, 2, 4), 0);
        assertTrue(aggregate("ipc", false, 4, 6) < aggregate("ipc", false, 1, 2));
    }

    @Test
    public void failedMaximizingObjectivesRemainWorstForJMetal() throws Exception {
        for (double invalid : new double[]{Double.MAX_VALUE, Double.MIN_VALUE, Double.NaN,
                Double.POSITIVE_INFINITY}) {
            assertEquals(Double.MAX_VALUE, aggregate("ipc", false, 2, invalid), 0);
        }
        assertEquals(Double.MAX_VALUE, aggregate("ipc", false, 2), 0);
    }
}

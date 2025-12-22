package core.algorithm.adapters;

import core.algorithm.builders.NSGAIIIBBuilder;
import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.util.EnvironmentalSelection;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.util.ReferencePoint;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.ranking.Ranking;
import org.uma.jmetal.util.ranking.impl.FastNonDominatedSortRanking;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class NSGAIIIB<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, List<S>> {
    protected int iterations;
    protected int maxIterations;
    protected SolutionListEvaluator<S> evaluator;
    protected int numberOfDivisions;
    protected List<ReferencePoint<S>> referencePoints = new Vector<>();

    public NSGAIIIB(NSGAIIIBBuilder<S> builder) {
        super(builder.getProblem());
        this.maxIterations = builder.getMaxIterations();
        this.crossoverOperator = builder.getCrossoverOperator();
        this.mutationOperator = builder.getMutationOperator();
        this.selectionOperator = builder.getSelectionOperator();
        this.evaluator = builder.getEvaluator();
        this.numberOfDivisions = builder.getNumberOfDivisions();
        (new ReferencePoint<S>()).generateReferencePoints(this.referencePoints, this.getProblem().numberOfObjectives(), this.numberOfDivisions);

        this.setMaxPopulationSize(builder.getPopulationSize());
        JMetalLogger.logger.info("rpssize: " + this.referencePoints.size());
    }

    protected void initProgress() {
        this.iterations = 1;
    }

    protected void updateProgress() {
        ++this.iterations;
    }

    protected boolean isStoppingConditionReached() {
        return this.iterations >= this.maxIterations;
    }

    protected List<S> evaluatePopulation(List<S> population) {
        population = this.evaluator.evaluate(population, this.getProblem());
        return population;
    }

    protected List<S> selection(List<S> population) {
        List<S> matingPopulation = new ArrayList<>(population.size());

        for(int i = 0; i < this.getMaxPopulationSize(); ++i) {
            S solution = this.selectionOperator.execute(population);
            matingPopulation.add(solution);
        }

        return matingPopulation;
    }

    protected List<S> reproduction(List<S> population) {
        List<S> offspringPopulation = new ArrayList<>(this.getMaxPopulationSize());

        for(int i = 0; i < this.getMaxPopulationSize(); i += 2) {
            List<S> parents = new ArrayList<>(2);
            parents.add(population.get(i));
            parents.add(population.get(Math.min(i + 1, this.getMaxPopulationSize() - 1)));
            List<S> offspring = crossoverOperator.execute(parents);
            this.mutationOperator.execute(offspring.get(0));
            this.mutationOperator.execute(offspring.get(1));
            offspringPopulation.add(offspring.get(0));
            offspringPopulation.add(offspring.get(1));
        }

        return offspringPopulation;
    }

    private List<ReferencePoint<S>> getReferencePointsCopy() {
        List<ReferencePoint<S>> copy = new ArrayList<>();

        for(ReferencePoint<S> r : this.referencePoints) {
            copy.add(new ReferencePoint<>(r));
        }

        return copy;
    }

    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        List<S> jointPopulation = new ArrayList<>();
        jointPopulation.addAll(population);
        jointPopulation.addAll(offspringPopulation);
        Ranking<S> ranking = this.computeRanking(jointPopulation);
        List<S> last = new ArrayList<>();
        List<S> pop = new ArrayList<>();
        List<List<S>> fronts = new ArrayList<>();
        int rankingIndex = 0;

        for (int candidateSolutions = 0; candidateSolutions < this.getMaxPopulationSize(); ++rankingIndex) {
            last = ranking.getSubFront(rankingIndex);
            fronts.add(last);
            candidateSolutions += last.size();
            if (pop.size() + last.size() <= this.getMaxPopulationSize()) {
                pop.addAll(last);
            }
        }

        if (pop.size() == this.getMaxPopulationSize()) {
            return pop;
        }

        EnvironmentalSelection<S> selection = new EnvironmentalSelection<>(fronts, this.getMaxPopulationSize() - pop.size(), this.getReferencePointsCopy(), this.getProblem().numberOfObjectives());
        List<S> chosen = selection.execute(last);
        pop.addAll(chosen);
        return pop;

    }

    public List<S> result() {
        return this.getNonDominatedSolutions(this.getPopulation());
    }

    protected Ranking<S> computeRanking(List<S> solutionList) {
        Ranking<S> ranking = new FastNonDominatedSortRanking<>();
        ranking.compute(solutionList);
        return ranking;
    }

    protected List<S> getNonDominatedSolutions(List<S> solutionList) {
        return SolutionListUtils.getNonDominatedSolutions(solutionList);
    }

    public String name() {
        return "NSGAIIIb";
    }

    public String description() {
        return "Nondominated Sorting Genetic Algorithm version IIIb";
    }
}

package core.algorithm.builders;

import core.algorithm.adapters.NSGAIIIB;
import org.uma.jmetal.algorithm.AlgorithmBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.util.List;

public class NSGAIIIBBuilder<S extends Solution<?>> implements AlgorithmBuilder<NSGAIIIB<S>> {
    private final Problem<S> problem;
    private int maxIterations;
    private int populationSize;
    private int numberOfDivisions;
    private CrossoverOperator<S> crossoverOperator;
    private MutationOperator<S> mutationOperator;
    private SelectionOperator<List<S>, S> selectionOperator;
    private SolutionListEvaluator<S> evaluator;

    public NSGAIIIBBuilder(Problem<S> problem) {
        this.problem = problem;
        this.maxIterations = 250;
        this.populationSize = 100;
        this.numberOfDivisions = 12;
        this.evaluator = new SequentialSolutionListEvaluator<>();
    }

    public NSGAIIIBBuilder<S> setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
        return this;
    }

    public NSGAIIIBBuilder<S> setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
        return this;
    }

    public NSGAIIIBBuilder<S> setNumberOfDivisions(int numberOfDivisions) {
        this.numberOfDivisions = numberOfDivisions;
        return this;
    }

    public NSGAIIIBBuilder<S> setCrossoverOperator(CrossoverOperator<S> crossoverOperator) {
        this.crossoverOperator = crossoverOperator;
        return this;
    }

    public NSGAIIIBBuilder<S> setMutationOperator(MutationOperator<S> mutationOperator) {
        this.mutationOperator = mutationOperator;
        return this;
    }

    public NSGAIIIBBuilder<S> setSelectionOperator(SelectionOperator<List<S>, S> selectionOperator) {
        this.selectionOperator = selectionOperator;
        return this;
    }

    public NSGAIIIBBuilder<S> setSolutionListEvaluator(SolutionListEvaluator<S> evaluator) {
        this.evaluator = evaluator;
        return this;
    }

    public SolutionListEvaluator<S> getEvaluator() {
        return this.evaluator;
    }

    public Problem<S> getProblem() {
        return this.problem;
    }

    public int getMaxIterations() {
        return this.maxIterations;
    }

    public int getPopulationSize() {
        return this.populationSize;
    }

    public int getNumberOfDivisions() {
        return this.numberOfDivisions;
    }

    public CrossoverOperator<S> getCrossoverOperator() {
        return this.crossoverOperator;
    }

    public MutationOperator<S> getMutationOperator() {
        return this.mutationOperator;
    }

    public SelectionOperator<List<S>, S> getSelectionOperator() {
        return this.selectionOperator;
    }

    public NSGAIIIB<S> build() {
        return new NSGAIIIB<>(this);
    }
}
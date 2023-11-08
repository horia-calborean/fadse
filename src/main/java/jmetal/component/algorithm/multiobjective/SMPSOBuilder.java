package jmetal.component.algorithm.multiobjective;

import jmetal.component.algorithm.ParticleSwarmOptimizationAlgorithm;
import jmetal.component.catalogue.common.evaluation.Evaluation;
import jmetal.component.catalogue.common.evaluation.impl.SequentialEvaluation;
import jmetal.component.catalogue.common.solutionscreation.SolutionsCreation;
import jmetal.component.catalogue.common.solutionscreation.impl.RandomSolutionsCreation;
import jmetal.component.catalogue.common.termination.Termination;
import jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import jmetal.component.catalogue.pso.globalbestinitialization.GlobalBestInitialization;
import jmetal.component.catalogue.pso.globalbestinitialization.impl.DefaultGlobalBestInitialization;
import jmetal.component.catalogue.pso.globalbestselection.GlobalBestSelection;
import jmetal.component.catalogue.pso.globalbestselection.impl.BinaryTournamentGlobalBestSelection;
import jmetal.component.catalogue.pso.globalbestupdate.GlobalBestUpdate;
import jmetal.component.catalogue.pso.globalbestupdate.impl.DefaultGlobalBestUpdate;
import jmetal.component.catalogue.pso.inertiaweightcomputingstrategy.InertiaWeightComputingStrategy;
import jmetal.component.catalogue.pso.inertiaweightcomputingstrategy.impl.ConstantValueStrategy;
import jmetal.component.catalogue.pso.localbestinitialization.LocalBestInitialization;
import jmetal.component.catalogue.pso.localbestinitialization.impl.DefaultLocalBestInitialization;
import jmetal.component.catalogue.pso.localbestupdate.LocalBestUpdate;
import jmetal.component.catalogue.pso.localbestupdate.impl.DefaultLocalBestUpdate;
import jmetal.component.catalogue.pso.perturbation.Perturbation;
import jmetal.component.catalogue.pso.perturbation.impl.FrequencySelectionMutationBasedPerturbation;
import jmetal.component.catalogue.pso.positionupdate.PositionUpdate;
import jmetal.component.catalogue.pso.positionupdate.impl.DefaultPositionUpdate;
import jmetal.component.catalogue.pso.velocityinitialization.VelocityInitialization;
import jmetal.component.catalogue.pso.velocityinitialization.impl.DefaultVelocityInitialization;
import jmetal.component.catalogue.pso.velocityupdate.VelocityUpdate;
import jmetal.component.catalogue.pso.velocityupdate.impl.ConstrainedVelocityUpdate;
import jmetal.core.operator.mutation.MutationOperator;
import jmetal.core.operator.mutation.impl.PolynomialMutation;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.archive.BoundedArchive;
import jmetal.core.util.archive.impl.CrowdingDistanceArchive;
import jmetal.core.util.comparator.dominanceComparator.impl.DefaultDominanceComparator;
/**
 * Class to configure and build an instance of the SMPSO algorithm
 */
public class SMPSOBuilder {
  private final String name ;
  private SolutionsCreation<DoubleSolution> swarmInitialization;
  private Evaluation<DoubleSolution> evaluation;
  private Termination termination;
  private VelocityInitialization velocityInitialization;
  private LocalBestInitialization localBestInitialization;
  private GlobalBestInitialization globalBestInitialization;
  private InertiaWeightComputingStrategy inertiaWeightComputingStrategy;
  private VelocityUpdate velocityUpdate;
  private PositionUpdate positionUpdate;
  private Perturbation perturbation;
  private GlobalBestUpdate globalBestUpdate;
  private LocalBestUpdate localBestUpdate;
  private GlobalBestSelection globalBestSelection;
  private BoundedArchive<DoubleSolution> archive;

  public SMPSOBuilder(DoubleProblem problem, int swarmSize) {
    name = "SMPSO";

    swarmInitialization = new RandomSolutionsCreation<>(problem, swarmSize);
    evaluation = new SequentialEvaluation<>(problem);
    termination = new TerminationByEvaluations(25000);
    velocityInitialization = new DefaultVelocityInitialization();
    localBestInitialization = new DefaultLocalBestInitialization();
    globalBestInitialization = new DefaultGlobalBestInitialization();

    archive = new CrowdingDistanceArchive<>(swarmSize);
    globalBestSelection = new BinaryTournamentGlobalBestSelection(archive.comparator()) ;

    double r1Min = 0.0;
    double r1Max = 1.0;
    double r2Min = 0.0;
    double r2Max = 1.0;
    double c1Min = 1.5;
    double c1Max = 2.5;
    double c2Min = 1.5;
    double c2Max = 2.5;
    double weight = 0.1;
    inertiaWeightComputingStrategy = new ConstantValueStrategy(weight) ;

    velocityUpdate = new ConstrainedVelocityUpdate(r1Min, r1Max, r2Min, r2Max, c1Min, c1Max,
        c2Min, c2Max, problem);

    double velocityChangeWhenLowerLimitIsReached = -1.0;
    double velocityChangeWhenUpperLimitIsReached = -1.0;
    positionUpdate = new DefaultPositionUpdate(velocityChangeWhenLowerLimitIsReached,
        velocityChangeWhenUpperLimitIsReached, problem.variableBounds());

    int frequencyOfMutation = 6;
    MutationOperator<DoubleSolution> mutationOperator = new PolynomialMutation(1.0 / problem.numberOfVariables(), 20.0) ;
    perturbation = new FrequencySelectionMutationBasedPerturbation(mutationOperator, frequencyOfMutation);
    globalBestUpdate = new DefaultGlobalBestUpdate();
    localBestUpdate = new DefaultLocalBestUpdate(new DefaultDominanceComparator<>());
  }

  public SMPSOBuilder setTermination(Termination termination) {
    this.termination = termination;

    return this;
  }

  public SMPSOBuilder setArchive(BoundedArchive<DoubleSolution> archive) {
    this.archive = archive;

    return this;
  }

  public SMPSOBuilder setEvaluation(Evaluation<DoubleSolution> evaluation) {
    this.evaluation = evaluation;

    return this;
  }

  public SMPSOBuilder setPerturbation(Perturbation perturbation) {
    this.perturbation = perturbation ;

    return this ;
  }

  public SMPSOBuilder setPositionUpdate(PositionUpdate positionUpdate) {
    this.positionUpdate = positionUpdate ;

    return this ;
  }

  public SMPSOBuilder setGlobalBestSelection(GlobalBestSelection globalBestSelection) {
    this.globalBestSelection = globalBestSelection ;

    return this ;
  }

  public SMPSOBuilder setGlobalBestInitialization(GlobalBestInitialization globalBestInitialization) {
    this.globalBestInitialization = globalBestInitialization ;

    return this ;
  }

  public SMPSOBuilder setGlobalBestUpdate(GlobalBestUpdate globalBestUpdate) {
    this.globalBestUpdate = globalBestUpdate ;

    return this ;
  }

  public SMPSOBuilder setLocalBestUpdate(LocalBestUpdate localBestUpdate) {
    this.localBestInitialization = localBestInitialization ;

    return this ;
  }

  public ParticleSwarmOptimizationAlgorithm build() {
    return new ParticleSwarmOptimizationAlgorithm(name, swarmInitialization, evaluation, termination,
        velocityInitialization,
        localBestInitialization,
        globalBestInitialization,
        inertiaWeightComputingStrategy,
        velocityUpdate,
        positionUpdate,
        perturbation,
        globalBestUpdate,
        localBestUpdate,
        globalBestSelection,
        archive);
  }
}

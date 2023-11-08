package jmetal.auto.autoconfigurablealgorithm;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jmetal.auto.parameter.BooleanParameter;
import jmetal.auto.parameter.CategoricalParameter;
import jmetal.auto.parameter.IntegerParameter;
import jmetal.auto.parameter.Parameter;
import jmetal.auto.parameter.PositiveIntegerValue;
import jmetal.auto.parameter.RealParameter;
import jmetal.auto.parameter.StringParameter;
import jmetal.auto.parameter.catalogue.AggregationFunctionParameter;
import jmetal.auto.parameter.catalogue.CreateInitialSolutionsParameter;
import jmetal.auto.parameter.catalogue.CrossoverParameter;
import jmetal.auto.parameter.catalogue.DifferentialEvolutionCrossoverParameter;
import jmetal.auto.parameter.catalogue.ExternalArchiveParameter;
import jmetal.auto.parameter.catalogue.MutationParameter;
import jmetal.auto.parameter.catalogue.ProbabilityParameter;
import jmetal.auto.parameter.catalogue.RepairDoubleSolutionStrategyParameter;
import jmetal.auto.parameter.catalogue.SelectionParameter;
import jmetal.auto.parameter.catalogue.VariationParameter;
import jmetal.component.algorithm.EvolutionaryAlgorithm;
import jmetal.component.catalogue.common.evaluation.Evaluation;
import jmetal.component.catalogue.common.evaluation.impl.SequentialEvaluation;
import jmetal.component.catalogue.common.evaluation.impl.SequentialEvaluationWithArchive;
import jmetal.component.catalogue.common.solutionscreation.SolutionsCreation;
import jmetal.component.catalogue.common.termination.Termination;
import jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import jmetal.component.catalogue.ea.replacement.Replacement;
import jmetal.component.catalogue.ea.replacement.impl.MOEADReplacement;
import jmetal.component.catalogue.ea.selection.Selection;
import jmetal.component.catalogue.ea.selection.impl.PopulationAndNeighborhoodSelection;
import jmetal.component.catalogue.ea.variation.Variation;
import jmetal.core.problem.Problem;
import jmetal.problem.ProblemFactory;
import jmetal.core.problem.doubleproblem.DoubleProblem;
import jmetal.core.solution.doublesolution.DoubleSolution;
import jmetal.core.util.aggregationfunction.AggregationFunction;
import jmetal.core.util.archive.Archive;
import jmetal.core.util.neighborhood.Neighborhood;
import jmetal.core.util.neighborhood.impl.WeightVectorNeighborhood;
import jmetal.core.util.pseudorandom.JMetalRandom;
import jmetal.core.util.sequencegenerator.impl.IntegerPermutationGenerator;

/**
 * Class to configure NSGA-II with an argument string using class {@link EvolutionaryAlgorithm}
 *
 * @autor Antonio J. Nebro
 */
public class AutoMOEAD implements AutoConfigurableAlgorithm {
  public List<Parameter<?>> autoConfigurableParameterList = new ArrayList<>();
  public List<Parameter<?>> fixedParameterList = new ArrayList<>();
  private StringParameter problemNameParameter;
  public StringParameter referenceFrontFilenameParameter;
  private PositiveIntegerValue randomGeneratorSeedParameter;
  private PositiveIntegerValue maximumNumberOfEvaluationsParameter;
  private CategoricalParameter algorithmResultParameter;
  private ExternalArchiveParameter<DoubleSolution> externalArchiveParameter;
  private PositiveIntegerValue populationSizeParameter;
  private PositiveIntegerValue offspringPopulationSizeParameter;
  private CreateInitialSolutionsParameter createInitialSolutionsParameter;
  private SelectionParameter<DoubleSolution> selectionParameter;
  private VariationParameter variationParameter;
  private ProbabilityParameter neighborhoodSelectionProbabilityParameter;
  private IntegerParameter neighborhoodSizeParameter;
  private IntegerParameter maximumNumberOfReplacedSolutionsParameter;
  private AggregationFunctionParameter aggregationFunctionParameter;
  private BooleanParameter normalizeObjectivesParameter ;

  @Override
  public List<Parameter<?>> configurableParameterList() {
    return autoConfigurableParameterList;
  }

  @Override
  public List<Parameter<?>> fixedParameterList() {
    return fixedParameterList;
  }

  public AutoMOEAD() {
    this.configure() ;
  }

  public void configure() {
    problemNameParameter = new StringParameter("problemName");
    randomGeneratorSeedParameter = new PositiveIntegerValue("randomGeneratorSeed") ;
    maximumNumberOfEvaluationsParameter =
        new PositiveIntegerValue("maximumNumberOfEvaluations");

    referenceFrontFilenameParameter = new StringParameter("referenceFrontFileName");

    populationSizeParameter = new PositiveIntegerValue("populationSize");

    fixedParameterList.add(populationSizeParameter);
    fixedParameterList.add(problemNameParameter);
    fixedParameterList.add(referenceFrontFilenameParameter);
    fixedParameterList.add(maximumNumberOfEvaluationsParameter);
    fixedParameterList.add(randomGeneratorSeedParameter) ;

    neighborhoodSizeParameter = new IntegerParameter("neighborhoodSize",5, 50);
    neighborhoodSelectionProbabilityParameter =
        new ProbabilityParameter("neighborhoodSelectionProbability");
    maximumNumberOfReplacedSolutionsParameter =
        new IntegerParameter("maximumNumberOfReplacedSolutions",1, 5);

    normalizeObjectivesParameter = new BooleanParameter("normalizeObjectives") ;
    RealParameter epsilonParameterForNormalizing = new RealParameter("epsilonParameterForNormalizing", 0.0000001, 25.0) ;
    normalizeObjectivesParameter.addGlobalParameter(epsilonParameterForNormalizing);

    aggregationFunctionParameter =
        new AggregationFunctionParameter(
            List.of("tschebyscheff", "weightedSum", "penaltyBoundaryIntersection"));
    RealParameter pbiTheta = new RealParameter("pbiTheta",1.0, 200);
    aggregationFunctionParameter.addSpecificParameter("penaltyBoundaryIntersection", pbiTheta);
    aggregationFunctionParameter.addGlobalParameter(normalizeObjectivesParameter);

    algorithmResult();
    createInitialSolution();
    selection();
    variation();

    autoConfigurableParameterList.add(neighborhoodSizeParameter);
    autoConfigurableParameterList.add(maximumNumberOfReplacedSolutionsParameter);
    autoConfigurableParameterList.add(normalizeObjectivesParameter);
    autoConfigurableParameterList.add(aggregationFunctionParameter);

    autoConfigurableParameterList.add(algorithmResultParameter);
    autoConfigurableParameterList.add(createInitialSolutionsParameter);
    autoConfigurableParameterList.add(variationParameter);
    autoConfigurableParameterList.add(selectionParameter);
  }

  private void variation() {
    CrossoverParameter crossoverParameter = new CrossoverParameter(List.of("SBX", "BLX_ALPHA", "wholeArithmetic"));
    ProbabilityParameter crossoverProbability =
        new ProbabilityParameter("crossoverProbability");
    crossoverParameter.addGlobalParameter(crossoverProbability);
    RepairDoubleSolutionStrategyParameter crossoverRepairStrategy =
        new RepairDoubleSolutionStrategyParameter(
            "crossoverRepairStrategy", Arrays.asList("random", "round", "bounds"));
    crossoverParameter.addGlobalParameter(crossoverRepairStrategy);

    RealParameter distributionIndex = new RealParameter("sbxDistributionIndex",5.0, 400.0);
    crossoverParameter.addSpecificParameter("SBX", distributionIndex);

    RealParameter alpha = new RealParameter("blxAlphaCrossoverAlphaValue",0.0, 1.0);
    crossoverParameter.addSpecificParameter("BLX_ALPHA", alpha);

    MutationParameter mutationParameter =
        new MutationParameter(Arrays.asList("uniform", "polynomial", "linkedPolynomial", "nonUniform"));

    RealParameter mutationProbabilityFactor = new RealParameter("mutationProbabilityFactor",
        0.0, 2.0);
    mutationParameter.addGlobalParameter(mutationProbabilityFactor);
    RepairDoubleSolutionStrategyParameter mutationRepairStrategy =
        new RepairDoubleSolutionStrategyParameter(
            "mutationRepairStrategy", Arrays.asList("random", "round", "bounds"));
    mutationParameter.addGlobalParameter(mutationRepairStrategy);

    RealParameter distributionIndexForPolynomialMutation =
        new RealParameter("polynomialMutationDistributionIndex", 5.0, 400.0);
    mutationParameter.addSpecificParameter("polynomial", distributionIndexForPolynomialMutation);

    RealParameter distributionIndexForLinkedPolynomialMutation =
        new RealParameter("linkedPolynomialMutationDistributionIndex",5.0, 400.0);
    mutationParameter.addSpecificParameter("linkedPolynomial",
        distributionIndexForLinkedPolynomialMutation);

    RealParameter uniformMutationPerturbation =
        new RealParameter("uniformMutationPerturbation",0.0, 1.0);
    mutationParameter.addSpecificParameter("uniform", uniformMutationPerturbation);

    RealParameter nonUniformMutationPerturbation =
        new RealParameter("nonUniformMutationPerturbation", 0.0, 1.0);
    mutationParameter.addSpecificParameter("nonUniform", nonUniformMutationPerturbation);

    DifferentialEvolutionCrossoverParameter deCrossoverParameter =
        new DifferentialEvolutionCrossoverParameter(List.of("RAND_1_BIN", "RAND_1_EXP", "RAND_2_BIN"));

    RealParameter crParameter = new RealParameter("CR", 0.0, 1.0);
    RealParameter fParameter = new RealParameter("F", 0.0, 1.0);
    deCrossoverParameter.addGlobalParameter(crParameter);
    deCrossoverParameter.addGlobalParameter(fParameter);

    offspringPopulationSizeParameter = new PositiveIntegerValue("offspringPopulationSize") ;

    variationParameter =
        new VariationParameter(List.of("crossoverAndMutationVariation", "differentialEvolutionVariation"));
    variationParameter.addSpecificParameter("crossoverAndMutationVariation", crossoverParameter);
    variationParameter.addSpecificParameter("crossoverAndMutationVariation", mutationParameter);
    variationParameter.addSpecificParameter("crossoverAndMutationVariation", offspringPopulationSizeParameter);
    variationParameter.addSpecificParameter("differentialEvolutionVariation", mutationParameter);
    variationParameter.addSpecificParameter("differentialEvolutionVariation", deCrossoverParameter);
  }

  private void selection() {
    selectionParameter = new SelectionParameter<>(Arrays.asList("populationAndNeighborhoodMatingPoolSelection"));
    neighborhoodSelectionProbabilityParameter =
        new ProbabilityParameter("neighborhoodSelectionProbability");
    selectionParameter.addSpecificParameter(
        "populationAndNeighborhoodMatingPoolSelection", neighborhoodSelectionProbabilityParameter);
  }

  private void createInitialSolution() {
    createInitialSolutionsParameter =
        new CreateInitialSolutionsParameter(Arrays.asList("random", "latinHypercubeSampling", "scatterSearch"));
  }

  private void algorithmResult() {
    algorithmResultParameter =
        new CategoricalParameter("algorithmResult", List.of("externalArchive", "population"));
    externalArchiveParameter = new ExternalArchiveParameter(List.of("crowdingDistanceArchive", "unboundedArchive"));

    algorithmResultParameter.addSpecificParameter(
        "externalArchive", externalArchiveParameter);
  }

  @Override
  public void parse(String[] arguments) {
    for (Parameter<?> parameter : fixedParameterList) {
      parameter.parse(arguments).check();
    }
    for (Parameter<?> parameter : configurableParameterList()) {
      parameter.parse(arguments).check();
    }
  }

  protected Problem<DoubleSolution> problem() {
    return ProblemFactory.loadProblem(problemNameParameter.value());
  }

  /**
   * Creates an instance of MOEA/D from the parsed parameters
   *
   * @return
   */
  public EvolutionaryAlgorithm<DoubleSolution> create() {
    JMetalRandom.getInstance().setSeed(randomGeneratorSeedParameter.value());

    Problem<DoubleSolution> problem = problem() ;

    Archive<DoubleSolution> archive = null;
    Evaluation<DoubleSolution> evaluation ;
    if (algorithmResultParameter.value().equals("externalArchive")) {
      externalArchiveParameter.setSize(populationSizeParameter.value());
      archive = externalArchiveParameter.getParameter();
      evaluation = new SequentialEvaluationWithArchive<>(problem, archive);
    } else {
      evaluation = new SequentialEvaluation<>(problem);
    }

    var initialSolutionsCreation =
        (SolutionsCreation<DoubleSolution>) createInitialSolutionsParameter.getParameter((DoubleProblem) problem,
            populationSizeParameter.value());

    Termination termination =
        new TerminationByEvaluations(maximumNumberOfEvaluationsParameter.value());

    MutationParameter mutationParameter = (MutationParameter) variationParameter.findSpecificParameter(
        "mutation");
    mutationParameter.addNonConfigurableParameter("numberOfProblemVariables",
        problem.numberOfVariables());

    if (mutationParameter.value().equals("nonUniform")) {
      mutationParameter.addSpecificParameter("nonUniform", maximumNumberOfEvaluationsParameter);
      mutationParameter.addNonConfigurableParameter("maxIterations",
          maximumNumberOfEvaluationsParameter.value() / populationSizeParameter.value());
    }

    Neighborhood<DoubleSolution> neighborhood = null ;

    if (problem.numberOfObjectives() == 2) {
      neighborhood =
          new WeightVectorNeighborhood<>(
              populationSizeParameter.value(), neighborhoodSizeParameter.value());
    } else {
      try {
        neighborhood =
            new WeightVectorNeighborhood<>(
                populationSizeParameter.value(),
                problem.numberOfObjectives(),
                neighborhoodSizeParameter.value(),
                "resources/weightVectorFiles/moead");
      } catch (FileNotFoundException exception) {
        exception.printStackTrace();
      }
    }

    var subProblemIdGenerator = new IntegerPermutationGenerator(populationSizeParameter.value());
    selectionParameter.addNonConfigurableParameter("neighborhood", neighborhood);
    selectionParameter.addNonConfigurableParameter("subProblemIdGenerator", subProblemIdGenerator);

    variationParameter.addNonConfigurableParameter("subProblemIdGenerator", subProblemIdGenerator);

    var variation = (Variation<DoubleSolution>) variationParameter.getDoubleSolutionParameter();

    var selection =
        (PopulationAndNeighborhoodSelection<DoubleSolution>)
            selectionParameter.getParameter(variation.getMatingPoolSize(), null);

    int maximumNumberOfReplacedSolutions = maximumNumberOfReplacedSolutionsParameter.value();

    aggregationFunctionParameter.normalizedObjectives(normalizeObjectivesParameter.value());
    AggregationFunction aggregativeFunction = aggregationFunctionParameter.getParameter();
    var replacement =
        new MOEADReplacement<>(
            selection,
            (WeightVectorNeighborhood<DoubleSolution>) neighborhood,
            aggregativeFunction,
            subProblemIdGenerator,
            maximumNumberOfReplacedSolutions, normalizeObjectivesParameter.value());

    class EvolutionaryAlgorithmWithArchive extends EvolutionaryAlgorithm<DoubleSolution> {
      private Archive<DoubleSolution> archive ;
      /**
       * Constructor
       *
       * @param name                      Algorithm name
       * @param initialPopulationCreation
       * @param evaluation
       * @param termination
       * @param selection
       * @param variation
       * @param replacement
       */
      public EvolutionaryAlgorithmWithArchive(String name,
          SolutionsCreation<DoubleSolution> initialPopulationCreation,
          Evaluation<DoubleSolution> evaluation, Termination termination,
          Selection<DoubleSolution> selection, Variation<DoubleSolution> variation,
          Replacement<DoubleSolution> replacement,
          Archive<DoubleSolution> archive) {
        super(name, initialPopulationCreation, evaluation, termination, selection, variation,
            replacement);
        this.archive = archive ;
      }

      @Override
      public List<DoubleSolution> result() {
        return archive.solutions() ;
      }
    }

    if (algorithmResultParameter.value().equals("externalArchive")) {
      return new EvolutionaryAlgorithmWithArchive(
          "MOEAD",
          initialSolutionsCreation,
          evaluation,
          termination,
          selection,
          variation,
          replacement,
          archive) ;
    } else {
      return new EvolutionaryAlgorithm<>(
          "MOEAD",
          initialSolutionsCreation,
          evaluation,
          termination,
          selection,
          variation,
          replacement);
    }
  }

  public static void print(List<Parameter<?>> parameterList) {
    parameterList.forEach(System.out::println);
  }
}

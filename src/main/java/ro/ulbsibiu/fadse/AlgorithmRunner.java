package ro.ulbsibiu.fadse;

import java.io.File;
import java.io.FileInputStream;

import jmetal.base.*;
import jmetal.problems.*;
import jmetal.util.Configuration;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.uma.jmetal.util.errorchecking.JMetalException;
import ro.ulbsibiu.fadse.environment.SimulationIO;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.server.status.SimulationStatus;
import jmetal.experiments.Settings;
import jmetal.experiments.SettingsFactory;

public class AlgorithmRunner {
    public static Logger logger_; // Logger object
    public static FileHandler fileHandler_; // FileHandler object
    private Algorithm algorithm = null; // The algorithm to use

    public void run(SimulationIO env) throws JMetalException, SecurityException,
            IOException, IllegalArgumentException, IllegalAccessException,
            ClassNotFoundException {
        // Runtime.getRuntime().addShutdownHook(new Thread(new
        // PerformCleanup()));
        Problem problem; // The problem to solve

        Properties properties;
        Settings settings = null;
        String algorithmName = env.getDesignSpaceDocument().getMetaheuristicName();
        String problemName = env.getDesignSpaceDocument().getSimulatorName();

        properties = new Properties();
        String path = "N/A";
        String currentDir = System.getProperty("user.dir");
        System.out.println("Current folder is: "+currentDir);
        try {
            path = env.getDesignSpaceDocument().getMetaheuristicConfigPath();
            properties.load(new FileInputStream(currentDir+ File.separator + path));
        } catch (Exception e) {
            System.out.println("BAD properties file [" + path + "]. going with default values");
        }
        long initTime = System.currentTimeMillis();
        // Logger object and file to store log messages
        logger_ = Configuration.logger_;
        fileHandler_ = new FileHandler(algorithmName + ".log");
        logger_.addHandler(fileHandler_);
        SolutionSet population = null;
        System.out.println(env.getDesignSpaceDocument().getSimulatorType());
        if (env.getDesignSpaceDocument().getSimulatorType().equalsIgnoreCase("synthetic")) {
            // it is a synthetic problem
            problem = null;
            Object[] problemParams = {"Real"};// TODO configure the problem
            // param type, nr of variables,
            // nr of objectives
            if (problemName.startsWith("DTLZ")) {
                problemParams = new Object[3];
                problemParams[0] = "Real";
                problemParams[1] = env.getDesignSpaceDocument().getParameters().length;
                problemParams[2] = env.getDesignSpaceDocument().getObjectives().size();
            }
            problem = (new ProblemFactory()).getProblem(problemName,
                    problemParams);
        } else {
            // is a simulator
            Object[] problemParams = {env};
            problem = (new ProblemFactory()).getProblem(problemName,
                    problemParams);
        }
        Object[] settingsParams = {problem};
        settings = (new SettingsFactory()).getSettingsObject(algorithmName,
                settingsParams);
        algorithm = settings.configure(properties);
        try {
            algorithm.getOperator("mutation").setParameter("environment", env);
        } catch (Exception e) {
            System.out.println("MUTATION was not defined");
        }
        try {
            algorithm.getOperator("crossover").setParameter("environment", env);
        } catch (Exception e) {
            System.out.println("CROSSOVER was not defined");
        }
        // Algorithm parameters htey work only for NSGA-II for other algorithms
        // we need to define others, we have to see how to do it more easily
        // probably with configuration files
        if (env.getCheckpointFileParameter() != null
                && !env.getCheckpointFileParameter().equals("")) {
            algorithm.setInputParameter("checkpointFile",
                    env.getCheckpointFileParameter());
        }
        if (env.getDesignSpaceDocument().getSimulatorParameter(
                "forceFeasibleFirstGeneration") != null) {
            algorithm.setInputParameter(
                    "forceFeasibleFirstGeneration",
                    env.getDesignSpaceDocument().getSimulatorParameter(
                    "forceFeasibleFirstGeneration"));
        }
        if (env.getDesignSpaceDocument().getSimulatorParameter(
                "forceMinimumPercentageFeasibleIndividuals") != null) {
            algorithm.setInputParameter(
                    "forceMinimumPercentageFeasibleIndividuals",
                    env.getDesignSpaceDocument().getSimulatorParameter(
                    "forceMinimumPercentageFeasibleIndividuals"));
        } else {
            algorithm.setInputParameter(
                    "forceMinimumPercentageFeasibleIndividuals", "0");
        }

        String outputPath = env.getDesignSpaceDocument().getOutputPath();
        env.setResultsFolder(outputPath);

        algorithm.setInputParameter("outputPath", outputPath);

        SimulationStatus.getInstance().setAlgorithm(algorithm);
        SimulationStatus.getInstance().setEnvironment(env);
        // Execute the Algorithm
        population = algorithm.execute();

        population.printObjectivesToFile((new File(outputPath, "FUN")).getPath());
        population.printVariablesToFile((new File(outputPath, "VAR")).getPath());
        long estimatedTime = System.currentTimeMillis() - initTime;
        // Result messages
        logger_.info("Total execution time: " + estimatedTime + "ms");
        logger_.info("Objectives values have been writen to file FUN");
        logger_.info("Variables values have been writen to file VAR");
    } // main
} // main


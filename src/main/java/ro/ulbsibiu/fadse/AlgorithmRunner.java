package ro.ulbsibiu.fadse;

import java.io.File;
import java.io.FileInputStream;

import jmetal.base.*;
import jmetal.core.algorithm.Algorithm;
import jmetal.problems.*; import jmetal.core.problem.Problem; import jmetal.problem.ProblemFactory2;
import jmetal.util.Configuration;
import jmetal.core.util.errorchecking.JMetalException;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.server.status.SimulationStatus;
//import jmetal.experiments.Settings;
//import jmetal.experiments.SettingsFactory;
import jmetal.core.qualityindicator.QualityIndicator;
import jmetal.core.util.Configuration;

/*
 *
 *
 * This file is part of the FADSE tool.
 *
 * Authors: Horia Andrei Calborean {horia.calborean at ulbsibiu.ro}, Andrei
 * Zorila Copyright (c) 2009-2010 All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * The names of its contributors NOT may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 *
 */
/**
 *
 * @author Horia Calborean <horia.calborean at ulbsibiu.ro>
 */
public class AlgorithmRunner {

    public static Logger logger_; // Logger object
    public static FileHandler fileHandler_; // FileHandler object
    private Algorithm algorithm = null; // The algorithm to use

    public void run(Environment env) throws JMetalException, SecurityException, IOException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException
    {
        Problem problem;

        Settings settings = null;
        String algorithmName = env.getInputDocument().getMetaheuristicName();
        String problemName = env.getInputDocument().getSimulatorName();

        Properties properties = new Properties();
        String path = "N/A";
        String currentDir = System.getProperty("user.dir");
        System.out.println("Current folder is: " + currentDir);

        try
        {
            path = env.getInputDocument().getMetaheuristicConfigPath();            
            properties.load(new FileInputStream(currentDir+ File.separator + path));
        }
        catch (Exception e)
        {
            System.out.println("BAD properties file [" + path + "]. going with default values");
        }

        long initTime = System.currentTimeMillis();
        logger_ = Configuration.logger_;
        fileHandler_ = new FileHandler(algorithmName + ".log");
        logger_.addHandler(fileHandler_);
        List<S> population = null;
        System.out.println(env.getInputDocument().getSimulatorType());

        if (env.getInputDocument().getSimulatorType().equalsIgnoreCase("synthetic"))
        {
            problem = null;
            Object[] problemParams = {"Real"};// TODO configure the problem

            if (problemName.startsWith("DTLZ"))
            {
                problemParams = new Object[3];
                problemParams[0] = "Real";
                problemParams[1] = env.getInputDocument().getParameters().length;
                problemParams[2] = env.getInputDocument().getObjectives().size();
            }

            problem = (new ProblemFactory2()).getProblem(problemName, problemParams);
        }
        else
        {
            Object[] problemParams = { env };
            problem = (new ProblemFactory2()).getProblem(problemName, problemParams);
        }

        Object[] settingsParams = { problem };
        settings = (new SettingsFactory()).getSettingsObject(algorithmName, settingsParams);

        algorithm = settings.configure(properties);

        try
        {
            algorithm.getOperator("mutation").setParameter("environment", env);
        }
        catch (Exception e)
        {
            System.out.println("MUTATION was not defined");
        }
        try
        {
            algorithm.getOperator("crossover").setParameter("environment", env);
        }
        catch (Exception e)
        {
            System.out.println("CROSSOVER was not defined");
        }

        if (env.getCheckpointFileParameter() != null && !env.getCheckpointFileParameter().equals(""))
        {
            algorithm.setInputParameter("checkpointFile", env.getCheckpointFileParameter());
        }
        if (env.getInputDocument().getSimulatorParameter("forceFeasibleFirstGeneration") != null)
        {
            algorithm.setInputParameter("forceFeasibleFirstGeneration", env.getInputDocument().getSimulatorParameter("forceFeasibleFirstGeneration"));
        }
        if (env.getInputDocument().getSimulatorParameter("forceMinimumPercentageFeasibleIndividuals") != null)
        {
            algorithm.setInputParameter("forceMinimumPercentageFeasibleIndividuals", env.getInputDocument().getSimulatorParameter("forceMinimumPercentageFeasibleIndividuals"));
        } else
        {
            algorithm.setInputParameter("forceMinimumPercentageFeasibleIndividuals", "0");
        }

        String outputPath = env.getInputDocument().getOutputPath();
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
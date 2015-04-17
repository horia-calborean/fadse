package ro.ulbsibiu.fadse.extended.problems.simulators;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.environment.Individual;
import ro.ulbsibiu.fadse.environment.Objective;
import ro.ulbsibiu.fadse.extended.problems.SimulatorWrapper;
import ro.ulbsibiu.fadse.persistence.Result;
import jmetal.base.Solution;

/**
 * Base Class for simulator configuration.
 * Contains methods for loading the config from an xml
 *
 * @author Andrei
 * @version 0.1
 * @since 15.04.2010
 */
public abstract class SimulatorBase extends SimulatorWrapper {

    protected String simulatorOutputFile;
    protected Map<String, Double> simpleObjectives;
    protected LinkedList<Objective> currentObjectives;
    protected SimulatorRunner simulatorRunner;
    protected SimulatorOutputParser simulatorOutputParser;

    /**
     * Class constructor, initialize variables and calls {@link #parseXml(String)}
     * @param xmlFilePath String the complete path to the configuration fill
     */
    public SimulatorBase(Environment environment) throws ClassNotFoundException {
        super(environment);
        this.InitSimulator();
    }

    /**
     * Simulator Initialization
     */
    public void InitSimulator() {
        this.simulatorOutputFile = "";
        this.simulatorRunner = new SimulatorRunner(this);
        this.simulatorOutputParser = new SimulatorOutputParser(this);
    }

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters" >
    /**
     * @return simulatorRunner linked with this simulator
     */
    public SimulatorRunner getRunner() {
        return this.simulatorRunner;
    }

    /**
     * @return simulatorOutputParser linked with this simulator
     */
    public SimulatorOutputParser getOutputParser() {
        return this.simulatorOutputParser;
    }

    /**
     * @return simulator output file
     */
    public String getSimulatorOutputFile() {
        return this.simulatorOutputFile;
    }

    /**
     * set simulator output file
     */
    public void setSimulatorOutputFile(String value) {
        this.simulatorOutputFile = value;
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Overriden Methods" >
    /**
     * This is the method from GeneralSimulator, maybe i should inherit that class
     * or something
     *
     * @param individual
     * @return
     */
    @Override
    public void performSimulation(Individual individual) {
        System.out.println("\n\n---------- PERFORM SIMULATION ---------");
        // Configure the simulatorRunner
        this.simulatorRunner.setParameters(individual.getParameters());
        // Configure the SimulatorOutputparser
        this.simulatorOutputParser.setObjectives(
                this.environment.getInputDocument().getObjectives());
        LinkedList<Objective> results = null;
        // Look for results in database
        int feasible = -1;
        try {
            feasible = Result.isFeasible(individual);
        } catch (Exception e) {
            System.out.println("ERROR WHILE ACCESING THE DATABASE");
            e.printStackTrace();
        }
        if (feasible < 0) {//NOT in the database
            // If no result found: Run simulation!
            System.out.println("- simulation has to be run");
            simulatorRunner.setIndividual(individual);
            this.simulatorRunner.run(true);
            // Parse simulator output file and get results, objectives
            results = this.simulatorOutputParser.getResults(individual);
            individual.setObjectives(results);
            try {
                Result.insertResult(individual.getEnvironment().getInputDocument(),
                        individual, simulatorOutputParser.fileContents.toString());
            } catch (Exception e) {
                System.out.println("ERROR WHILE ACCESING THE DATABASE");
            }
        } else if (feasible == 0) {//individual is in the database but it is not feasible
            individual.markAsInfeasibleAndSetBadValuesForObjectives("individual is in the databse but it is not feasible");
        } else {//individual in the database and feasible
            individual.setFeasible(true);
            // Parse simulator output file and get results, objectives
            results = this.simulatorOutputParser.getResults(individual);
            individual.setObjectives(results);
        }
    }

    @Override
    public void closeSimulation(Individual individual) {
        this.simulatorRunner.stopRunning();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("\n------------------------------------------------\n");
        str.append(this.environment.getInputDocument().getSimulatorName() + "\n");
        str.append(this.environment.getInputDocument().getSimulatorParameter("simulator_executable") + "\n");
        str.append("------------------- PARAMETERS -----------------\n");
        str.append(this.environment.getInputDocument().getParameters().toString() + "\n");
        str.append("------------------- OBJECTIVES -----------------\n");
        str.append(this.environment.getInputDocument().getObjectives().toString() + "\n");
        str.append("------------------------------------------------\n");

        return str.toString();
    }
}

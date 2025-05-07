package core.network.client;

import core.model.individual.FadseIndividual;
import core.model.objectives.Objective;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;

import java.util.LinkedList;
import java.util.Map;

public abstract class Simulator {

    protected String simulatorOutputFile;
    protected Map<String, Double> simpleObjectives;
    protected LinkedList<Objective> currentObjectives;
    protected SimulatorRunner simulatorRunner;
    protected SimulatorOutputParser simulatorOutputParser;
    protected InputData inputData;

    public Simulator(InputData inputData) {
        this.inputData = inputData;
        this.initSimulator();
    }

    public void initSimulator() {
        simulatorOutputFile = "";
        simulatorRunner = new SimulatorRunner(this);
        simulatorOutputParser = new SimulatorOutputParser(this);
    }

    public SimulatorRunner getRunner() {
        return this.simulatorRunner;
    }

    public SimulatorOutputParser getOutputParser() {
        return this.simulatorOutputParser;
    }

    public String getSimulatorOutputFile() {
        return this.simulatorOutputFile;
    }

    public void setSimulatorOutputFile(String value) {
        this.simulatorOutputFile = value;
    }

    public void performSimulation(FadseIndividual individual) {
        System.out.println("\n\n---------- PERFORM SIMULATION ---------");

        simulatorRunner.setParameters(individual.getParameters());

         Map<String,Objective> objectivesMap = (Map<String, Objective>) inputData.get(CommonSetupParameters.OBJECTIVES);

        simulatorOutputParser.setObjectives(objectivesMap);

        LinkedList<Objective> results;

        System.out.println("- simulation has to be run");
        simulatorRunner.setIndividual(individual);
        this.simulatorRunner.run(true);

        results = this.simulatorOutputParser.getResults(individual);
        individual.setObjectives(results);
    }

    public void closeSimulation(FadseIndividual individual) {
        simulatorRunner.stopRunning();
    }

    public InputData getInputData() {
        return inputData;
    }

    @Override
    public String toString() {
        return "not implemented";
    }
}
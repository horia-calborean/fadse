package ro.ulbsibiu.fadse.extended.problems.simulators.msim2;

import java.util.LinkedList;

import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

import ro.ulbsibiu.fadse.environment.Individual;
import ro.ulbsibiu.fadse.environment.Objective;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorBase;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorOutputParser;

import java.io.File;

/**
 * Parser for the output file of the Msim-2 simulator
 *
 * @author Camil
 */
public class Msim2OutputParser extends SimulatorOutputParser {

    private double CPI;
    private boolean infeasible;
    private boolean firstTime = true;

    public Msim2OutputParser(SimulatorBase scbSim) {
        super(scbSim);
        CPI = Double.MAX_VALUE;
        infeasible = false;
    }

    private void analyze(String line, int linenumber) {
        if (line.contains("THROUGHPUT IPC:")) {
            String[] splitLIne = line.split(":");
            if (splitLIne.length == 2) {
                double ipc = Double.parseDouble(splitLIne[1].trim());
                CPI = 1/ipc;
            }

        }
    }

    private String getExtractedObjectiveName() {
        return "CPI";
    }

    private double getExtractedObjectiveValue() {
        return CPI;
    }

    private boolean nextExtractedObjective() {
        if (firstTime) {
            firstTime = false;
            return true;
        }
        return false;
    }

    @Override
    protected void processLine(String textLine, int lineNumber) {
        if (infeasible == false) {
            analyze(textLine, lineNumber);
            infeasible = isInfeasible();
        }
    }

    protected void resetAnalyzer() {
    }

    protected boolean isInfeasible() {
        return false;
    }

    @Override
    public LinkedList<Objective> getResults(Individual individual) {
        this.processFile(individual);
        LinkedList<Objective> finalResults = new LinkedList<Objective>();

        try {
            if (infeasible) {
                individual.markAsInfeasibleAndSetBadValuesForObjectives("An exception occured when parsing the simulator's output.");
                return null;
            }

            String ExtractedObjectiveName;
            double ExtractedObjectiveValue;

            while (nextExtractedObjective()) {
                ExtractedObjectiveName = getExtractedObjectiveName();
                ExtractedObjectiveValue = getExtractedObjectiveValue();

                if (this.isInOutputs(ExtractedObjectiveName)) {
                    this.addSimpleObjective(ExtractedObjectiveName, ExtractedObjectiveValue);
                }
            }
            // Iterate over the objectives enumerated in the config XML
            for (Objective obj : this.currentObjectives) {
                String key = obj.getName();
                if (this.results.containsKey(key)) {
                    obj.setValue(this.results.get(key));
                }
                if (obj.getValue() == 0) {
                    individual.markAsInfeasibleAndSetBadValuesForObjectives("one of the objectives is Double.MAX_VALUE: " + finalResults);
                    setWorstObjectives(finalResults);
                    break;
                }
                finalResults.add(obj);
            }
        } catch (Exception e) {//In case something bad happens here just mark the individual as infeasible
            individual.markAsInfeasibleAndSetBadValuesForObjectives(e.getMessage());
            setWorstObjectives(finalResults);
        }

        if (individual.isFeasible()) {
            (new File(this.simulator.getSimulatorOutputFile())).delete();//DELETE OUTPUT FILE ONLY IF SIMULATION WAS SUCCESFULL
        }

        resetAnalyzer();

        return finalResults;
    }

    /**
     * Adds additional objectives needed to compute the objective received as
     * parameter
     * e.g: powerconsumption = sum(core_0_total_power + ... + core_n_total_power)
     *
     * @param objectiveName
     * @return the list with the additional objectives
     */
    @Override
    protected LinkedList<String> getRealSimulatorObjective(String objectiveName) {
        LinkedList<String> alObjectives = new LinkedList<String>();

        if (objectiveName.equals(Msim2Constants.O_POWERCONSUMPTION)) {
            int coreNumber = 1;
            if (this.simulator.getRunner().getSimpleParameters().containsKey(Msim2Constants.P_NUM_CORES)) {
                coreNumber = Integer.parseInt(this.simulator.getRunner().getSimpleParameters().get(Msim2Constants.P_NUM_CORES));
            }

            for (int core = 0; core < coreNumber; core++) {
                alObjectives.add(Msim2Constants.P_CORE_X_TOTAL_POWER.replace(
                        Msim2Constants.NUMER_REPLACE, Integer.toString(core)));
            }
        } else if (objectiveName.equals(Msim2Constants.O_IPC)) {
            alObjectives.add(Msim2Constants.O_THROUGHPUT_IPC);
        } else {
            alObjectives.add(objectiveName);
        }

        return alObjectives;
    }
}

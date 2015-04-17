package ro.ulbsibiu.fadse.extended.problems.simulators.msim3;

import java.util.LinkedList;

import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

import ro.ulbsibiu.fadse.environment.Individual;
import ro.ulbsibiu.fadse.environment.Objective;
import ro.ulbsibiu.fadse.extended.problems.simulators.Msim3Simulator;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorBase;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorOutputParser;
import ro.ulbsibiu.fadse.extended.problems.simulators.msim3.Msim3PyExtractor;

import java.io.File;

/**
 * Parser for the output file of the Msim-3 simulator
 *
 * @author Camil
 */
public class Msim3OutputParser extends SimulatorOutputParser {

    private boolean infeasible;
    
    private Msim3PyExtractor PyExtractor;

    public Msim3OutputParser(SimulatorBase scbSim) {
        super(scbSim);

        PyExtractor = new Msim3PyExtractor(
        		((Msim3Simulator)this.simulator).getParserOutFile(),
        		((Msim3Simulator)this.simulator).getParserErrFile()
        		);
        PyExtractor.initializeExtractor();
    }


    @Override
    protected void processLine(String textLine, int lineNumber) {
        if (infeasible == false) {
            PyExtractor.processLine(textLine);
            infeasible = isInfeasible();
        }
    }
    
    protected boolean isInfeasible() {
    	if (PyExtractor.getErrorCount().intValue() > 1) {
    		return true;
    	}
    	return false;
    }

    @Override
    public LinkedList<Objective> getResults(Individual individual) {
        LinkedList<Objective> finalResults = new LinkedList<Objective>();
        try {
            this.processFile(individual);
            PyExtractor.finalize();
            if (infeasible) {
                individual.markAsInfeasibleAndSetBadValuesForObjectives("An exception occured when parsing the simulator's output.");
                return null;
            }
            // Iterate over the objectives enumerated in the config XML 
            for (Objective obj : this.currentObjectives) {
                String objectiveName = obj.getName();
                Double Value = PyExtractor.getObjectiveValue(objectiveName);
                if (Value != null) {
                    obj.setValue(Value.doubleValue());
                }
                else {
                	obj.setValue(0);
                }
                if (obj.getValue() == 0) {
                    individual.markAsInfeasibleAndSetBadValuesForObjectives("one of the objectives is Double.MAX_VALUE: " + finalResults);
                    setWorstObjectives(finalResults);
                    break;
                }
                finalResults.add(obj);
            }
        } catch (Exception e) {//In case something bad happens here just mark the individual as infeasible
            individual.markAsInfeasibleAndSetBadValuesForObjectives(e.toString());
            setWorstObjectives(finalResults);
            System.out.println(e);
        }
        if (individual.isFeasible()) {
            (new File(this.simulator.getSimulatorOutputFile())).delete();//DELETE OUTPUT FILE ONLY IF SIMULATION WAS SUCCESFULL
        }

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

        if (objectiveName.equals(Msim3Constants.O_POWERCONSUMPTION)) {
            int coreNumber = 1;
            if (this.simulator.getRunner().getSimpleParameters().containsKey(Msim3Constants.P_NUM_CORES)) {
                coreNumber = Integer.parseInt(this.simulator.getRunner().getSimpleParameters().get(Msim3Constants.P_NUM_CORES));
            }

            for (int core = 0; core < coreNumber; core++) {
                alObjectives.add(Msim3Constants.P_CORE_X_TOTAL_POWER.replace(
                        Msim3Constants.NUMER_REPLACE, Integer.toString(core)));
            }
        } else if (objectiveName.equals(Msim3Constants.O_IPC)) {
            alObjectives.add(Msim3Constants.O_THROUGHPUT_IPC);
        } else {
            alObjectives.add(objectiveName);
        }

        return alObjectives;
    }
}

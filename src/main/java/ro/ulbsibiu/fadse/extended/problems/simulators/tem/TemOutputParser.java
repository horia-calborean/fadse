/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.extended.problems.simulators.tem;

import java.io.File;
import java.util.LinkedList;
import java.util.Scanner;

import ro.ulbsibiu.fadse.environment.Individual;
import ro.ulbsibiu.fadse.environment.Objective;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorBase;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorOutputParser;

/**
 * Parser for the output file of Msim simulator
 * 
 * @author Rolf
 */
public class TemOutputParser extends SimulatorOutputParser {

    /**
     * constructor
     * 
     * @param outputFilePath
     *            the path to the output file that needs processing
     */
    public TemOutputParser(SimulatorBase scbSim) {
        super(scbSim);
    }

    /**
     * Searches for the output result value in a line of text This is depending
     * on the simulator output file format
     * 
     * @param textLine
     *            the line of text where the value is to be searched
     * @param lineNumber
     *            in some cases the line number is necessary for processing
     *            skipping lines, or changing the delimiter (starts at 1)
     */
    @Override
    protected void processLine(String textLine, int lineNumber) {

        Scanner scanner = new Scanner(textLine);
        // this.defaultDelimiter = " \\s*";
        this.defaultDelimiter = ":\\s*";
        scanner.useDelimiter(this.defaultDelimiter);

        if (scanner.hasNext()) {

            String name = scanner.next().trim();
            System.out.println("Token " + name);

            if (this.isInOutputs(name)) {
                System.out.println("Token " + name + " ist ein Input");
                if (scanner.hasNext()) {
                    String value_str = scanner.next();
                    System.out.println("Weiteres Token: " + value_str);
                    double value = Double.parseDouble(value_str);
                    this.addSimpleObjective(name, value);
                }
            }
        }

        scanner.close();
    }

    /**
     * gets the final results
     * 
     * @return
     */
    @Override
    public LinkedList<Objective> getResults(Individual individual) {
        this.file = new File(this.simulator.getSimulatorOutputFile());

        if (!this.file.exists()) {
            individual
                    .markAsInfeasibleAndSetBadValuesForObjectives("Exception while running: File "
                            + this.file.getAbsolutePath() + " does not exist.");
        }

        // TODO: regroup results
        this.processFile(individual);

        LinkedList<Objective> finalResults = new LinkedList<Objective>();
        for (Objective obj : this.currentObjectives) {
            String key = obj.getName();
            if (this.results.containsKey(key)) {
                obj.setValue(this.results.get(key));
            }

            finalResults.add(obj);
        }

        return finalResults;
    }

    /**
     * Adds additional objectives needed to compute the objective received as
     * parameter e.g: powerconsumption = sum(core_0_total_power + ... +
     * core_n_total_power)
     * 
     * @param objectiveName
     * @return the list with the additional objectives
     */
    @Override
    protected LinkedList<String> getRealSimulatorObjective(String objectiveName) {
        LinkedList<String> alObjectives = new LinkedList<String>();

        alObjectives.add(objectiveName);

        return alObjectives;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators.sniper;

import java.io.File;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Scanner;

import ro.ulbsibiu.fadse.environment.Individual;
import ro.ulbsibiu.fadse.environment.Objective;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorBase;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorOutputParser;

/**
 *
 * @author Andrei DAIAN
 * @since 03.05.2013
 * @version 1.0
 */
public class SniperOutputParser extends SimulatorOutputParser {

    public SniperOutputParser(SimulatorBase simulator) {
        super(simulator);
        this.defaultDelimiter = " = ";
    }

    @Override
    protected void processLine(String textLine, int lineNumber) {

        Scanner scanner = new Scanner(textLine).useLocale(Locale.ENGLISH);
        scanner.useDelimiter(this.defaultDelimiter);

        if (scanner.hasNext()) {
            String name = scanner.next().trim();
            if (this.isInOutputs(name)) {
                if (scanner.hasNextDouble()) {
                    double value = scanner.nextDouble();
                    addSimpleObjective(name, value);
                }
            }
        }

        scanner.close();
    }

    @Override
    public LinkedList<Objective> getResults(Individual individual) {

        this.file = new File(this.simulator.getSimulatorOutputFile());
        // Process the file and find some objectives => can be found in this.results
        this.processFile(individual);
        // The return object
        LinkedList<Objective> finalResults = new LinkedList<Objective>();

        // Go through all the objectives and copy them to the return-object finalResults
        try {
            for (Objective obj : this.currentObjectives) {
                String key = obj.getName();
                if (this.results.containsKey(key) && this.results.get(key) != null) {
                    obj.setValue(this.results.get(key));
                    // System.out.println("Found value for " + key + ": " + this.results.get(key));
                } else {
                    individual.markAsInfeasibleAndSetBadValuesForObjectives("Objective " + key + " cannot be found (not existent or null): " + this.results);
                    setWorstObjectives(finalResults);
                    break;
                }

                finalResults.add(obj);
                // System.out.println("Final Results after adding " + obj.getValue() + " for " + obj.getName() + ": " + finalResults);
            }
        } catch (Exception ex) {
            System.err.println("Error while calculating Objective: " + ex.getMessage());
            individual.markAsInfeasibleAndSetBadValuesForObjectives("Error calculating objective: " + ex.getMessage());
            setWorstObjectives(finalResults);
        }

        // Check if one of the values if MAX, then set as infeasible
        for (Objective item : finalResults) {
            if (item.getValue() == 0 || item.getValue() == Double.MAX_VALUE) {
                individual.markAsInfeasibleAndSetBadValuesForObjectives("one of the objectives is zero or Double.MAX_VALUE: " + finalResults);
                setWorstObjectives(finalResults);
                break;
            }
        }

        // If infeasible, then set all values to max.
        if (!individual.isFeasible()) {
            // Set all the objectives to the max available value...
            System.out.println("Individual is infeasible - clear objectives.");
            System.out.println("Note that out files are kept for infeasible individuals.");
            setWorstObjectives(finalResults);
        }

        // System.out.println("I calculated as results: " + finalResults);

        return finalResults;
    }

    @Override
    protected LinkedList<String> getRealSimulatorObjective(String objectiveName) {
        LinkedList<String> alObjectives = new LinkedList<String>();

        alObjectives.add(objectiveName);

        return alObjectives;
    }
}
/*
 * This file is part of the FADSE tool.
 * 
 *   Authors: 	Ciprian Radu {ciprian.radu at ulbsibiu.ro} 
 *   			Horia Andrei Calborean {horia.calborean at ulbsibiu.ro}
 *   Copyright (c) 2009-2011
 *   All rights reserved.
 * 
 *   Redistribution and use in source and binary forms, with or without modification,
 *   are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 * 
 *   The names of its contributors NOT may be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 * 
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *   AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *   THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *   PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 *   OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *   ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 *   OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators.unimap;

import java.io.File;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.environment.Individual;
import ro.ulbsibiu.fadse.environment.Objective;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorBase;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorOutputParser;

/**
 * Output file parser for the ns-3 NoC simulator of UniMap.
 * 
 * @author Ciprian Radu
 * @author Horia Calborean
 */
public class UniMapOutputParser extends SimulatorOutputParser {

	/** application runtime, in seconds */
    public static final String OBJECTIVE_APP_RUNTIME = "application-runtime";
    
    /** System on Chip (SoC) energy, in Joule (includes IP cores and Network-on-Chip) */
    public static final String OBJECTIVE_ENERGY = "soc-energy";
    
    /** System on Chip (SoC) area, in mm^2 (includes IP cores and Network-on-Chip) */
    public static final String OBJECTIVE_AREA = "soc-area";

    private FileRemover fileRemover;
    
    /**
     * Constructor
     * 
     * @param simulator the simulator
     */
    public UniMapOutputParser(SimulatorBase simulator, FileRemover fileRemover) {
        super(simulator);
        this.defaultDelimiter = " = ";
        this.fileRemover = fileRemover;
    }

    /* (non-Javadoc)
     * @see ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorOutputParser#getResults(environment.Individual)
     */
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
//                    System.out.println("Found value for " + key + ": " + this.results.get(key));
                } else {
                    individual.markAsInfeasibleAndSetBadValuesForObjectives("Objective " + key + " cannot be found (not existent or null): " + this.results);
                    setWorstObjectives(finalResults);
                    break;
                }

                finalResults.add(obj);
//                System.out.println("Final Results after adding " + obj.getValue() + " for " + obj.getName() + ": " + finalResults);
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
        } else {
        	Environment environment = individual.getEnvironment();
        	Map<String, String> simulatorParameters = environment.getInputDocument().getSimulatorParameters();
        	String outFilePath = simulatorParameters.get("simulator_output_file");
			if (outFilePath != null && !outFilePath.isEmpty()) {
				fileRemover.addFileToBeCleaned(outFilePath);
				fileRemover.addFileToBeCleaned(outFilePath + ".out.log");
				fileRemover.addFileToBeCleaned(outFilePath + ".err.log");
				fileRemover.addFileToBeCleaned(outFilePath + ".db");
			} else {
				System.out.println("Could not remove UniMap ns-3 NoC output files because simulator_output_file is unspecified");
			}
        	fileRemover.removeFiles();
        }

//        System.out.println("I calculated as results: " + finalResults);

        return finalResults;
    }

    /* (non-Javadoc)
     * @see ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorOutputParser#processLine(java.lang.String, int)
     */
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
}

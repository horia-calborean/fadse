package ro.ulbsibiu.fadse.extended.problems.simulators;

import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.util.Scanner;
import java.util.LinkedList;
import java.util.Locale;

import ro.ulbsibiu.fadse.environment.Individual;
import ro.ulbsibiu.fadse.environment.Objective;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorBase;
import ro.ulbsibiu.fadse.persistence.Result;

/**
 * Generic class for parsing output file of a simulator
 *
 * @author Andrei
 * @version 0.1
 * @since 15.04.2010
 */
public class SimulatorOutputParser {

    /** Delimiter to separate keys from values in the result file */
    protected String defaultDelimiter;
    protected SimulatorBase simulator;
    protected Map<String, Double> results;
    protected LinkedList<Objective> currentObjectives;
    public StringBuilder fileContents;
    /** The file which has to be parsed to get results and basic objectives */
    protected File file;

    /**
     * constructor
     * @param outputFilePath  the path to the output file that needs processing
     */
    public SimulatorOutputParser(SimulatorBase simulator) {
        this.simulator = simulator;
        this.defaultDelimiter = ":\\s+";
        this.currentObjectives = new LinkedList<Objective>();
        this.results = new HashMap<String, Double>();
        file = new File(this.simulator.getSimulatorOutputFile());
    }

    /**
     * tests if the output value is in the needed values
     * @param name the keyname
     * @return true if the value is found, false otherwise
     */
    protected boolean isInOutputs(String name) {
        return this.results.containsKey(name);
    }

    // <editor-fold defaultstate="collapsed" desc="Objectives Functions" >
    /**
     * OBJECTIVE FUNCTIONS
     * all functions needed for converting between base objectives and current
     * simulator output
     */
    public void setObjectives(LinkedList<Objective> objectives) {
        this.currentObjectives = objectives;
        this.prepareObjectives();
    }

    public void setObjectives(Map<String, Objective> objectives) {
        this.currentObjectives = new LinkedList<Objective>();
        for (Map.Entry<String, Objective> entry : objectives.entrySet()) {
            this.currentObjectives.add(entry.getValue());
        }

        this.prepareObjectives();
    }

    /**
     * adds a output value to this.simpleObjectives Hashmap
     * @param name  the name of the output value to be returned
     */
    public void addSimpleObjective(String name, double value) {
        System.out.println("- Add Objective: " + name + " " + value);
        this.results.put(name, value);
    }

    /**
     * Return the objectives specific for the simulator
     * @return Map<String, String>
     */
    public Map<String, Double> getSimpleObjectives() {
        return this.results;
    }

    /**
     * gets the final results
     * @return
     */
    public LinkedList<Objective> getResults(Individual individual) {
        this.processFile(individual);
        LinkedList<Objective> finalResults = new LinkedList<Objective>();

        for (Objective obj : this.currentObjectives) {
            String key = obj.getName();
            if (this.results.containsKey(key)) {
                obj.setValue(this.results.get(key));
            } else {
                individual.markAsInfeasibleAndSetBadValuesForObjectives("Objective " + key + " cannot be found (not existent or null): " + this.results);
                setWorstObjectives(finalResults);
                break;
            }
            finalResults.add(obj);
        }

        // Check if one of the values if MAX, then set as infeasible
        for (Objective item : finalResults) {
            if (item.getValue() == Double.MAX_VALUE) {
                individual.markAsInfeasibleAndSetBadValuesForObjectives("one of the objectives is Double.MAX_VALUE: " + finalResults);
                setWorstObjectives(finalResults);
                break;
            }
        }

        // If infeasible, then set all values to max.
        if (!individual.isFeasible()) {
            // Set all the objectives to the max available value...
            System.out.println("Individual is infeasible - clear objectives.");
            setWorstObjectives(finalResults);
        }

        return finalResults;
    }

    /**
     * depending on the simulator, this function will specify all the output
     * measures needed for calculating the needed objective
     * @param objectiveName
     * @return an ArrayList&lt;String&gt; containing the real output measures
     */
    protected LinkedList<String> getRealSimulatorObjective(String objectiveName) {
        LinkedList<String> alObjectives = new LinkedList();
        alObjectives.add(objectiveName);
        return alObjectives;
    }

    /**
     * Creates a simple map (easy to work with) collection of objectives
     * all as simple strings
     */
    protected void prepareObjectives() {
        this.results = new HashMap<String, Double>();
        for (Objective objTemp : this.currentObjectives) {
            LinkedList<String> realObjective = this.getRealSimulatorObjective(objTemp.getName());
            for (String sObj : realObjective) {
                this.addSimpleObjective(sObj, 0.0);
            }
        }
        // System.out.println("Preparing parameters:");
        // System.out.println(this.results);
    }

    // </editor-fold>
    /**
     * Reads the output file line by line and calls the line processing
     * function that should search for the output values {@link #processLine(String, int)}
     */
    protected void processFile(Individual individual) {
        this.results = this.getSimpleObjectives();

        try {
            // if there is a saved element in database then use the scanner
            // on the text from database
            boolean inTheDatabase = false;
            Scanner scanner;
            fileContents = new StringBuilder();

            String dbResult = null;
			try {
                dbResult = Result.getTextResuls(this.simulator.getInputDocument(),
                individual);
        	} catch (Exception e) {
            	System.out.println("ERROR WHILE ACCESING THE DATABASE");
            	e.printStackTrace();
        	}

            // if the result file is already in the database
            if (dbResult != null) {
                // use the text returned from db
                System.out.println("Using Saved Output");
                scanner = new Scanner(dbResult);
                inTheDatabase = true;
            } else {
                // else use it on the file
                System.out.println("Using Output file");
                scanner = new Scanner(this.file);
            }

            // Read output file line by line and look for objectives...
            int currentLine = 0;
            try {
                while (scanner.hasNextLine()) {
                    // Read a single line from the the result file
                    String line = scanner.nextLine();

                    // System.out.println("\n" + line)  ;

                    // Append a line-breakt to the line to recreate the content
                    // of the result file to store it later in the database
                    if (!inTheDatabase) {
                        fileContents.append(line + "\n");
                    }

                    // Handle this single line
                    this.processLine(line.trim(), ++currentLine);
                }
            } finally {
                scanner.close();
            }


//            persistence.DerbyDB.closeConnection(individual.getenvironment().getInputDocument());
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Searches for the output result value in a line of text
     * This is depending on the simulator output file format
     * It uses this.defaultDelimiter so separate keys from values
     * @param textLine    the line of text where the value is to be searched
     * @param lineNumber  in some cases the line number is necessary for processing
     *                    skipping lines, or changing the delimiter
     */
    protected void processLine(String textLine, int lineNumber) {
        Scanner scanner = new Scanner(textLine).useLocale(Locale.ENGLISH);
        scanner.useDelimiter(this.defaultDelimiter);

        if (scanner.hasNext()) {
            String name = scanner.next().trim();
            if (this.isInOutputs(name)) {
                if (scanner.hasNextFloat()) {
                    float value = scanner.nextFloat();
                    addSimpleObjective(name, value);
                }
            }
        }

        scanner.close();
    }

    protected void setWorstObjectives(LinkedList<Objective> finalResults) {
        finalResults.clear();
        for (Objective item : this.currentObjectives) {
            item.setValue(Double.MAX_VALUE);
            finalResults.add(item);
        }
    }
}

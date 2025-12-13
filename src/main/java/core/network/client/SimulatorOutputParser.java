package core.network.client;

import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.util.Objects;
import java.util.Scanner;
import java.util.LinkedList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import core.model.individual.FadseIndividual;
import core.model.objectives.Objective;

/**
 * SimulatorOutputParser - Parses simulation output files and extracts objectives.
 *
 * Improvements:
 * - Proper resource management (Scanner always closed)
 * - Better logging instead of System.out
 * - Null validation
 * - File reference updated when needed
 * - Removed dead code
 */
public class SimulatorOutputParser {
    private static final Logger LOGGER = Logger.getLogger(SimulatorOutputParser.class.getName());

    protected String defaultDelimiter;
    protected Simulator simulator;
    protected Map<String, Double> results;
    protected LinkedList<Objective> currentObjectives;
    public StringBuilder fileContents;
    protected File file;  // File reference for subclasses (updated before use)

    public SimulatorOutputParser(Simulator simulator) {
        this.simulator = Objects.requireNonNull(simulator, "Simulator cannot be null");
        this.defaultDelimiter = ":\\s+";
        this.currentObjectives = new LinkedList<>();
        this.results = new HashMap<>();
        this.file = null;  // Will be set when needed
    }

    protected boolean isInOutputs(String name) {
        return this.results.containsKey(name);
    }

    public void setObjectives(LinkedList<Objective> objectives) {
        this.currentObjectives = objectives != null ? objectives : new LinkedList<>();
        this.prepareObjectives();
    }

    public void setObjectives(Map<String, Objective> objectives) {
        this.currentObjectives = new LinkedList<>();
        if (objectives != null) {
            for (Map.Entry<String, Objective> entry : objectives.entrySet()) {
                this.currentObjectives.add(entry.getValue());
            }
        }
        this.prepareObjectives();
    }

    public void addSimpleObjective(String name, double value) {
        LOGGER.log(Level.FINE, String.format("Add Objective: %s = %.4f", name, value));
        this.results.put(name, value);
    }

    public Map<String, Double> getSimpleObjectives() {
        return this.results;
    }

    public LinkedList<Objective> getResults(FadseIndividual individual) {
        processFile(individual);
        LinkedList<Objective> finalResults = new LinkedList<>();

        // Check if all objectives were found
        for (Objective obj : this.currentObjectives) {
            String key = obj.getName();
            if (this.results.containsKey(key)) {
                obj.setValue(this.results.get(key));
                finalResults.add(obj);
            } else {
                LOGGER.log(Level.WARNING, String.format(
                    "Objective '%s' not found in simulation output", key
                ));
                individual.setBadValuesForObjectives();
                setWorstObjectives(finalResults);
                return finalResults;
            }
        }

        // Check if any objective has worst value (MAX_VALUE)
        for (Objective item : finalResults) {
            if (item.getValue() == Double.MAX_VALUE) {
                LOGGER.log(Level.WARNING, String.format(
                    "Objective '%s' has worst value (MAX_VALUE)", item.getName()
                ));
                individual.setBadValuesForObjectives();
                setWorstObjectives(finalResults);
                return finalResults;
            }
        }

        // Check if individual is feasible
        if (!individual.isFeasible()) {
            LOGGER.log(Level.WARNING, "Individual is infeasible - setting worst objectives");
            setWorstObjectives(finalResults);
        }

        return finalResults;
    }

    protected LinkedList<String> getRealSimulatorObjective(String objectiveName) {
        LinkedList<String> alObjectives = new LinkedList<>();
        alObjectives.add(objectiveName);
        return alObjectives;
    }

    protected void prepareObjectives() {
        this.results = new HashMap<>();
        for (Objective objTemp : this.currentObjectives) {
            LinkedList<String> realObjective = this.getRealSimulatorObjective(objTemp.getName());
            for (String sObj : realObjective) {
                this.addSimpleObjective(sObj, 0.0);
            }
        }
    }

    protected void processFile(FadseIndividual individual) {
        this.results = this.getSimpleObjectives();

        // Use this.file if it was set by subclass (e.g., GAPOutputParser does directory navigation)
        // Otherwise, get fresh path from simulator
        File file = this.file;

        if (file == null) {
            String outputFilePath = simulator.getSimulatorOutputFile();
            if (outputFilePath == null || outputFilePath.trim().isEmpty()) {
                LOGGER.log(Level.SEVERE, "Simulator output file not configured");
                return;
            }
            file = new File(outputFilePath);
        }

        if (!file.exists()) {
            LOGGER.log(Level.SEVERE, String.format("Output file does not exist: %s", file.getAbsolutePath()));
            return;
        }

        Scanner scanner = null;
        try {
            fileContents = new StringBuilder();
            scanner = new Scanner(file);

            LOGGER.log(Level.INFO, "Parsing output file: " + file.getAbsolutePath());

            // Read output file line by line and look for objectives...
            int currentLine = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                fileContents.append(line).append("\n");
                this.processLine(line.trim(), ++currentLine);
            }

            LOGGER.log(Level.INFO, String.format("Parsed %d lines from output file", currentLine));

        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, String.format("File not found: %s", file.getAbsolutePath()), ex);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    protected void processLine(String textLine, int lineNumber) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(textLine).useLocale(Locale.ENGLISH);
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
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    protected void setWorstObjectives(LinkedList<Objective> finalResults) {
        finalResults.clear();
        for (Objective item : this.currentObjectives) {
            item.setValue(Double.MAX_VALUE);
            finalResults.add(item);
        }
    }
}
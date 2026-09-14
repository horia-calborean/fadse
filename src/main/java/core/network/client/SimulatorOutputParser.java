package core.network.client;

import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.util.Scanner;
import java.util.LinkedList;
import java.util.Locale;

import core.model.individual.FadseIndividual;
import core.model.objectives.Objective;

public class SimulatorOutputParser {

    protected String defaultDelimiter;
    protected Simulator simulator;
    protected Map<String, Double> results;
    protected LinkedList<Objective> currentObjectives;
    public StringBuilder fileContents;
    protected File file;

    public SimulatorOutputParser(Simulator simulator) {
        this.simulator = simulator;
        this.defaultDelimiter = ":\\s+";
        this.currentObjectives = new LinkedList<>();
        this.results = new HashMap<>();
        file = new File(this.simulator.getSimulatorOutputFile());
    }

    protected boolean isInOutputs(String name) {
        return this.results.containsKey(name);
    }

    public void setObjectives(LinkedList<Objective> objectives) {
        this.currentObjectives = objectives;
        this.prepareObjectives();
    }

    public void setObjectives(Map<String, Objective> objectives) {
        this.currentObjectives = new LinkedList<>();
        for (Map.Entry<String, Objective> entry : objectives.entrySet()) {
            this.currentObjectives.add(entry.getValue());
        }

        this.prepareObjectives();
    }

    public void addSimpleObjective(String name, double value) {
        System.out.println("- Add Objective: " + name + " " + value);
        this.results.put(name, value);
    }

    public Map<String, Double> getSimpleObjectives() {
        return this.results;
    }

    public LinkedList<Objective> getResults(FadseIndividual individual) {
        processFile(individual);
        LinkedList<Objective> finalResults = new LinkedList<>();

        for (Objective obj : this.currentObjectives) {
            String key = obj.getName();
            if (this.results.containsKey(key)) {
                obj.setValue(this.results.get(key));
            } else {
                individual.setBadValuesForObjectives();
                setWorstObjectives(finalResults);
                break;
            }
            finalResults.add(obj);
        }

        for (Objective item : finalResults) {
            if (item.getValue() == Double.MAX_VALUE) {
                individual.setBadValuesForObjectives();
                setWorstObjectives(finalResults);
                break;
            }
        }

        if (!individual.isFeasible()) {
            System.out.println("Individual is infeasible - clear objectives.");
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

        try {
            // if there is a saved element in database then use the scanner
            // on the text from database
            boolean inTheDatabase = false;
            Scanner scanner;
            fileContents = new StringBuilder();

            String dbResult = null;

            System.out.println("Using Output file");
            scanner = new Scanner(this.file);

            // Read output file line by line and look for objectives...
            int currentLine = 0;
            try {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();

                    if (!inTheDatabase) {
                        fileContents.append(line).append("\n");
                    }

                    this.processLine(line.trim(), ++currentLine);
                }
            } finally {
                scanner.close();
            }


        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

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
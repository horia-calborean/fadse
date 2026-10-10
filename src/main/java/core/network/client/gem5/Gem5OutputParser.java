package core.network.client.gem5;

import core.model.objectives.Objective;
import core.model.individual.FadseIndividual;
import core.network.client.Simulator;
import core.network.client.SimulatorOutputParser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map;

public class Gem5OutputParser extends SimulatorOutputParser {
    private final Set<String> objectiveNames;
    private Map<String, Double> currentDump;
    private Map<String, Double> lastCompleteDump;
    private boolean framedStats;

    public Gem5OutputParser(Simulator simulator) {
        super(simulator);
        this.objectiveNames = new HashSet<>();
    }

    @Override
    protected void processFile(FadseIndividual individual) {
        results.clear();
        currentDump = null;
        lastCompleteDump = new HashMap<>();
        framedStats = false;
        super.processFile(individual);
        if (framedStats) {
            results.clear();
            results.putAll(lastCompleteDump);
        }
    }

    @Override
    protected void prepareObjectives() {
        this.results = new HashMap<>();
        this.objectiveNames.clear();
        for (Objective objective : this.currentObjectives) {
            this.objectiveNames.add(objective.getName());
        }
    }

    @Override
    protected void processLine(String textLine, int lineNumber) {
        if (textLine == null) {
            return;
        }

        String trimmedLine = textLine.trim();
        if (trimmedLine.startsWith("---------- Begin Simulation Statistics")) {
            framedStats = true;
            currentDump = new HashMap<>();
            return;
        }
        if (trimmedLine.startsWith("---------- End Simulation Statistics")) {
            if (currentDump != null) lastCompleteDump = currentDump;
            currentDump = null;
            return;
        }
        if (trimmedLine.isEmpty()
                || trimmedLine.startsWith("#")
                || trimmedLine.startsWith("-")) {
            return;
        }

        ParsedMetric metric = parseMetric(trimmedLine);
        if (metric == null || !objectiveNames.contains(metric.name)) {
            return;
        }

        Map<String, Double> destination = framedStats ? currentDump : results;
        try {
            double value = Double.parseDouble(metric.value);
            if (destination != null) {
                if (Double.isFinite(value)) destination.put(metric.name, value);
                else destination.remove(metric.name);
            }
        } catch (NumberFormatException ignored) {
            if (destination != null) destination.remove(metric.name);
            // gem5 emits comments and occasional non-numeric tokens; only numeric stats feed objectives.
        }
    }

    private static ParsedMetric parseMetric(String line) {
        int assignmentIndex = firstAssignmentIndex(line);
        if (assignmentIndex > 0) {
            String name = line.substring(0, assignmentIndex).trim();
            String value = firstToken(line.substring(assignmentIndex + 1).trim());
            return new ParsedMetric(name, value);
        }

        String[] tokens = line.split("\\s+");
        if (tokens.length < 2) {
            return null;
        }
        return new ParsedMetric(tokens[0], tokens[1]);
    }

    private static int firstAssignmentIndex(String line) {
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '#') return -1;
            if (ch == '=' || (ch == ':' && (i == 0 || line.charAt(i - 1) != ':')
                    && (i + 1 == line.length() || line.charAt(i + 1) != ':'))) return i;
            if (Character.isWhitespace(ch)) {
                int next = i;
                while (next < line.length() && Character.isWhitespace(line.charAt(next))) next++;
                if (next < line.length() && (line.charAt(next) == '=' || line.charAt(next) == ':')) return next;
                return -1;
            }
        }
        return -1;
    }

    private static String firstToken(String text) {
        if (text.isEmpty()) {
            return text;
        }
        return text.split("\\s+")[0];
    }

    @Override
    protected void setWorstObjectives(LinkedList<Objective> finalResults) {
        finalResults.clear();
        for (Objective objective : this.currentObjectives) {
            objective.setValue(objective.isMinimized() ? Double.MAX_VALUE : Double.MIN_VALUE);
            finalResults.add(objective);
        }
    }

    private static class ParsedMetric {
        private final String name;
        private final String value;

        ParsedMetric(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}

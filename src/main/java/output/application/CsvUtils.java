package output.application;

import core.model.objectives.Objective;
import input.ports.parameter.problem.ProblemParameter;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.errorchecking.JMetalException;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CsvUtils {
    public static <S extends Solution<?>> String generateCSV(List<S> population) {
        StringBuilder csvOutput = new StringBuilder();

        for (S individual : population) {
            StringBuilder csvLine = new StringBuilder();
            List<?> individualVariables = individual.variables();

            for (Object variable : individualVariables) {
                try {
                    csvLine.append(variable).append(",");
                } catch (JMetalException ex) {
                    Logger.getLogger(CsvUtils.class.getName()).log(Level.SEVERE, null, ex);
                    csvLine.append("unknown" + ",");
                }
            }

            int numberOfObjectives = individual.objectives().length;

            for (int objectiveIndex = 0; objectiveIndex < numberOfObjectives; objectiveIndex++) {
                double objVal = individual.objectives()[objectiveIndex];
                csvLine.append(objVal).append(",");
            }

            csvLine = new StringBuilder(csvLine.substring(0, csvLine.length() - 1));
            csvLine.append(System.lineSeparator());
            csvOutput.append(csvLine);
        }
        return csvOutput.toString();
    }

    public static String generateCSVHeader(ProblemParameter<?>[] designVariables, Map<String, Objective> objectives) {
        StringBuilder header = new StringBuilder();

        for (ProblemParameter<?> parameter : designVariables) {
            header.append(parameter.getName()).append(",");
        }

        for (Objective o : objectives.values()) {
            header.append(o.getName()).append(",");
        }

        header = new StringBuilder(header.substring(0, header.length() - 1));
        header.append(System.lineSeparator());
        return header.toString();
    }
}
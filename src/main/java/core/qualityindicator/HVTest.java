package core.qualityindicator;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import output.application.CsvUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HVTest {
    private static final Logger LOGGER = Logger.getLogger(HVTest.class.getName());

    public static void main(String[] args) {
        String filePath = "src/main/java/core/qualityindicator/fadse.xlsx"; // Path to the Excel file with all generations
        String outputFilePath = "hypervolume_results.xlsx"; // Output file for HV values

        try {
            HypervolumeIndicator<Solution<?>> hvIndicator = new HypervolumeIndicator<>(2);

            // Step 1: Read initial population to set fixed reference point
            List<Solution<?>> initialPopulationEvaluated = readSolutionsFromSheet(filePath, "initial pop evaluated");

            if (initialPopulationEvaluated.isEmpty()) {
                LOGGER.log(Level.SEVERE, "Initial population is empty. Cannot proceed.");
                return;
            }

            // Set fixed reference point from initial population (2.15x margin)
            hvIndicator.setFixedReferencePointFromPopulation(initialPopulationEvaluated, 2.15);
            LOGGER.log(Level.INFO, "Fixed reference point set from initial population");

            // Step 2: Get all sheet names from the workbook
            List<String> sheetNames = getAllSheetNames(filePath);

            // Step 3: Process each generation sheet
            int generation = 0;
            for (String sheetName : sheetNames) {
                // Skip non-generation sheets
                if (sheetName.equals("initial pop non-evaluated") ||
                    sheetName.equals("Hypervolume") ||
                    !sheetName.startsWith("pop after gen") && !sheetName.equals("initial pop evaluated")) {
                    continue;
                }

                LOGGER.log(Level.INFO, "Processing sheet: " + sheetName);

                // Read current generation from sheet
                List<Solution<?>> currentGeneration = readSolutionsFromSheet(filePath, sheetName);

                if (currentGeneration.isEmpty()) {
                    LOGGER.log(Level.WARNING, "Generation " + generation + " is empty, skipping");
                    continue;
                }

                // Extract Pareto front
                List<Solution<?>> paretoFront = SolutionListUtils.getNonDominatedSolutions(currentGeneration);

                // Calculate hypervolume
                double hvValue = hvIndicator.calculateNormalizedHypervolume(paretoFront);

                LOGGER.log(Level.INFO, String.format("Generation %d: HV = %.6f (Pareto size: %d/%d)",
                    generation, hvValue, paretoFront.size(), currentGeneration.size()));

                // Append to output file
                CsvUtils.appendValue("Hypervolume", generation, hvValue, outputFilePath);

                generation++;

                if(generation == 48){
                    CsvUtils.writeExcel(paretoFront, "pareto front", outputFilePath);
                }
            }

            //TODO: FOR THE LAST GENERATION, PRINT IN A SEPARATE SHEET THE PARETO FRONT OBJECTIVES

            LOGGER.log(Level.INFO, "Hypervolume calculation complete. Results saved to: " + outputFilePath);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing hypervolume: " + e.getMessage(), e);
        }
    }

    /**
     * Reads solutions from a specific sheet in an Excel workbook.
     * Expected format: index | variable[0] | variable[1] | ... | objective[0] | objective[1] | ...
     *
     * @param filePath Path to the Excel file
     * @param sheetName Name of the sheet to read
     * @return List of solutions read from the sheet
     */
    private static List<Solution<?>> readSolutionsFromSheet(String filePath, String sheetName) throws IOException {
        List<Solution<?>> solutions = new ArrayList<>();

        File file = new File(filePath);
        if (!file.exists()) {
            LOGGER.log(Level.SEVERE, "File not found: " + filePath);
            return solutions;
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                LOGGER.log(Level.WARNING, "Sheet not found: " + sheetName);
                return solutions;
            }

            // Read header to determine number of variables and objectives
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                LOGGER.log(Level.WARNING, "Header row is missing in sheet: " + sheetName);
                return solutions;
            }

            int numVariables = 0;
            int numObjectives = 0;

            for (Cell cell : headerRow) {
                String headerValue = cell.getStringCellValue();
                if (headerValue.startsWith("variable[")) {
                    numVariables++;
                } else if (headerValue.startsWith("objective")) {
                    numObjectives++;
                }
            }

            LOGGER.log(Level.FINE, String.format("Sheet %s: %d variables, %d objectives",
                sheetName, numVariables, numObjectives));

            // Read data rows (skip header)
            for (int i = 1; i < sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // Create a simple solution that only holds objectives
                    SimpleSolution solution = new SimpleSolution(numVariables, numObjectives);

                    // Skip index column (column 0)
                    int cellIndex = 1;

                    // Read variables
                    for (int v = 0; v < numVariables; v++) {
                        Cell cell = row.getCell(cellIndex++);
                        if (cell != null) {
                            int value = (int) getNumericCellValue(cell);
                            solution.setVariable(v, value);
                        }
                    }

                    // Read objectives
                    for (int o = 0; o < numObjectives; o++) {
                        Cell cell = row.getCell(cellIndex++);
                        if (cell != null) {
                            double value = getNumericCellValue(cell);
                            solution.setObjective(o, value);
                        }
                    }

                    solutions.add(solution);

                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error reading row " + i + " in sheet " + sheetName + ": " + e.getMessage());
                }
            }
        }

        return solutions;
    }

    /**
     * Gets all sheet names from an Excel workbook.
     */
    private static List<String> getAllSheetNames(String filePath) throws IOException {
        List<String> sheetNames = new ArrayList<>();

        File file = new File(filePath);
        if (!file.exists()) {
            LOGGER.log(Level.SEVERE, "File not found: " + filePath);
            return sheetNames;
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            int numberOfSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                sheetNames.add(workbook.getSheetName(i));
            }
        }

        return sheetNames;
    }

    /**
     * Safely extracts numeric value from a cell regardless of cell type.
     */
    private static double getNumericCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            case FORMULA:
                try {
                    return cell.getNumericCellValue();
                } catch (Exception e) {
                    return 0.0;
                }
            default:
                return 0.0;
        }
    }

    /**
     * Simple solution implementation that only stores objectives (for hypervolume calculation).
     * Variables are stored but not used in hypervolume computation.
     */
    static class SimpleSolution implements Solution<Integer> {
        private final List<Integer> variables;
        private final double[] objectives;

        public SimpleSolution(int numVariables, int numObjectives) {
            this.variables = new ArrayList<>(numVariables);
            for (int i = 0; i < numVariables; i++) {
                this.variables.add(0);
            }
            this.objectives = new double[numObjectives];
        }

        public void setVariable(int index, Integer value) {
            variables.set(index, value);
        }

        public void setObjective(int index, double value) {
            objectives[index] = value;
        }

        @Override
        public List<Integer> variables() {
            return variables;
        }

        @Override
        public double[] objectives() {
            return objectives;
        }

        @Override
        public double[] constraints() {
            return new double[0];
        }

        @Override
        public Map<Object, Object> attributes() {
            return new java.util.HashMap<>();
        }

        @Override
        public Solution<Integer> copy() {
            SimpleSolution copy = new SimpleSolution(variables.size(), objectives.length);
            for (int i = 0; i < variables.size(); i++) {
                copy.setVariable(i, variables.get(i));
            }
            for (int i = 0; i < objectives.length; i++) {
                copy.setObjective(i, objectives[i]);
            }
            return copy;
        }
    }
}

package output.application;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import core.model.objectives.Objective;
import input.ports.parameter.problem.ProblemParameter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.errorchecking.JMetalException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

    public static void writeExcel(List<? extends Solution<?>> population, String title, String filePath) {
        Workbook workbook;

        File file = new File(filePath);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                workbook = WorkbookFactory.create(fis);
            } catch (IOException e) {
                e.fillInStackTrace();
                return;
            }
        } else {
            workbook = new XSSFWorkbook();
        }

        Sheet sheet = workbook.createSheet(title);

        CellStyle cellStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);

        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);
        int numVariables = population.get(0).variables().size();
        int numObjectives = population.get(0).objectives().length;

        int colNum = 0;
        Cell headerCell = headerRow.createCell(colNum++);
        headerCell.setCellValue("index");
        headerCell.setCellStyle(cellStyle);

        for (int i = 0; i < numVariables; i++) {
            Cell cell = headerRow.createCell(colNum++);
            cell.setCellValue("variable[" + i + "]");
            cell.setCellStyle(cellStyle);
        }
        for (int i = 0; i < numObjectives; i++) {
            Cell cell = headerRow.createCell(colNum++);
            cell.setCellValue("objective [" + i + "]");
            cell.setCellStyle(cellStyle);
        }

        int index = 0;
        for (Solution<?> solution : population) {
            Row row = sheet.createRow(rowNum++);
            int cellNum = 0;

            Cell indexCell = row.createCell(cellNum++);
            indexCell.setCellValue(index++);
            indexCell.setCellStyle(cellStyle);

            for (int i = 0; i < numVariables; i++) {
                Object var = solution.variables().get(i);
                double roundedVar = ((Number) var).doubleValue();
                roundedVar = Math.round(roundedVar * 100.0) / 100.0;
                Cell cell = row.createCell(cellNum++);
                cell.setCellValue(roundedVar);
                cell.setCellStyle(cellStyle);
            }

            for (int i = 0; i < numObjectives; i++) {
                double obj = solution.objectives()[i];
                double roundedObj = Math.round(obj * 100.0) / 100.0;
                Cell cell = row.createCell(cellNum++);
                cell.setCellValue(roundedObj);
                cell.setCellStyle(cellStyle);
            }
        }

        for (int i = 0; i < colNum; i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
            workbook.close();
        } catch (IOException e) {
            e.fillInStackTrace();
        }
    }
}
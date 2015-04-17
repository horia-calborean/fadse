package ro.ulbsibiu.fadse.environment;

import java.io.File;
import java.io.Serializable;

import ro.ulbsibiu.fadse.environment.document.InputDocument;
import ro.ulbsibiu.fadse.environment.parameters.CheckpointFileParameter;
import ro.ulbsibiu.fadse.io.XMLInputReader;
import ro.ulbsibiu.fadse.persistence.ConnectionPool;

public class Environment implements Serializable {

    private String neighborsConfigFile;
    private CheckpointFileParameter checkpointFileParam;
    private String fuzzyInputFile;
    private InputDocument inputDocument;
    private String resultsFolder;

    public Environment(String inputFilePath) {
        inputDocument = (new XMLInputReader()).parse(inputFilePath);
        ConnectionPool.setInputDocument(inputDocument);
        String currentdir = System.getProperty("user.dir");
        File dir = new File(currentdir);
        File resultsFolderFile = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "results" + System.currentTimeMillis());
        if (resultsFolderFile.mkdir()) {
            resultsFolder = resultsFolderFile.getAbsolutePath();
            System.out.println("Results folder: " + resultsFolder);
        } else {
            System.out.println("COULD NOT CREATE RESULTS FOLDER");
            System.exit(1);
        }
    }

    public String getNeighborsConfigFile() {
        return neighborsConfigFile;
    }

    public void setNeighborsConfigFile(String neighborsConfigFile) {
        this.neighborsConfigFile = neighborsConfigFile;
    }

    public InputDocument getInputDocument() {
        return inputDocument;
    }

    public void setInputDocument(InputDocument inputDocument) {
        this.inputDocument = inputDocument;
    }

    public CheckpointFileParameter getCheckpointFileParameter() {
        return checkpointFileParam;
    }

    public void setCheckpointFileParameter(CheckpointFileParameter checkpointFileParam) {
        this.checkpointFileParam = checkpointFileParam;
    }

    public String getResultsFolder() {
        return resultsFolder;
    }

    public void setResultsFolder(String resultsFolder) {
        this.resultsFolder = resultsFolder;
    }

    public String getFuzzyInputFile() {
        return fuzzyInputFile;
    }

    public void setFuzzyInputFile(String fuzzyInputFile) {
        this.fuzzyInputFile = fuzzyInputFile;
    }

    
}

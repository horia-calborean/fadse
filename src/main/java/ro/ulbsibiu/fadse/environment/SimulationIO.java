package ro.ulbsibiu.fadse.environment;

import java.io.File;
import java.io.Serializable;
import java.nio.file.FileSystems;

import ro.ulbsibiu.fadse.environment.document.InputDocument;
import ro.ulbsibiu.fadse.environment.parameters.CheckpointFileParameter;
import ro.ulbsibiu.fadse.io.XmlInputReader;
import ro.ulbsibiu.fadse.persistence.ConnectionPool;

public class SimulationIO implements Serializable {
    protected InputDocument designSpaceDocument;
    protected String resultsFolderPath;
    protected String clientsConfigFilePath;
    protected CheckpointFileParameter checkpointFileParameter;
    protected String fuzzyInputFilePath;

    public SimulationIO(String designSpaceConfigFilePath) {
        designSpaceDocument = (new XmlInputReader()).parse(designSpaceConfigFilePath);
        ConnectionPool.setInputDocument(designSpaceDocument);

        String currentDirPath = System.getProperty("user.dir");
        String fileSeparator = FileSystems.getDefault().getSeparator();
        long currentTime = System.currentTimeMillis();
        File resultsFolderFile = new File(currentDirPath + fileSeparator + "results" + currentTime);

        if (resultsFolderFile.mkdir()) {
            resultsFolderPath = resultsFolderFile.getAbsolutePath();
            System.out.println("Results folder created in: " + resultsFolderPath);
        } else {
            System.out.println("COULD NOT CREATE RESULTS FOLDER!");
            System.exit(1);
        }
    }

    public String getClientsConfigFilePath() {
        return clientsConfigFilePath;
    }

    public void setClientsConfigFilePath(String clientsConfigFilePath) {
        this.clientsConfigFilePath = clientsConfigFilePath;
    }

    public InputDocument getDesignSpaceDocument() {
        return designSpaceDocument;
    }

    public CheckpointFileParameter getCheckpointFileParameter() {
        return checkpointFileParameter;
    }

    public void setCheckpointFileParameter(CheckpointFileParameter checkpointFileParameter) {
        this.checkpointFileParameter = checkpointFileParameter;
    }

    public String getResultsFolderPath() {
        return resultsFolderPath;
    }

    public void setResultsFolder(String resultsFolderPath) {
        this.resultsFolderPath = resultsFolderPath;
    }

    public String getFuzzyInputFilePath() {
        return fuzzyInputFilePath;
    }

    public void setFuzzyInputFilePath(String fuzzyInputFilePath) {
        this.fuzzyInputFilePath = fuzzyInputFilePath;
    }
}
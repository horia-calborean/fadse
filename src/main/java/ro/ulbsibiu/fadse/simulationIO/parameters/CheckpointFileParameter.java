package ro.ulbsibiu.fadse.simulationIO.parameters;

import java.io.Serializable;

public class CheckpointFileParameter implements Serializable {
    protected String name;
    protected String checkpointFilePath;
    protected String additionalDataFilePath;

    public CheckpointFileParameter(String checkpointFilePath, String additionalDataFilePath) {
        name = "checkpointFile";
        this.checkpointFilePath = checkpointFilePath;
        this.additionalDataFilePath = additionalDataFilePath;
    }

    public String getCheckpointFilePath() {
        return checkpointFilePath;
    }

    public String getAdditionalDataFilePath() {
        return additionalDataFilePath;
    }

    public CheckpointFileParameter clone() {
        return new CheckpointFileParameter(checkpointFilePath, additionalDataFilePath);
    }
}
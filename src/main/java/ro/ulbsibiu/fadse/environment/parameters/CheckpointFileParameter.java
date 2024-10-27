package ro.ulbsibiu.fadse.environment.parameters;

import java.io.Serializable;

public class CheckpointFileParameter extends SimulatorParameter<String> implements Serializable {
    protected String checkpointFilePath;
    protected String additionalDataFilePath;

    public CheckpointFileParameter(String name, String checkpointFilePath, String additionalDataFilePath) {
        super(name);
        this.checkpointFilePath = checkpointFilePath;
        this.additionalDataFilePath = additionalDataFilePath;
    }

    public String getCheckpointFilePath() {
        return checkpointFilePath;
    }

    public String getAdditionalDataFilePath() {
        return additionalDataFilePath;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        CheckpointFileParameter clone = new CheckpointFileParameter(name, checkpointFilePath, additionalDataFilePath);

        clone.setDescription(description);

        return clone;
    }
}
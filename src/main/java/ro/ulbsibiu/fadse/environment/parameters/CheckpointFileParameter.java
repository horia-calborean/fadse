/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.environment.parameters;

import java.io.Serializable;

/**
 *
 * @author Radu
 */
public class CheckpointFileParameter implements Serializable {
    String checkpointFile_;
    String secondFile_;

    public CheckpointFileParameter(String checkpointFile, String secondFile){
        checkpointFile_ = checkpointFile;
        secondFile_ = secondFile;
    }

    /**
     * Gets the checkpointFile which contains the population
     * @return path to checkpointFile
     */
    public String GetCheckpointFile(){
        return checkpointFile_;
    }

    /**
     * Gets the file which contains additional data for the current algorithm
     * @return path to additional document
     */
    public String GetSecondFile(){
        return secondFile_;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.extended.qualityIndicator;

import java.io.File;
import java.util.LinkedList;

/**
 *
 * @author Radu
 */
public class HypervolumeHelperResult {
    public int NrObjectives;
    public int NrFolders;
    public int[] PopulationSizeN;
    public double[] MaxObjectives;
    public File MetricsFolder;
    LinkedList<LinkedList> ParsedFilesN;
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators.gap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import ro.ulbsibiu.fadse.environment.Individual;

/**
 *
 * @author jahrralf
 */
public class GapLogger {

    private static File file = new File("infeasible_log_" + System.currentTimeMillis() + ".txt");
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");

    public static synchronized void logInfeasible(Individual ind, String reason) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file, true));

            String line = "";
            line += sdf.format(new Date()) + " ";
            line += "Infeasible: " + ind.toString() + " because ";
            line += reason;

            bw.append(line + "\r\n");

        } catch (IOException ex) {
            Logger.getLogger(GapLogger.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (Exception e) {
                // Nothing to do
            }
        }
    }
}

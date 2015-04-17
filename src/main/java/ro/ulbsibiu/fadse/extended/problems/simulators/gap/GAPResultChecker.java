/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators.gap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.CRC32;

/**
 * This class checks the output chreated by the GAPsimulator and compares it to a reference output.
 * @author Ralf
 */
public class GAPResultChecker {

    /**
     * Checks the results, returns true if the results are correct, false else.
     * @param benchmarkDirectory File with the Benchmark
     * @param fileMap Map with pairs of reference file and calculated file
     * @return
     */
    public static boolean compareResults(HashMap<String, String> fileMap) throws FileNotFoundException, IOException {
        // System.out.println("##################################");
        // System.out.println("now we start comparing.");
        try {
            for (String reference : fileMap.keySet()) {
                String calculated = fileMap.get(reference);

                // System.out.println("REF:  " + reference);
                // System.out.println("CALC: " + calculated);

                File refFile = new File(reference);
                File calcFile = new File(calculated);

                long refCheck = getChecksum(refFile);
                long calcCheck = getChecksum(calcFile);

                System.out.println("Checksum reference:  " + refCheck + " for " + reference);
                System.out.println("checksum calculated: " + calcCheck + " for " + calcFile);

                if (refCheck == calcCheck) {
                    System.out.println("Files " + refFile + " and " + calcFile + " match.");
                } else {
                    System.out.println("Files " + refFile + " and " + calcFile + " are differents.");
                    return false;
                }
            }
        } catch (java.io.FileNotFoundException fnf) {
            System.out.println("FileNotFoundException " + fnf.getMessage());
            throw fnf;
        }
        // System.out.println("##################################");

        return true;
    }

    private static long getChecksum(File f) throws FileNotFoundException, IOException {
        FileInputStream fis = null;
        try {
            CRC32 crc = new CRC32();
            fis = new FileInputStream(f);
            byte[] buffer = new byte[1024];
            while (fis.available() > 0) {
                int read = fis.read(buffer);
                // System.out.println("I read " + read + " byte.");

                for (int i = 0; i < read; i++) {
                    if (buffer[i] != '\r') {
                        crc.update(buffer[i]);
                        // System.out.println("Added " + buffer[i] + ", resulted in " + crc.getValue());
                    } else {
                        // System.out.println("Index " + i + " was \\r!");
                    }
                }
            }
            return crc.getValue();
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }
}

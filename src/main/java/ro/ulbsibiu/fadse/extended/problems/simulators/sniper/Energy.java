/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators.sniper;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Andrei DAIAN
 * @since 22.05.2013
 * @version 1.0
 */
public class Energy {

    public final String CORE_CORE = "core-core";
    public final String CORE_IFETCH = "core-ifetch";
    public final String CORE_INT = "core-int";
    public final String CORE_FP = "core-fp";
    public final String CORE_MEM = "core-mem";
    public final String ICACHE = "icache";
    public final String DCACHE = "dcache";
    public final String L2 = "l2";
    public final String DRAM = "dram";
    public final String OTHER = "other";

    public float computeEnergy(String energyFile, int start, int end, float frequency) {
        ArrayList params = new ArrayList();

        File file = new File(energyFile);

        try {

            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();

                if (line.startsWith(CORE_CORE)) {
                    params.add(line.substring(start, end).trim());
                }

                if (line.startsWith(CORE_IFETCH)) {
                    params.add(line.substring(start, end).trim());
                }

                if (line.startsWith(CORE_INT)) {
                    params.add(line.substring(start, end).trim());
                }

                if (line.startsWith(CORE_FP)) {
                    params.add(line.substring(start, end).trim());
                }

                if (line.startsWith(CORE_MEM)) {
                    params.add(line.substring(start, end).trim());
                }

                if (line.startsWith(ICACHE)) {
                    params.add(line.substring(start, end).trim());
                }

                if (line.startsWith(DCACHE)) {
                    params.add(line.substring(start, end).trim());
                }

                if (line.startsWith(L2)) {
                    params.add(line.substring(start, end).trim());
                }

                if (line.startsWith(DRAM)) {
                    params.add(line.substring(start, end).trim());
                }

                if (line.startsWith(OTHER)) {
                    params.add(line.substring(start, end).trim());
                }

            }
            scanner.close();

        } catch (FileNotFoundException e) {
            System.out.println("File " + energyFile + " not found !");
        }

        System.out.println(params);

        float period = 0; // microseconds
        float totalPower = 0; // Watts

        period = 1 / frequency;

        for (int i = 0; i < params.size(); i++) {
            totalPower += Float.parseFloat((String) params.get(i));
        }

        float totalEnergy = 0;
        totalEnergy = (float) ((totalPower * period) / Math.pow(10, 9));

        return totalEnergy;
    }

    public float readEnergy(String outputFile) {
        File file = new File(outputFile);
        try {
            Scanner scanner = new Scanner(file);

            String previousLine = scanner.nextLine().trim();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if(line.contains("[SNIPER] End") && previousLine.contains("total")){
                    String[] split = previousLine.split("\\s+");
                                                             
                     float totalPower = Float.parseFloat(split[1]);; // Watts            

                    float totalEnergy = 0;
                    totalEnergy = (float) (totalPower  / (2.66 * Math.pow(10, 9))); //frequency

                    return totalEnergy;                                        
                }
                previousLine = line;
            }
            scanner.close();

        } catch (FileNotFoundException e) {
            System.out.println("File " + outputFile + " not found !");
        }
        //value is very small if ok, else send back a very big value
        return 100;
    }
}

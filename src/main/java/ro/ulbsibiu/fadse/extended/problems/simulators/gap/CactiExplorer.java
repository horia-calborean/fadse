/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators.gap;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jahrralf
 */
public class CactiExplorer {

    public static void main(String[] args) {
        StringBuffer sb = new StringBuffer();
        try {
            /*
            <parameter name="c_chunk" description="c_chunk" type="exp2" min="4" max="16"/> <!-- default: 8 (1/32) -->
            <parameter name="c_sets" description="c_sets" type="exp2" min="32" max="8192"/> <!-- default: 128 (32/8192) -->
            <parameter name="c_lines" description="c_lines" type="exp2" min="1" max="128"/> <!-- default 1 (1/128) -->
             */

            int[] c_chunk_s = {4, 8, 16};
            int[] c_sets_s = {32, 64, 128, 256, 512, 1024, 2048, 4096, 8192};
            int[] c_lines_s = {1, 2, 4, 8, 16, 32, 64, 128};

            sb.append("cache_chunk_size" + ";" + "cache_lines_per_set" + ";" + "cache_sets" + ";" + "complexity" + "\r\n");

            for (int cache_chunk_size : c_chunk_s) {
                for (int cache_sets : c_sets_s) {
                    for (int cache_lines_per_set : c_lines_s) {
                        double complexity = GAPOutputParser.getHardwareComplexityCache(cache_chunk_size, cache_lines_per_set, cache_sets);

                        System.out.print("L " + cache_chunk_size + ";" + cache_lines_per_set + ";" + cache_sets + ";" + complexity);
                        sb.append(cache_chunk_size + ";" + cache_lines_per_set + ";" + cache_sets + ";" + complexity + "\r\n");
                    }
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(CactiExplorer.class.getName()).log(Level.SEVERE, null, ex);
        }

        // CactiConnector.writeCache();

        System.out.println("############################");
        System.out.println(sb);
    }

    public static void main_old(String[] args) {
        StringBuffer sb = new StringBuffer();
        try {
            /* double c = GAPOutputParser.getHardwareComplexity(4, 4, 32, 16, 1024, 2);
            System.out.println("c := " + c); */

            int[][] a = {{4, 4}, {8, 8}, {12, 12}, {16, 16}, {24, 24}, {32, 16}, {32, 32}};
            int[] b = {32, 64};
            int[][] c = {{16, 1024, 16}, {8, 128, 1}, {16, 1024, 2}}; // 8kb, 256 kb

            for (int i = 0; i < a.length; i++) {
                /* int cache_chunk_size = (int) (Math.round(Math.random() * 5) + 0);
                int cache_sets = (int) (Math.round(Math.random() * 8) + 5);
                int cache_lines_per_set = (int) (Math.round(Math.random() * 8) + 0);

                cache_chunk_size = (int) Math.pow(2, cache_chunk_size);
                cache_sets = (int) Math.pow(2, cache_sets);
                cache_lines_per_set = (int) Math.pow(2, cache_lines_per_set); */

                int lines = a[i][0];
                int columns = a[i][1];

                for (int j : b) {
                    int layers = j;

                    for (int[] k : c) {
                        /* int cache_chunk_size = k[0];
                        int cache_sets = k[1];
                        int cache_lines_per_set = k[2]; */

                        int cache_chunk_size = k[0];
                        int cache_sets = k[1];
                        int cache_lines_per_set = k[2];

                        try {
                            double complexity = GAPOutputParser.getHardwareComplexity(
                                    lines, columns, layers,
                                    cache_chunk_size, cache_lines_per_set, cache_sets);
                            System.out.print("L "
                                    + lines + ";" + columns + ";" + layers + ";"
                                    + cache_chunk_size + ";" + cache_lines_per_set + ";" + cache_sets + ";");
                            System.out.println(complexity);
                            sb.append(lines + ";" + columns + ";" + layers + ";" + cache_chunk_size + ";" + cache_lines_per_set + ";" + cache_sets + ";" + complexity + "\n");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        System.out.println("");
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(CactiExplorer.class.getName()).log(Level.SEVERE, null, ex);
        }

//        CactiConnector.writeCache();

        System.out.println("############################");
        System.out.println(sb);
    }
}

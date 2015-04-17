/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators.gap;

import java.util.LinkedHashMap;
import java.util.Vector;

/**
 * All strings used for msim should be defined here
 *
 * @version 0.1
 * @since 21.04.2010
 * @author Andrei, Ralf
 */
public class GAPConstants {

    public static final String C_CHUNK = "c_chunk";
    public static final String C_SETS = "c_sets";
    public static final String C_LINES = "c_lines";

    public static final String DO_FUNCTION_INLINING_FIRST = "do_function_inlining_first";

    public static final String DO_FUNCTION_INLINING = "do_function_inlining";
    public static final String FINLINE_KPI_INSNS_PER_CALLER = "finline_kpi_insns_per_caller";
    public static final String FINLINE_LENGTH_OF_FUNCTION = "finline_length_of_function";
    public static final String FINLINE_MAX_CALLER_COUNT = "finline_max_caller_count";
    public static final String FINLINE_WEIGHT_OF_CALLER = "finline_weight_of_caller";

    public static final String DO_LOOP_ACCELERATION = "do_loop_acceleration";
    public static final String DO_BRANCH_PREDICTION = "do_branch_prediction";

    public static final String DO_PEX = "do_pex";
    public static final String PEX_MAX_TOTAL_DYNAMIC_LENGTH = "pex_max_total_dynamic_length";
    public static final String PEX_MAX_TOTAL_DYNAMIC_LENGTH_IN_LOOPS = "pex_max_total_dynamic_length_in_loops";
    // public static final String PEX_MAX_AVG_DYNAMIC_LENGTH = "pex_max_avg_dynamic_length";
    // public static final String PEX_MAX_AVG_DYNAMIC_LENGTH_IN_LOOPS = "pex_max_avg_dynamic_length_in_loops";
    public final static String PEX_DECIDEDNESS_MIN = "pex_decidedness_min";
    public final static String PEX_DECIDEDNESS_MAX = "pex_decidedness_max";
    public final static String PEX_RELEVANCE_MIN = "pex_relevance_min";
    public final static String PEX_RELEVANCE_MAX = "pex_relevance_max";

    public static final String DO_STATIC_SPECULATION = "do_static_speculation";
    public static final String SSPEC_MIN_NODE_WEIGHT = "sspec_min_node_weight";
    public static final String SSPEC_MIN_PROBABILITY_PERCENT = "sspec_min_probability_percent";
    public static final String SSPEC_HOT_FUNCTION_RATIO_PERCENT = "sspec_hot_function_ratio_percent";
    public static final String SSPEC_MAX_NUMBER_OF_INSNS_TO_SHIFT = "sspec_max_number_of_insns_to_shift";
    public static final String SSPEC_MAX_NUMBER_OF_BLOCKS_TO_SHIFT = "sspec_max_number_of_blocks_to_shift";
    public static final String SSPEC_MAX_ALLOWED_ADDITIONAL_HEIGHT = "sspec_max_allowed_additional_height";

    public static final String DO_QDLRU = "do_qdlru";

    /** Memory latency */
    public static final String MEM_LATENCY = "mem_latency";
    /** Number of columns */
    public static final String N_COLUMNS = "n_columns";
    /** Number of layers */
    public static final String N_LAYERS = "n_layers";
    /** Number of lines/rows */
    public static final String N_LINES = "n_lines";
    // OTHER STRINGS
    public static final String P_SCRIPT = "script";
    /** Name of benchmark key in XML file */
    public static final String P_TARGET_DIRECTORY = "benchmark";
    /** This list contains all the parameteres that can or shall be used to generate the command line */
    public static Vector<String> commandLineParameters = new Vector<String>();

    static {
        commandLineParameters.add(N_LINES);
        commandLineParameters.add(N_COLUMNS);
        commandLineParameters.add(N_LAYERS);
        commandLineParameters.add(MEM_LATENCY);
        commandLineParameters.add(C_CHUNK);
        commandLineParameters.add(C_SETS);
        commandLineParameters.add(C_LINES);
        commandLineParameters.add(DO_LOOP_ACCELERATION);
        commandLineParameters.add(DO_BRANCH_PREDICTION);
        commandLineParameters.add(DO_PEX);
        commandLineParameters.add(DO_QDLRU);
    }

    // SIMPLE PARAMETERS
    /* public static LinkedHashMap<String, String> getEmptyParameterMap() {
        LinkedHashMap<String, String> paramList = new LinkedHashMap<String, String>();

        paramList.put(N_LINES, "");
        paramList.put(N_COLUMNS, "");
        paramList.put(N_LAYERS, "");
        paramList.put(MEM_LATENCY, "");
        paramList.put(C_CHUNK, "");
        paramList.put(C_SETS, "");
        paramList.put(C_LINES, "");

        paramList.put(DO_BRANCH_PREDICTION, "1");
        paramList.put(DO_PEX, "0");
        paramList.put(PEX_MAX_LENGTH, "12");
        paramList.put(PEX_MAX_LENGTH_IN_LOOPS, "24");

        paramList.put(DO_LOOP_ACCELERATION, "1");

        paramList.put(DO_FUNCTION_INLINING, "0");
        paramList.put(FINLINE_KPI_INSNS_PER_CALLER, "0");
        paramList.put(FINLINE_LENGTH_OF_FUNCTION, "0");
        paramList.put(FINLINE_MAX_CALLER_COUNT, "0");
        paramList.put(FINLINE_WEIGHT_OF_CALLER, "0");

        paramList.put(DO_STATIC_SPECULATION, "0");
        paramList.put(SSPEC_MIN_NODE_WEIGHT, "0");
        paramList.put(SSPEC_MIN_PROBABILITY_PERCENT, "0");
        paramList.put(SSPEC_HOT_FUNCTION_RATIO_PERCENT, "0");
        paramList.put(SSPEC_MAX_NUMBER_OF_INSNS_TO_SHIFT, "0");
        paramList.put(SSPEC_MAX_NUMBER_OF_BLOCKS_TO_SHIFT, "0");
        paramList.put(SSPEC_MAX_ALLOWED_ADDITIONAL_HEIGHT, "0");

        return paramList;
    } */

    // PARAMETER DEFAULTS
    public static LinkedHashMap<String, String> getDefaultParameterMap() {
        LinkedHashMap<String, String> paramList = new LinkedHashMap<String, String>();

        paramList.put(N_LINES, "12");
        paramList.put(N_COLUMNS, "12");
        paramList.put(N_LAYERS, "32");
        paramList.put(MEM_LATENCY, "24");
        paramList.put(C_CHUNK, "8");
        paramList.put(C_SETS, "128");
        paramList.put(C_LINES, "1");

        paramList.put(DO_BRANCH_PREDICTION, "1");
        paramList.put(DO_PEX, "0");

        paramList.put(DO_LOOP_ACCELERATION, "1");

        paramList.put(DO_FUNCTION_INLINING_FIRST, "1");

        paramList.put(DO_FUNCTION_INLINING, "0");
        paramList.put(FINLINE_KPI_INSNS_PER_CALLER, "10");
        paramList.put(FINLINE_LENGTH_OF_FUNCTION, "128");
        paramList.put(FINLINE_MAX_CALLER_COUNT, "5");
        paramList.put(FINLINE_WEIGHT_OF_CALLER, "10");

        paramList.put(DO_STATIC_SPECULATION, "0");
        paramList.put(SSPEC_MIN_NODE_WEIGHT, "0");
        paramList.put(SSPEC_MIN_PROBABILITY_PERCENT, "0");
        paramList.put(SSPEC_HOT_FUNCTION_RATIO_PERCENT, "0");
        paramList.put(SSPEC_MAX_NUMBER_OF_INSNS_TO_SHIFT, "0");
        paramList.put(SSPEC_MAX_NUMBER_OF_BLOCKS_TO_SHIFT, "0");
        paramList.put(SSPEC_MAX_ALLOWED_ADDITIONAL_HEIGHT, "0");

        paramList.put(DO_QDLRU, "0");

        return paramList;
    }
}

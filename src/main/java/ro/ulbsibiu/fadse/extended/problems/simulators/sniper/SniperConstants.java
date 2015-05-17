/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators.sniper;

import java.util.HashMap;

/**
 *
 * @author Andrei DAIAN
 * @since 03.05.2013
 * @version 1.0
 */
public class SniperConstants {

    public static HashMap<String, String> getSimpleParameters() {
    	HashMap<String, String> paramList = new HashMap<String, String>();
        paramList.put("num_cores", "-n ");
        paramList.put("logical_cpus", "-g --perf_model/core/logical_cpus=");
        paramList.put("num_cache_levels", "-g --perf_model/cache/levels=");
        paramList.put("rob_size", "-g --perf_model/core/interval_timer/window_size=");
        paramList.put("controllers_interleaving", "-g --perf_model/dram/controllers_interleaving=");
        
        paramList.put("scheduler_type","-g --scheduler/type=");

        /*
         * L1 Cache Parameters
         * L1 ICache
         */
        paramList.put("l1_icache_associativity", "-g --perf_model/l1_icache/associativity=");
        paramList.put("l1_icache_cache_block_size", "-g --perf_model/l1_icache/cache_block_size=");
        paramList.put("l1_icache_cache_size", "-g --perf_model/l1_icache/cache_size=");
        paramList.put("l1_icache_shared_cores", "-g --perf_model/l1_icache/shared_cores=");

        /*
         * L1 DCache
         */
        paramList.put("l1_dcache_associativity", "-g --perf_model/l1_dcache/associativity=");
        paramList.put("l1_dcache_cache_block_size", "-g --perf_model/l1_dcache/cache_block_size=");
        paramList.put("l1_dcache_cache_size", "-g --perf_model/l1_dcache/cache_size=");
        paramList.put("l1_dcache_shared_cores", "-g --perf_model/l1_dcache/shared_cores=");

        /*
         * -------------------------------------------------------------------------------------------
         */

        /*
         * L2 Cache Parameters
         */
        paramList.put("l2_cache_associativity", "-g --perf_model/l2_cache/associativity=");
        paramList.put("l2_cache_cache_block_size", "-g --perf_model/l2_cache/cache_block_size=");
        paramList.put("l2_cache_cache_size", "-g --perf_model/l2_cache/cache_size=");
        paramList.put("l2_cache_shared_cores", "-g --perf_model/l2_cache/shared_cores=");

        /*
         * -------------------------------------------------------------------------------------------
         */

        /*
         * L3 Cache Parameters
         */
        paramList.put("l3_cache_associativity", "-g --perf_model/l3_cache/associativity=");
        paramList.put("l3_cache_cache_size", "-g --perf_model/l3_cache/cache_size=");
        paramList.put("l3_cache_shared_cores", "-g --perf_model/l3_cache/shared_cores=");

        /*
         * -------------------------------------------------------------------------------------------
         */

        /*
         * Branch Predictor Parameters
         */
        paramList.put("branch_predictor", "-g --perf_model/branch_predictor/type=");

        /*
         * -------------------------------------------------------------------------------------------
         */                  

        return paramList;
    }
;
}
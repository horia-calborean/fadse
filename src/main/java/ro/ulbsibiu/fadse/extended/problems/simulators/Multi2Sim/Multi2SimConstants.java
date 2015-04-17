/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.extended.problems.simulators.Multi2Sim;
import java.util.LinkedHashMap;
import java.util.LinkedList;
/**
 * All strings used for multi2sim should be defined here
 *
 * @version 0.1
 * @since 21.04.2010
 * @author Andrei
 */
public class Multi2SimConstants {

    // OTHER STRINGS
    public static final String OF_PIPELINE     = "m2s_p.txt";
    public static final String OF_CACHE        = "m2s_c.txt";
    public static final String REG_DELIMITER   = "#";


    // SIMPLE PARAMETERS
    public static LinkedHashMap<String, String> getSimpleParameters() {
        LinkedHashMap<String, String> paramList = new LinkedHashMap<String, String>();

        paramList.put( "report:pipeline", "" );
        paramList.put( "report:cache", "" );
 
        paramList.put( "max_cycles", "" );
        paramList.put( "max_inst", "" );
        paramList.put( "max_time", "" );
        paramList.put( "fastfwd", "" );
        paramList.put( "cores", "" );
        paramList.put( "threads", "" );
        paramList.put( "stage_time_stats", "" );
        paramList.put( "recover_kind", "" );
        paramList.put( "recover_penalty", "" );
        paramList.put( "quantum", "" );
        paramList.put( "switch_penalty", "" );
        paramList.put( "fetch_kind", "" );
        paramList.put( "decode_width", "" );
        paramList.put( "dispatch_kind", "" );
        paramList.put( "dispatch_width", "" );
        paramList.put( "issue_kind", "" );
        paramList.put( "issue_width", "" );
        paramList.put( "commit_kind", "" );
        paramList.put( "commit_width", "" );
        paramList.put( "bpred", "" );
        paramList.put( "bpred:btb", "" );
        paramList.put( "bpred:ras", "" );
        paramList.put( "bpred:bimod", "" );
        paramList.put( "bpred:twolevel", "" );
        paramList.put( "bpred:choice", "" );
        paramList.put( "tcache", "" );
        paramList.put( "tcache:topo", "" );
        paramList.put( "tcache:trace_size", "" );
        paramList.put( "tcache:branch_max", "" );
        paramList.put( "tcache:queue_size", "" );
        paramList.put( "fetchq_size", "" );
        paramList.put( "uopq_size", "" );
        paramList.put( "rob_kind", "" );
        paramList.put( "rob_size", "" );
        paramList.put( "rf_kind", "" );
        paramList.put( "rf_size", "" );
        paramList.put( "iq_kind", "" );
        paramList.put( "iq_size", "" );
        paramList.put( "lsq_kind", "" );
        paramList.put( "lsq_size", "" );
        paramList.put( "cacheconfig", "" );
        paramList.put( "iperfect", "" );
        paramList.put( "dperfect", "" );
        paramList.put( "page_size", "" );
        
        return paramList;
    };

}

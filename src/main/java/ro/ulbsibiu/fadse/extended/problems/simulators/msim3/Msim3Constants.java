/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.extended.problems.simulators.msim3;
import java.util.LinkedHashMap;

import ro.ulbsibiu.fadse.extended.problems.simulators.CompositeParameter;
/**
 * All strings used for msim should be defined here
 *
 * @version 0.1
 * @since 21.04.2010
 * @author Andrei
 */
public class Msim3Constants {

    // OBJECTIVES AND OUTPUTS
    public static final String O_POWERCONSUMPTION    = "powerconsumption";
    public static final String O_THROUGHPUT_IPC      = "THROUGHPUT IPC";
    public static final String O_IPC                 = "ipc";

    // PARAMETER AND PARAMETER TEMPLATES
    public static final String P_NUM_CORES           = "numcores";
    public static final String P_CORE_X_TOTAL_POWER  = "Core_[X]_total_power";
    public static final String P_BENCHMARK           = "benchmark";

    // OTHER STRINGS
    public static final String NUMER_REPLACE    = "[X]";
    public static final String SMT_STATS        = "******* SMT STATS *******";
    public static final String CMP_STATS        = "******* CMP STATS *******";

    // SIMPLE PARAMETERS
    public static LinkedHashMap<String, String> getSimpleParameters() {
        LinkedHashMap<String, String> paramList = new LinkedHashMap<String, String>();
        paramList.put( "config",                "-config" );
        paramList.put( "dumpconfig",            "-dumpconfig" );
        paramList.put( "h",                     "-h" );
        paramList.put( "v",                     "-v" );
        paramList.put( "d",                     "-d" );
        paramList.put( "seed",                  "-seed" );
        paramList.put( "q",                     "-q" );
        // paramList.put( "redir_sim",             "-redir:sim" );
        // paramList.put( "redir_prog",            "-redir:prog" );
        // paramList.put( "redir_err",             "-redir:err" );
        paramList.put( "nice",                  "-nice" );
        paramList.put( "max_inst",              "-max:inst" );
        paramList.put( "max_cycles",            "-max:cycles" );
        paramList.put( "fastfwd",               "-fastfwd" );
        paramList.put( "ptrace",                "-ptrace" );
        paramList.put( "pcstat",                "-pcstat" );
        paramList.put( "power_print_stats",     "-power:print_stats" );
        paramList.put( "num_cores",             "-num_cores" );
        paramList.put( "max_contexts_per_core", "-max_contexts_per_core" );
        paramList.put( "fetch_speed",           "-fetch:speed" );
        paramList.put( "decode_width",          "-decode:width" );
        paramList.put( "issue_width",           "-issue:width" );
        paramList.put( "issue_inorder",         "-issue:inorder" );
        paramList.put( "issue_wrongpath",       "-issue:wrongpath" );
        paramList.put( "commit_width",          "-commit:width" );
        paramList.put( "iq_issue_exec_delay",   "-iq:issue_exec_delay" );
        paramList.put( "fetch_rename_delay",    "-fetch_rename_delay" );
        paramList.put( "rename_dispatch_delay", "-rename_dispatch_delay" );
        paramList.put( "lsq_size",              "-lsq:size" );
        paramList.put( "rob_size",              "-rob:size" );
        paramList.put( "fetch_policy",          "-fetch:policy" );
        paramList.put( "recovery_model",        "-recovery:model" );
        paramList.put( "iq_size",               "-iq:size" );
        paramList.put( "rf_size",               "-rf:size" );
        paramList.put( "res_ialu",              "-res:ialu" );
        paramList.put( "res_imult",             "-res:imult" );
        paramList.put( "res_memport",           "-res:memport" );
        paramList.put( "res_fpalu",             "-res:fpalu" );
        paramList.put( "res_fpmult",            "-res:fpmult" );
        paramList.put( "write_buf_size",        "-write_buf:size" );
        paramList.put( "cache_dl1lat",          "-cache:dl1lat" );
        paramList.put( "cache_dl2lat",          "-cache:dl2lat" );
        paramList.put( "cache_il1lat",          "-cache:il1lat" );
        paramList.put( "cache_il2lat",          "-cache:il2lat" );
        paramList.put( "tlb_lat",               "-tlb:lat" );
        paramList.put( "bpred",                 "-bpred" );
        paramList.put( "bpred_ras",             "-bpred:ras" );
        paramList.put( "bpred_spec_update",     "-bpred:spec_update" );
        paramList.put( "bpred_bimod",           "-bpred:bimod" );
        paramList.put( "bpred_comb",            "-bpred:comb" );
        paramList.put( "cpred",                 "-cpred" );
        paramList.put( "cpred_ras",             "-cpred:ras" );
        paramList.put( "cpred_bimod",           "-cpred:bimod" );
        paramList.put( "cpred_comb",            "-cpred:comb" );
        paramList.put( "cache_dl3lat",          "-cache:dl3lat" );
        paramList.put( "cache_il3lat",          "-cache:il3lat" );
        
        return paramList;
    };

    public static CompositeParameter getCacheDL1() {
    	return new CompositeParameter(
    			"-cache:dl1", "dl1", ":",
    			new String[] {"type", "nsets", "bsize", "assoc", "repl"}, 
    			new String[] {"dl1", "256", "32", "4", "l"}
    	);
    }
    
    public static CompositeParameter getCacheDL2() {
    	return new CompositeParameter(
    			"-cache:dl2", "dl2", ":",
    			new String[] {"type", "nsets", "bsize", "assoc", "repl"}, 
    			new String[] {"ul2", "512", "128", "8", "l"}
    	);
    }
    
    public static CompositeParameter getCacheIL1() {
    	return new CompositeParameter(
    			"-cache:il1", "il1", ":",
    			new String[] {"type", "nsets", "bsize", "assoc", "repl"}, 
    			new String[] {"il1", "512", "32", "2", "l"}
    	);
    }
    
    public static CompositeParameter getCacheIL2() {
    	return new CompositeParameter(
    			"-cache:il2", "il2", ":",
    			new String[] {"type", "nsets", "bsize", "assoc", "repl"}, 
    			new String[] {"ul2", "512", "128", "8", "l"}
    	);
    }
    
    public static CompositeParameter getCacheDL3() {
    	return new CompositeParameter(
    			"-cache:dl3", "dl3", ":",
    			new String[] {"type", "nsets", "bsize", "assoc", "repl"},
    			null
    			);
    }
    
    public static CompositeParameter getCacheIL3() {
    	return new CompositeParameter(
    			"-cache:il3", "il3", ":",
    			new String[] {"type", "nsets", "bsize", "assoc", "repl"},
    			null
    			);
    }
    
    public static CompositeParameter getITLB() {
    	return new CompositeParameter(
    			"-tlb:itlb", "itlb", ":",
    			new String[] {"type", "nsets", "bsize", "assoc", "repl"}, 
    			new String[] {"itlb", "16", "4096", "4", "l"}
    	);
    }
    
    public static CompositeParameter getDTLB() {
    	return new CompositeParameter(
    			"-tlb:dtlb", "dtlb", ":",
    			new String[] {"type", "nsets", "bsize", "assoc", "repl"}, 
    			new String[] {"dtlb", "32", "4096", "4", "l"}
    	);
    }
    
    public static CompositeParameter getBPred_2lev() {
    	CompositeParameter C = new CompositeParameter(
    			"-bpred:2lev", "bpred2lev", " ",
    			new String[] {"l1size", "l2size", "histsize", "xor"}, 
    			new String[] {"1", "1024", "8", "0"}
    	);
    	//C.setValueAffixes("[ ", " ]");
    	return C;
    }
    
    public static CompositeParameter getBPred_BTB() {
    	CompositeParameter C = new CompositeParameter(
    			"-bpred:btb", "bpredbtb", " ",
    			new String[] {"numsets", "assoc"}, 
    			new String[] {"512", "4"}
    	);
    	//C.setValueAffixes("[ ", " ]");
    	return C;
    }
    
    public static CompositeParameter getCPred_2lev() {
    	CompositeParameter C = new CompositeParameter(
    			"-cpred:2lev", "cpred2lev", " ",
    			new String[] {"l1size", "l2size", "histsize", "xor"}, 
    			new String[] {"1", "1024", "8", "0"}
    	);
    	//C.setValueAffixes("[ ", " ]");
    	return C;
    }
    
    public static CompositeParameter getCPred_BTB() {
    	CompositeParameter C = new CompositeParameter(
    			"-cpred:btb", "cpredbtb", " ",
    			new String[] {"numsets", "assoc"}, 
    			new String[] {"512", "4"}
    	);
    	//C.setValueAffixes("[ ", " ]");
    	return C;
    }
    
    public static CompositeParameter getMemConfig_Chunk() {
    	return new CompositeParameter(
    			"-mem:config", "memconfig", ":",
    			new String[] {"type", "buswidth", "firstchunklatency", "nextchunklatency"},
    			new String[] {"chunk", "4", "300", "2"}
    			);
    }
    
    public static CompositeParameter getMemConfig_Basic() {
    	return new CompositeParameter(
    			"-mem:config", "memconfig", ":",
    			new String[] {"type", "to_dram_time", "from_dram_time", "row_hit_time", "row_closed_time", "row_conflict_time", "number_of_banks", "row_buffer_size"},
    			new String[] {"basic", "4", "6", "12", "90", "90", "90", "8", "2048"}
    			);
    }
    
    // CUSTOM PARAMETERS
    public static final LinkedHashMap<String, String> getCustomParameters() {
        LinkedHashMap<String, String> paramList = new LinkedHashMap<String, String>();

        /*
         * CACHE CONFIGURATION
         *
            <name>:<nsets>:<bsize>:<assoc>:<repl>

            <name>   - name of the cache being defined
            <nsets>  - number of sets in the cache
            <bsize>  - block size of the cache
            <assoc>  - associativity of the cache
            <repl>   - block replacement strategy, 'l'-LRU, 'f'-FIFO, 'r'-random

            Examples:   -cache:dl1 dl1:4096:32:1:l
                        -dtlb dtlb:128:4096:32:r
         */
        // dl1:256:32:4:l
        paramList.put( "dl1_type", "dl1");
        paramList.put( "dl1_nsets", "256");
        paramList.put( "dl1_bsize", "32");
        paramList.put( "dl1_assoc", "4");
        paramList.put( "dl1_repl", "l");

        // dl2:512:128:8:l
        paramList.put( "dl2_type", "ul2");
        paramList.put( "dl2_nsets", "512");
        paramList.put( "dl2_bsize", "128");
        paramList.put( "dl2_assoc", "8");
        paramList.put( "dl2_repl", "l");

        // il1:512:32:2:l
        paramList.put( "il1_type", "il1");
        paramList.put( "il1_nsets", "512");
        paramList.put( "il1_bsize", "32");
        paramList.put( "il1_assoc", "2");
        paramList.put( "il1_repl", "l");

        // il2: dl2
        // @TODO: set defaults for these:
        paramList.put( "il2_type", "dl2");
        paramList.put( "il2_nsets", "");
        paramList.put( "il2_bsize", "");
        paramList.put( "il2_assoc", "");
        paramList.put( "il2_repl", "");



        /*
         *   The main memory configuration parameter has the following format:
         *
            <type>:<width>:<config>

            <type>   - Type of main memory modeling - see dram.h
            <width>  - Width of the memory bus in bytes
            <config> - Configuration string - see dram.h

            Examples:   -mem:config chunk:4:300:2
                        -mem:config basic:4:6:12:90:90:90:8:2048
         */
        // chunk:4:300:2
         paramList.put( "mem_type", "chunk");
         paramList.put( "mem_width", "4");
         paramList.put( "mem_config", "300:2");
        return paramList;

    };
}

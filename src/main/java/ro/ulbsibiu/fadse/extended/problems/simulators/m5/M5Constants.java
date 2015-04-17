/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.extended.problems.simulators.m5;
import java.util.LinkedHashMap;
import java.util.LinkedList;
/**
 * All strings used for msim should be defined here
 *
 * @version 0.1
 * @since 21.04.2010
 * @author Andrei
 */
public class M5Constants {

    // OTHER STRINGS
    public static final String P_SCRIPT    = "script";


    // SIMPLE PARAMETERS
    public static LinkedHashMap<String, String> getSimpleParameters() {
        LinkedHashMap<String, String> paramList = new LinkedHashMap<String, String>();
        paramList.put( "script",                 "" );
        paramList.put( "d",                 "-d" );
        paramList.put( "t",                 "-t" );
        paramList.put( "n",                 "-n" );
        paramList.put( "caches",            "--caches" );
        paramList.put( "l2cache",           "--l2cache" );
        paramList.put( "cachehierachy",     "--cachehierachy" );
        paramList.put( "l1data_cache_size", "--l1data_cache_size" );
        paramList.put( "l1d_assoc",         "--l1d_assoc" );
        paramList.put( "l1d_mshrs",         "--l1d_mshrs" );
        paramList.put( "l1d_tgts_per_mshr", "--l1d_tgts_per_mshr" );
        paramList.put( "l1instr_cache_size","--l1instr_cache_size" );
        paramList.put( "l1i_assoc",         "--l1i_assoc" );
        paramList.put( "l1i_mshrs",         "--l1i_tgts_per_mshr" );
        paramList.put( "l2cache_size",      "--l2cache_size" );
        paramList.put( "l2_assoc",          "--l2_assoc" );
        paramList.put( "l2_mshrs",          "--l2_mshrs" );
        paramList.put( "l2_tgts_per_mshr",  "--l2_tgts_per_mshr" );
        paramList.put( "options",           "--options" );
      
        
        return paramList;
    };

    // PARAMETER DEFAULTS
    public static final LinkedHashMap<String, String> getSimpleParametersDefaults() {
        LinkedHashMap<String, String> paramList = new LinkedHashMap<String, String>();

        paramList.put( "d",                 "" );
        paramList.put( "t",                 "" );
        paramList.put( "n",                 "1" );
        paramList.put( "l2cache",           "" );
        paramList.put( "cachehierachy",     "sc" );
        paramList.put( "l1data_cache_size", "32" );
        paramList.put( "l1d_assoc",         "1" );
        paramList.put( "l1d_mshrs",         "1" );
        paramList.put( "l1d_tgts_per_mshr", "1" );
        paramList.put( "l1instr_cache_size","32" );
        paramList.put( "l1i_assoc",         "1" );
        paramList.put( "l1i_mshrs",         "1" );
        paramList.put( "l2cache_size",      "1024" );
        paramList.put( "l2_assoc",          "1" );
        paramList.put( "l2_mshrs",          "1" );
        paramList.put( "l2_tgts_per_mshr",  "1" );
        paramList.put( "options",           "\"p1 -m18\"" );

        return paramList;

    };

    // KB
    public static final LinkedList<String> getCustomParameters(){
        LinkedList<String> result = new LinkedList<String>();

        result.add("l1data_cache_size");
        result.add("l1instr_cache_size");
        result.add("l2cache_size");

        return result;
    }


}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.extended.problems.simulators.m5;
import java.util.LinkedList;
import java.util.Map;

import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorBase;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorRunner;
import ro.ulbsibiu.fadse.extended.problems.simulators.msim3.Msim3Constants;
/**
 *
 * @author Andrei
 */
public class M5Runner extends SimulatorRunner {
    public M5Runner(SimulatorBase simulator){
        super(simulator);
    }

    /**
     * creates the simpleParameter list and adds the fixed msim parameters
     * needed for proper execution
     *
     * ./build/ALPHA_SE/m5.opt configs/example/se3.py -d -t -n$Sn --caches --l2cache --cachehierachy=$Sch --l1data_cache_size=$Sd1cs --l1d_assoc=$Sd1a --l1d_mshrs=$Sd1m --l1d_tgts_per_mshr=$Sd1tm --l1instr_cache_size=$Si1cs --l1i_assoc=$Si1a --l1i_mshrs=$Si1m --l1i_tgts_per_mshr=$Si1tm --l2cache_size=$Sl2s --l2_assoc=$Sl2a --l2_mshrs=$Sl2m --l2_tgts_per_mshr=$Sl2tm --cmd=/dist/splash2/codes/kernels/fft/FFT --options="-p$Sn -m18"
     *
     */
    @Override
    public void prepareParameters(){
        super.prepareParameters();

        this.addSimpleParameter("d", "");
        this.addSimpleParameter("t", "");
        this.addSimpleParameter("caches", "");
        this.addSimpleParameter("l2cache", "");
        this.addSimpleParameter("options", "-p1 -m18");



    }

    @Override
    protected String[] getCommandLine(){
        LinkedList<String> sbParamList = new LinkedList<String>();
        //sbParamList.add();
        sbParamList.add(this.simulator.getInputDocument().getSimulatorParameter("simulator_executable"));
        
      

        // Search the basic params and add the existing ones
        Map<String, String> basicParams = M5Constants.getSimpleParameters();
        LinkedList<String> customParameters = M5Constants.getCustomParameters();
        for (Map.Entry<String, String> param:  basicParams.entrySet()){
            if (this.simpleParameters.containsKey(param.getKey())){

                String p = param.getValue();

                if (param.getKey().equals(M5Constants.P_SCRIPT))
                    p = this.simpleParameters.get(param.getKey());
                else
                if (!this.simpleParameters.get(param.getKey()).isEmpty()){
                    p += ("=" + this.simpleParameters.get(param.getKey()));
                    if (customParameters.contains(param.getKey()))
                        p += ("kB");
                }
                sbParamList.add(p);
            }
        }

        // search for benchmark
        if (this.simpleParameters.containsKey(Msim3Constants.P_BENCHMARK)){
            sbParamList.add("--cmd=" + this.simpleParameters.get(Msim3Constants.P_BENCHMARK));
        }



        String[] result = new String[sbParamList.size()];
        sbParamList.toArray(result);

        return result;
    }


}

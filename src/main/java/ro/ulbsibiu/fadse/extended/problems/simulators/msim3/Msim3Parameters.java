package ro.ulbsibiu.fadse.extended.problems.simulators.msim3;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import ro.ulbsibiu.fadse.extended.problems.simulators.msim3.parametermodifiers.CLIOptionCoreIndexSuffix;
import ro.ulbsibiu.fadse.extended.problems.simulators.msim3.parametermodifiers.CacheModifier;
import ro.ulbsibiu.fadse.extended.problems.simulators.msim3.parametermodifiers.IDCoreIndexPrefix;
import ro.ulbsibiu.fadse.extended.problems.simulators.msimcommon.CompositeParameter;
import ro.ulbsibiu.fadse.extended.problems.simulators.msimcommon.ParameterGroup;
import ro.ulbsibiu.fadse.extended.problems.simulators.msimcommon.SimpleParameter;

public class Msim3Parameters {
	public SimpleParameter SimulatorExecutable;
	public SimpleParameter SimulatorOutputFile;
	public SimpleParameter PrintPowerStats;
	public SimpleParameter MaxInstructions;
	public SimpleParameter FastFwdInstructions;
	public SimpleParameter HomogeneousCores;
	
	public ParameterGroup SimulatorParameters;
	
	// TODO
	public CompositeParameter MainMemory;
	public SimpleParameter NumCores;
	public SimpleParameter MaxContextsPerCore;
	
	public ParameterGroup StructuralParameters;
	
	public ParameterGroup MultiCoreParameters;
	
	public Msim3Parameters() {
	}
	
	public void initSimulatorParameters() {
		SimulatorExecutable = new SimpleParameter("simulator_executable", "");
		SimulatorOutputFile = new SimpleParameter("simulator_output_file", "-redir:sim");
		PrintPowerStats = new SimpleParameter("print_power_stats", "-power:print_stats");
		MaxInstructions = new SimpleParameter("max_inst", "-max:inst");
		FastFwdInstructions = new SimpleParameter("fastfwd", "-fastfwd");
		HomogeneousCores = new SimpleParameter("homogeneous_cores", "");
		HomogeneousCores.setNeverRender(true);
		
		SimulatorParameters = new ParameterGroup("simulator_parameters", "");
		SimulatorParameters.addSubParameter(SimulatorExecutable);
		SimulatorParameters.addSubParameter(SimulatorOutputFile);
		SimulatorParameters.addSubParameter(PrintPowerStats);
		SimulatorParameters.addSubParameter(FastFwdInstructions);
		SimulatorParameters.addSubParameter(MaxInstructions);
		SimulatorParameters.addSubParameter(HomogeneousCores);
	}
	
	public void retrieveSimulatorParameters(LinkedHashMap<String, String> SimulatorValues) {
		SimulatorParameters.retrieve(SimulatorValues);
	}
	
	
	public void initStructuralParameters() {
		NumCores = new SimpleParameter("num_cores", "-num_cores");
		MaxContextsPerCore = new SimpleParameter("max_contexts_per_core", "-max_contexts_per_core");
		
		StructuralParameters = new ParameterGroup("structural_parameters", "");
		StructuralParameters.addSubParameter(NumCores);
		StructuralParameters.addSubParameter(MaxContextsPerCore);
	}
	
	public void retrieveStructuralParameters(LinkedHashMap<String, String> StructuralValues) {
		StructuralParameters.retrieve(StructuralValues);
	}
	
	public void initCoreParameters() {
		MultiCoreParameters = new ParameterGroup("multicore_parameters", "");
		int nCores = Integer.parseInt(NumCores.getValue());
		
		for (int i = 0; i < nCores; i++) {
			ParameterGroup Core = createCoreParameterGroup();
			Core.setID("Core_" + Integer.toString(i));
			if (HomogeneousCores.getValue().equals("false")) {
				Core.modify(new IDCoreIndexPrefix(i));
			}
			if (nCores > 1) {
				Core.modify(new CLIOptionCoreIndexSuffix(i));
			}
			MultiCoreParameters.addSubParameter(Core);
		}
	}
	
	public void retrieveCoreParameters(LinkedHashMap<String, String> CoreValues) {
		MultiCoreParameters.retrieve(CoreValues);
	}
	
	private ParameterGroup createCoreParameterGroup() {
		CompositeParameter CacheDL1 = createCacheCompositeParameter();
		CacheDL1.setID("cache_dl1");
		CacheDL1.setCliOption("-cache:dl1");
		CacheDL1.setSeparator(":");
		CacheDL1.setSelfModifiable(false);
		CacheDL1.modify(new CacheModifier("dl1"));
		CacheDL1.setSelfModifiable(true);
		
		CompositeParameter CacheIL1 = createCacheCompositeParameter();
		CacheIL1.setID("cache_il1");
		CacheIL1.setCliOption("-cache:il1");
		CacheIL1.setSeparator(":");
		CacheIL1.setSelfModifiable(false);
		CacheIL1.modify(new CacheModifier("il1"));
		CacheIL1.setSelfModifiable(true);
		
		CompositeParameter CacheDL2 = createCacheCompositeParameter();
		CacheDL2.setID("cache_dl2");
		CacheDL2.setCliOption("-cache:dl2");
		CacheDL2.setSeparator(":");
		CacheDL2.setSelfModifiable(false);
		CacheDL2.modify(new CacheModifier("dl2"));
		CacheDL2.setSelfModifiable(true);
		
		SimpleParameter IssueWidth = new SimpleParameter("issue_width", "-issue:width");
		SimpleParameter CommitWidth = new SimpleParameter("commit_width", "-commit:width");
		SimpleParameter DecodeWidth = new SimpleParameter("decode_width", "-decode:width");
		SimpleParameter LsqSize = new SimpleParameter("lsq_size", "-lsq:size");
		SimpleParameter RobSize = new SimpleParameter("rob_size", "-rob:size");
		SimpleParameter IqSize = new SimpleParameter("iq_size", "-iq:size");
		SimpleParameter RfSize = new SimpleParameter("rf_size", "-rf:size");
		SimpleParameter ResIntAlu = new SimpleParameter("res_ialu", "-res:ialu");
		SimpleParameter ResIntMult = new SimpleParameter("res_imult", "-res:imult");
		SimpleParameter ResFPAlu = new SimpleParameter("res_fpalu", "-res:fpalu");
		SimpleParameter ResFPMult = new SimpleParameter("res_fpmult", "-res:fpmult");
		
		ParameterGroup CoreParameters = new ParameterGroup("", "");
		CoreParameters.addSubParameter(CacheDL1);
		CoreParameters.addSubParameter(CacheIL1);
		CoreParameters.addSubParameter(CacheDL2);
		CoreParameters.addSubParameter(IssueWidth);
		CoreParameters.addSubParameter(CommitWidth);
		CoreParameters.addSubParameter(DecodeWidth);
		CoreParameters.addSubParameter(LsqSize);
		CoreParameters.addSubParameter(RobSize);
		CoreParameters.addSubParameter(IqSize);
		CoreParameters.addSubParameter(RfSize);
		CoreParameters.addSubParameter(ResIntAlu);
		CoreParameters.addSubParameter(ResIntMult);
		CoreParameters.addSubParameter(ResFPAlu);
		CoreParameters.addSubParameter(ResFPMult);
		
		return CoreParameters;
	}
	
	private CompositeParameter createCacheCompositeParameter() {
		SimpleParameter Type = new SimpleParameter("type", "");
		SimpleParameter NSets = new SimpleParameter("nsets", "");
		SimpleParameter BlockSize = new SimpleParameter("bsize", "");
		SimpleParameter Associativity = new SimpleParameter("assoc", "");
		SimpleParameter ReplacementPolicy = new SimpleParameter("repl", "");
		
		CompositeParameter CacheParameters = new CompositeParameter("", "");
		CacheParameters.addSubParameter(Type);
		CacheParameters.addSubParameter(NSets);
		CacheParameters.addSubParameter(BlockSize);
		CacheParameters.addSubParameter(Associativity);
		CacheParameters.addSubParameter(ReplacementPolicy);
		
		return CacheParameters;
	}
	
	public ParameterGroup getSimulatorParameters() {
		return SimulatorParameters;
	}
	
	public ParameterGroup getStructuralParameters() {
		return StructuralParameters;
	}
	
	public ParameterGroup getMulticoreParameters() {
		return MultiCoreParameters;
	}
}

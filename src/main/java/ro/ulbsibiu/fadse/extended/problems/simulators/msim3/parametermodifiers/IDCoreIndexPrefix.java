package ro.ulbsibiu.fadse.extended.problems.simulators.msim3.parametermodifiers;

import ro.ulbsibiu.fadse.extended.problems.simulators.msimcommon.Parameter;
import ro.ulbsibiu.fadse.extended.problems.simulators.msimcommon.ParameterModifier;

public class IDCoreIndexPrefix implements ParameterModifier {
	private int CoreIndex;
	
	public IDCoreIndexPrefix(int coreindex) {
		CoreIndex = coreindex;
	}
	
	public void modify(Parameter p) {
		String id = p.getID();
		p.setID("Core_" + Integer.toString(CoreIndex) + "_" + id);
	}

}

package ro.ulbsibiu.fadse.extended.problems.simulators.msim3.parametermodifiers;

import ro.ulbsibiu.fadse.extended.problems.simulators.msimcommon.Parameter;
import ro.ulbsibiu.fadse.extended.problems.simulators.msimcommon.ParameterModifier;

public class CacheModifier implements ParameterModifier {
	private String Type;
	public CacheModifier(String type) {
		Type = type;
	}
	public void modify(Parameter p) {
		String id = p.getID();
		p.setID(Type + "_" + id);
	}

}

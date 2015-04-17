package ro.ulbsibiu.fadse.extended.problems.simulators.msimcommon;

import java.util.Iterator;

public class CompositeParameter extends ParameterGroup {

	private String Separator;
	private boolean hasValue;
	
	public CompositeParameter(String id, String clioption) {
		super(id, clioption);
		Separator = "";
		hasValue = false;
	}
	
	public void setSeparator(String separator) {
		Separator = separator;
	}
	public String getSeparator() {
		return Separator;
	}
	
	private String assembleValue() {
		Parameter P;
		StringBuilder ValueAssembler = new StringBuilder();
		
		Iterator<Parameter> I = SubParameters.iterator();
		while (I.hasNext()) {
			P = I.next();
			if (P.hasValue() && P.getNeverRender() == false) {
				ValueAssembler.append(P.getValue());
				if (I.hasNext()) {
					ValueAssembler.append(Separator);
				}
			}
		}
		hasValue = true;
		return ValueAssembler.toString().trim();
	}
	
	public String getValue() {
		if (!hasValue) {
			Value = assembleValue();
		}
		return Value;
	}
	
	public String toString() {
		StringBuilder CliAssembler = new StringBuilder();
		CliAssembler.append(CliOption);
		CliAssembler.append(" ");
		CliAssembler.append(getValue());
		return CliAssembler.toString();
	}


}

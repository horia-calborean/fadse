package ro.ulbsibiu.fadse.extended.problems.simulators;

import java.util.LinkedHashMap;

public class CompositeParameter {
	// the exact string used by this parameter in CLI 
	// (e.g. "-cache:il2")
	private String CliName; 
	
	// when searching the received parameters, this string is prefixed to the subparameter's name 
	// (e.g. searching for "dl1_nsets", where "dl1" is the ParameterPrefix, and "nsets" is a subparameter name)
	private String ParameterPrefix;  
	
	// a list with the names of the subparameters 
	// (e.g. new String[] {"type", "nsets", "bsize", "assoc", "repl"} - these names will be prefixed with 
	// ParameterPrefix before searching for values) 
	private String[] Subparameters;
	
	// the values of the subparameters - if null, this parameter will be unset
	private String[] Values;
	
	// the string placed between the values of the subparameters, when they are assembled into a string 
	private String Subseparator;
	
	// if false, the value of this parameter is "none"
	// if true, the value of this parameter is a concatenation of the values of the subparameters
	private boolean isSet;
	
	// optional affixes to the string resulted from the concatenation of the values of the subparameters
	private String ValuePrefix;
	private String ValueSuffix;
	
	public CompositeParameter(String cliname, String parameterprefix, String subseparator, String[] subparams, String[] defaultvalues) {
		if (defaultvalues != null) {
			isSet = true;
		}
		CliName = cliname; 
		ParameterPrefix = parameterprefix;
		Values = defaultvalues;
		Subseparator = subseparator;
		ValuePrefix = "";
		ValueSuffix = "";
		
		// The Subparameters array must contain all the values in subparams, prepended with the ID and an underscore
		Subparameters = subparams;
		for (int i = 0; i < Subparameters.length; i++) {
			Subparameters[i] = ParameterPrefix + '_' + Subparameters[i];
		}
	}
	
	public void setValueAffixes(String prefix, String suffix) {
		ValuePrefix = prefix;
		ValueSuffix = suffix;
	}
	
	public void assembleParameterValues(LinkedHashMap<String, String> receivedParameters, StringBuilder Output) {
		String pkey, pval;
		// See whether receivedParameters contains values for the subparameters; if so, overwrite them in the Values array
		isSet = false;
		for (int i = 0; i < Subparameters.length; i++) {
			pkey = Subparameters[i];
			if (receivedParameters.containsKey(pkey)) {
				isSet = true;
				Values[i] = receivedParameters.get(pkey);				
			}
		}

		if (!isSet) {
			return;
		}
		
		// Serialize everything
		Output.append(" ");
		Output.append(CliName);
		Output.append(" ");
		
		Output.append(ValuePrefix);
		
		for (int i = 0; i < Values.length; i++) {
			pval = Values[i];
			Output.append(pval);
			Output.append(Subseparator);
		}
		Output.deleteCharAt(Output.length() - 1);
		
		Output.append(ValueSuffix);
	}
	
	
}

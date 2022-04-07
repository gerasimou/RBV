package approach.estimators;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.util.Precision;

public abstract class Estimator {

	/** Holds the parameters of this estimator in <key,value> form */
	protected Map<String, Number> parametersMap; 
	
	protected String name;
	
	protected Number[] estimatedResults;
	
	private final int PRECISION = 6; 

	
	public Estimator (String name) {
		parametersMap 		= new HashMap<>();
		estimatedResults	= new Number[] {0, 1};
		this.name			= name;
	}
	
	
	/**
	 * Execute this estimator and return its values in the form of a number array
	 * To be implemented by any subclass
	 * 
	 */
	public abstract Number[] execute();
	
	
	public void putParameter (String key, Number value) {
		parametersMap.put(key, value);
	}
	
	public Number getParameter (String key) {
		return parametersMap.get(key);
	}
	
	public String getRangeCommand() {
		if (estimatedResults.length > 1)
			return name +"="+ Precision.round(estimatedResults[0].floatValue(),PRECISION) +":"+ Precision.round(estimatedResults[1].floatValue(),PRECISION);
		else
			return name +"="+ Precision.round(estimatedResults[0].floatValue(),PRECISION);
	}
	
	public String getConcreteCommand (boolean lowerBound) {
		Number v = (lowerBound) ? Precision.round(estimatedResults[0].floatValue(),PRECISION) : Precision.round(estimatedResults[1].floatValue(),PRECISION);  
		return "\n const double " + name +"="+ v +";\n" ; 
	}

	public String getParametricCommand () {
		return "const double " + name +";\n"; 
	}

	
	public String getName() {
		return name;
	}
}

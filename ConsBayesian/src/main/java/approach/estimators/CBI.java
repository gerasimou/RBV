package approach.estimators;

public class CBI extends Estimator {
		
	public double epsilon;
	public double theta;
	public int t;

	public CBI (String name) {
		super(name);
		estimatedResults=new Number[] {0};
	}

	@Override
	public Number[] execute() {
		
		// get the prior knowledge which is in the form of a confidence bound:
		// Pr(x<epsilon)=theta
		epsilon=parametersMap.get("epsilon").doubleValue();
		theta=parametersMap.get("theta").doubleValue();
		//get the data t which is the holding time in the state.
		t=parametersMap.get("t").intValue();
		
		if(t<=1/epsilon) {
			estimatedResults[0]=epsilon+Math.exp(-1)*(1-theta)/(t*Math.exp(-epsilon*t)*theta);
		}
		else {
			estimatedResults[0]=epsilon/theta;
		}
	
		return estimatedResults;
	}

}

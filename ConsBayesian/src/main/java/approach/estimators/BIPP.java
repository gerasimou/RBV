package approach.estimators;

public class BIPP extends Estimator {
		
	public double epsilon1;
	public double theta1;
	public double epsilon2;
	public double theta2;
	public double t;

	public BIPP (String name) {
		super (name);
	}
	
	private double likelihood_poisson(double x) {
		
		return Math.exp(-x*t);
	}

	@Override
	public Number[] execute() {
				
		// get the prior knowledge which is in the form of two confidence bounds:
		// Pr(x<epsilon1)=theta1
		epsilon1=parametersMap.get("epsilon1").doubleValue();
		theta1=parametersMap.get("theta1").doubleValue();
		// Pr(epsilon1<x<epsilon2)=theta2
		epsilon2=parametersMap.get("epsilon2").doubleValue();
		theta2=parametersMap.get("theta2").doubleValue();
		
		
		//check constraints: epsilon1<epsilon2, and theta1+theta2<1
		if ( (epsilon1 >= epsilon2) || (theta1+theta2>1) )
			throw new NumberFormatException("Exception in BIPP " + this.name + " - check its hyperparameters [" + 
							epsilon1 +","+ epsilon2 +","+ theta1 +","+ theta2);

		
		// t is the data, i.e. holding time in the state
		try {
			t=parametersMap.get("t").doubleValue();
		}catch (NullPointerException e) {
			t  = 0;
		}

		
		//now calculate the BBIPP-lower and store the result at estimated_results[0]
		if(theta2*(epsilon1-epsilon2)*likelihood_poisson(epsilon1)*likelihood_poisson(epsilon2)+theta1*epsilon1*likelihood_poisson(epsilon1)-theta1*epsilon2*likelihood_poisson(epsilon2)>=0) {
			estimatedResults[0]=(epsilon1*likelihood_poisson(epsilon1)*theta2)/(theta1+likelihood_poisson(epsilon1)*theta2);
		}
		else {
			estimatedResults[0]=(epsilon2*likelihood_poisson(epsilon2)*theta2)/(theta1+likelihood_poisson(epsilon2)*theta2);
		}
		
		//now calculate the BBIPP-upper (i.e. the CBI) and store the result at estimated_results[1]
		if(t<1/epsilon2) {
			estimatedResults[1]= (epsilon1*likelihood_poisson(epsilon1)*theta1 + epsilon2*likelihood_poisson(epsilon2)*theta2 + (1/t)*likelihood_poisson(1/t)*(1-theta2-theta1))
					/(likelihood_poisson(epsilon1)*theta1);
		}
		else if ((1/epsilon2<=t) && (t<=1/epsilon1)) {
			estimatedResults[1]=(epsilon1*likelihood_poisson(epsilon1)*theta1 + (1/t)*likelihood_poisson(1/t)*theta2 + epsilon2*likelihood_poisson(epsilon2)*(1-theta2-theta1))
					/(likelihood_poisson(epsilon1)*theta1);
		}
		else {//   (t>1/epsilon1) {
			estimatedResults[1]=(epsilon1*likelihood_poisson(epsilon1)*(theta1+theta2) + (epsilon2)*likelihood_poisson(epsilon2)*(1-theta2-theta1))
					/(likelihood_poisson(epsilon1)*theta1) ;
		}
	
		return estimatedResults;
	}

	public String getCommand(boolean lower) {
		Number v = (lower) ? estimatedResults[0] : estimatedResults[0] ;  
		return "\n const double " + name +"="+ v +";\n" ; 
	}
}
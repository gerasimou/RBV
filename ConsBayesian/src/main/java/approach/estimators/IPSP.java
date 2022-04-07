package approach.estimators;

public class IPSP extends Estimator {
	
	
	
	public double t_i_prior_lower; //pseudo count for prior confidence, e.g., 100
	public double t_i_prior_upper; //pseudo count for prior confidence, e.g., 200
	public double r_ij_prior_lower;//prior estimates on rates, e.g., 4
	public double r_ij_prior_upper;//prior estimates on rates, e.g., 6
	public double t_i;
	public int n_ij;

	public IPSP (String name) {
		super(name);
	}

	@Override
	public Number[] execute() {
		// TODO Auto-generated method stub
		
		t_i_prior_lower=parametersMap.get("t_i_prior_lower").intValue();
		t_i_prior_upper=parametersMap.get("t_i_prior_upper").intValue();
		r_ij_prior_lower=parametersMap.get("r_ij_prior_lower").doubleValue();
		r_ij_prior_upper=parametersMap.get("r_ij_prior_upper").doubleValue();
		try {
			t_i=parametersMap.get("t_i").doubleValue();
			n_ij=parametersMap.get("n_ij").intValue();
		}catch (NullPointerException e) {
			t_i  = 0;
			n_ij = 0;
		}
		try {
			//estimated_results[0] represents the estimated lower bound;
			if (n_ij/t_i >=r_ij_prior_lower) {
				estimatedResults[0]=(t_i_prior_upper*r_ij_prior_lower+n_ij)/(t_i_prior_upper+t_i);
			}
			else {
				estimatedResults[0]=(t_i_prior_lower*r_ij_prior_lower+n_ij)/(t_i_prior_lower+t_i);
			}
			//estimated_results[1] represents the estimated upper bound;		
			if (n_ij/t_i <=r_ij_prior_upper) {
				estimatedResults[1]=(t_i_prior_upper*r_ij_prior_upper+n_ij)/(t_i_prior_upper+t_i);
			}
			else {
				estimatedResults[1]=(t_i_prior_lower*r_ij_prior_upper+n_ij)/(t_i_prior_lower+t_i);
			}
		}	
		catch (ArithmeticException e) {
			e.printStackTrace();
		}
			
		return estimatedResults;
	}

}

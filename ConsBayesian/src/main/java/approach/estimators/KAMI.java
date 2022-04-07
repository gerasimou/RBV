package approach.estimators;

public class KAMI extends Estimator {
		
	// A list of the known parameters used by eq (8) in the paper
	public int t_i_prior;
	public double r_ij_prior;
	public int t_i;
	public int n_ij;

	public KAMI (String name) {
		super(name);
		estimatedResults=new Number[] {0};
	}

	@Override
	public Number[] execute() {
		// TODO Auto-generated method stub
		t_i_prior=parametersMap.get("t_i_prior").intValue();
		r_ij_prior=parametersMap.get("r_ij_prior").doubleValue();
		t_i=parametersMap.get("t_i").intValue();
		n_ij=parametersMap.get("n_ij").intValue();
		
		
		estimatedResults[0]= t_i_prior*r_ij_prior/(t_i_prior+t_i)+n_ij/(t_i_prior+t_i);
		
		return estimatedResults;
	}

}

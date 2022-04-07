package caseStudy.chainInspection;

import org.apache.commons.math3.random.RandomGenerator;

import approach.estimators.BIPP;
import approach.estimators.Estimator;
import approach.estimators.IPSP;
import utilities.Utility;

public class WindTurbineChain {
	
	Estimator failCleanIPSP;
	Estimator damageBIPP; 		
	Estimator cleanBIPP;
	int X;
	final int chainID;
	String    chainModelTemplate;

	
	double eInspect;
	double eTravel;
	double eClean;
	double ePrepare;

	
	
	public WindTurbineChain(int ID, RandomGenerator rng) {
		chainID				= ID;
		
		failCleanIPSP		= setupFailCleanIPSP("r_fail_clean"+ID, rng);
		damageBIPP 			= setupDamageBIPP ("r_damage"+ID, rng); 		
		cleanBIPP 			= setupCleanBIPP ("r_clean"+ID, rng);
		chainModelTemplate	= "models/auv/chainTemplate.sm";
		X					= 0;
		
		
		try {
			eInspect		= Double.parseDouble(Utility.getValue("E_INSPECT", "0.030")) + rng.nextDouble();
			eTravel			= Double.parseDouble(Utility.getValue("E_TRAVEL",  "0.045")) + rng.nextDouble();
			eClean			= Double.parseDouble(Utility.getValue("E_CLEAN",   "0.200"));// + rng.nextDouble();
			ePrepare		= Double.parseDouble(Utility.getValue("E_PREPARE", "0.011")) + rng.nextDouble();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public int getID() {
		return chainID;
	}
	
	
	public String getChainCommands() {
		String commands = "\n//Chain " + chainID + " commands\n";
		commands += failCleanIPSP.getParametricCommand();
		commands += damageBIPP.getParametricCommand();
		commands += cleanBIPP.getParametricCommand();
		commands += "const double e"+ chainID +"inspect = " + eInspect +";\n";  
		commands += "const double e"+ chainID +"travel  = " + eTravel  +";\n";  
		commands += "const double e"+ chainID +"clean   = " + eClean  +";\n";  
		commands += "const double e"+ chainID +"prepare = " + ePrepare +";\n";  
		return commands;
	}
	
	public String getChainRangeCommands() {		
		String paramsWithRanges	= failCleanIPSP.getRangeCommand()  +",";
		paramsWithRanges 		+= damageBIPP.getRangeCommand()	+",";	
		paramsWithRanges 		+= cleanBIPP.getRangeCommand();

		return paramsWithRanges;
	}
	
	
	private Estimator setupFailCleanIPSP (String name, RandomGenerator rng) {
		Estimator ipspFailClean = new IPSP (name);
		//add the parameters representing prior knowledge which are given by experts,
		//``based on 10-20 previous experiments, the rate of r_fail_clean is .., ..''
		
		int t_i_prior_lower = (int) (10 + Math.floor(10 * rng.nextDouble()));
		int t_i_prior_upper = (int) (20 + Math.floor(20 * rng.nextDouble()));
		double r_ij_prior_lower = 0.8/60.0  + 0.8/60.0  * rng.nextDouble()/10;
		double r_ij_prior_upper = 0.98/60.0 + 0.98/60.0 * rng.nextDouble()/10;
		ipspFailClean.putParameter("t_i_prior_lower",  t_i_prior_lower);
		ipspFailClean.putParameter("t_i_prior_upper",  t_i_prior_upper);
		ipspFailClean.putParameter("r_ij_prior_lower", r_ij_prior_lower);
		ipspFailClean.putParameter("r_ij_prior_upper", r_ij_prior_upper);
		ipspFailClean.execute();
		
		return ipspFailClean;
	}
	

	private Estimator setupDamageBIPP (String name, RandomGenerator rng) {
		Estimator bbippDamage = new BIPP (name); 		
		
		double epsilon1 = 0.00000001 + 0.00000001 * rng.nextDouble();
		double theta1   = 0.88       + 0.88       * rng.nextDouble()/100;
		double epsilon2 = 0.0000001  + 0.0000001  * rng.nextDouble();
		double theta2   = 0.10       + 0.10       * rng.nextDouble()/100;
		
		//add the parameters representing prior knowledge which are given by experts,
		//the expert says ``80% confident that the data failure rate is lower than 10^-7''
		bbippDamage.putParameter("epsilon1", epsilon1);
		bbippDamage.putParameter("theta1", theta1);
		//the expert also says ``90% confident that the data failure rate is better than 10^-5''
		//that means: ``10% confident that the data failure rate is between 10^-7 to 10^-6''
		bbippDamage.putParameter("epsilon2", epsilon2);
		bbippDamage.putParameter("theta2", theta2);
		
		bbippDamage.putParameter("t", 1);	        				
		bbippDamage.execute();
		
		return bbippDamage;
	}	
	

	private Estimator setupCleanBIPP (String name, RandomGenerator rng) {
		Estimator bbippClean = new BIPP (name); // another bipp estimator for r_clean
		
		double epsilon1 = 0.12 + 0.12 * rng.nextDouble();
		double theta1   = 0.10 + 0.10 * rng.nextDouble()/100;
		double epsilon2 = 0.90 + 0.90  * rng.nextDouble();
		double theta2   = 0.85 + 0.85  * rng.nextDouble()/100;

		
		//add the parameters representing prior knowledge which are given by experts,
		//the expert says ``99% confident that the r_clean rate is bigger than 0.05''
		bbippClean.putParameter("epsilon1", epsilon1);
		bbippClean.putParameter("theta1", theta1);
		//add the parameters representing prior knowledge which are given by experts,
		//that means: ``95% confident that the r_clean rate is between 0.1 and 0.05''
		bbippClean.putParameter("epsilon2", epsilon2);
		bbippClean.putParameter("theta2", theta2);

		bbippClean.putParameter("t", 0.01);	        				
		bbippClean.execute();
		
		return bbippClean;
	}

}

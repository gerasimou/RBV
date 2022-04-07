package caseStudy.chainInspection.other;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import _main.PRISMPSY_API_Runner;
import _main.Property;
import approach.estimators.BIPP;
import approach.estimators.Estimator;
import approach.estimators.IPSP;
import caseStudy.chainInspection.WindTurbineChain;


public class AUVAnalyser {
	Estimator ipspFailClean;
	Estimator bbippDamage; 		
	Estimator bbippClean; 		
	PRISMPSY_API_Runner runner;

	String modelFile;
	String propertiesFile; 
	
	List<WindTurbineChain> chainsList;
	
	public AUVAnalyser (String modelFile, String propertiesFile) {
		this.modelFile		= modelFile;
		this.propertiesFile	= propertiesFile;
		ipspFailClean		= setupIPSP   ("r_fail_clean");
		bbippDamage 		= setupBBIPP1 ("r_damage"); 		
		bbippClean 			= setupBBIPP2 ("r_clean"); 		
		runner 				= new PRISMPSY_API_Runner();		
	}

	
	public Property[] execute (double dirtyHoldingTSum, int dirtyHoldingTCount) {
        //IPSP estimator for the r_fail_clean
		ipspFailClean.putParameter("t_i",  dirtyHoldingTSum);
		ipspFailClean.putParameter("n_ij", dirtyHoldingTCount);
		Number[] ipspResults = ipspFailClean.execute();	        		
//		System.out.printf(" *  r_fail_clean:IPSP=[%.8f, %.8f];", ipspResults[0], ipspResults[1]);
		System.out.printf(" *  r_fail_clean=%.8f:%.8f, ", ipspResults[0], ipspResults[1]);

//		//BIPP estimator
		bbippDamage.putParameter("t", dirtyHoldingTSum);	        				
		Number[] bbippDamResults = bbippDamage.execute();
//		System.out.printf(" r_damage: BBIPP=[%2.8e, %2.8e];", bbippDamResults[0], bbippDamResults[1]);
		System.out.printf("r_damage=%.8e:%.8e, ", bbippDamResults[0], bbippDamResults[1]);
		
		//BIPP estimator
		bbippClean.putParameter("t", dirtyHoldingTSum);	        				
		Number[] bbippCleanResults = bbippClean.execute();
//		System.out.printf(" r_clean: BBIPP=[%2.8e, %2.8e]\n", bbippCleanResults[0], bbippCleanResults[1]);
		System.out.printf("r_clean=%2.8e:%2.8e\t", bbippCleanResults[0], bbippCleanResults[1]);
		
		//prepare param with ranges
		String paramsWithRanges	= ipspFailClean.getRangeCommand() +",";
		paramsWithRanges += bbippClean.getRangeCommand()		  +",";	
		paramsWithRanges += bbippDamage.getRangeCommand();

		//for each property, this function results its minimum and maximum values
		Property[] properties = runner.calculatePropertiesBounds(modelFile, propertiesFile, paramsWithRanges);
		for (Property p : properties) {
			System.out.print(Arrays.toString(p.getMinMax()) +"\t");
		}
		System.out.println();
		
		return properties;
	}
	
	
	private static Estimator setupIPSP (String name) {
		Estimator ipspFailClean = new IPSP (name);
		//add the parameters representing prior knowledge which are given by experts,
		//Simos: I am using the ones listed in Table 1 of the paper.
		//XZ: Ok, now I change it to ``based on 10-20 previous experiments, the rate of r_fail_clean is .., ..''
		ipspFailClean.putParameter("t_i_prior_lower",  10); //100
		ipspFailClean.putParameter("t_i_prior_upper",  20); //200
		ipspFailClean.putParameter("r_ij_prior_lower", 0.9/60.0);
		ipspFailClean.putParameter("r_ij_prior_upper", 0.98/60.0);
		
		return ipspFailClean;
	}
	

	private static Estimator setupBBIPP1 (String name) {
		Estimator bbippDamage = new BIPP (name); 		
		//add the parameters representing prior knowledge which are given by experts,
		//XZ: the expert says ``80% confident that the cata failure rate is better than 10^-6''
		bbippDamage.putParameter("epsilon1", 0.00000001);
		bbippDamage.putParameter("theta1", 0.8);
		//add the parameters representing prior knowledge which are given by experts,
		//XZ: the expert also says ``90% confident that the cata failure rate is better than 10^-5''
		//that means: ``10% confident that the cata failure rate is between 10^-6 to 10^-5''
		bbippDamage.putParameter("epsilon2", 0.0000001);
		bbippDamage.putParameter("theta2", 0.19);
		
		return bbippDamage;
	}	
	

	private static Estimator setupBBIPP2 (String name) {
		Estimator bbippClean = new BIPP (name); // another bipp estimator for r_clean
		//add the parameters representing prior knowledge which are given by experts,
		//XZ: the expert says ``99% confident that the r_clean rate is bigger than 0.05''
		bbippClean.putParameter("epsilon1", 0.05/20);
		bbippClean.putParameter("theta1", 0.03);
		//add the parameters representing prior knowledge which are given by experts,
		//that means: ``95% confident that the r_clean rate is between 0.1 and 0.05''
		bbippClean.putParameter("epsilon2", 0.5);
		bbippClean.putParameter("theta2", 0.97);
		
		return bbippClean;
	}
	

	public static void main(String[] args) {
		AUVAnalyser auv = new AUVAnalyser("models/auv/auv.sm", "models/auv/auv.csl");	

		// oK, let us say average cleaning time is 60 seconds,  
		// after the cleaning, the 3 outcomes with associated prob: fail (0.95), succ(1-0.95-10^{-5}), damage (0.00001)
		// in this scenario, we have the rates as follows:
		double r_fail_clean 		= 0.6;///90.0;
		double r_damage     		= 0.0000001;///90.0;
		double r_clean      		= 1-r_fail_clean-r_damage;//(1-0.5-0.0000001)/90.0;
		double averageHoldingTime	= 120;
		ExponentialDistribution dirtyExponential = new ExponentialDistribution(1/((r_damage + r_clean + r_fail_clean)/averageHoldingTime));

		double dirtyHoldingTSum = 0;
		int dirtyHoldingTCount 	= 0;
		
		for (int i=0; i<10; i++) {			
            double holdingT		= dirtyExponential.sample();
//            double holdingT		= 100;
            dirtyHoldingTSum   += holdingT;
            dirtyHoldingTCount++;
            System.out.print("Parameters:" + dirtyHoldingTCount +"\t"+ dirtyHoldingTSum +"\t");
        	auv.execute(dirtyHoldingTSum, dirtyHoldingTCount);
		}
		
	}
}

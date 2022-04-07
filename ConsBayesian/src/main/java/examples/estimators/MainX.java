package examples.estimators;

import java.util.Arrays;

import _main.PRISMPSY_API_Runner;
import _main.Property;
import approach.estimators.BIPP;
import approach.estimators.CBI;
import approach.estimators.Estimator;
import approach.estimators.IPSP;
import approach.estimators.KAMI;

public class MainX {


	public static void main(String[] args) {
		testKAMI();
		testCBI();
		testBBIPP();
		testIPSP();
	}
	
	
	
	public static void testCBI() {
		Estimator cbi = new CBI("testCBI");
		
		//add the parameters representing prior knowledge which are given by experts,
		cbi.putParameter("epsilon", 0.000001);
		cbi.putParameter("theta", 0.8);
		
		//add the parameters representing the data which is, in this case, the accumulated holding time in the safe state which may cause catastrophic failures.
		cbi.putParameter("t", 5000);
		
		//run the estimation and KAMI returns a point estimation, although it is stored by a Number array
		Number[] theCBIResult = cbi.execute();
		System.out.println("CBI:"+theCBIResult[0]);
	}
	
	
	public static void testKAMI() {
		// create the instance of a KAMI_CTMC estimator for the unknown transtion rate r_ij.
		Estimator kami = new KAMI("testKAMI"); 
		
		//add the parameters representing prior knowledge which are given by experts,
		kami.putParameter("t_i_prior", 1000);
		kami.putParameter("r_ij_prior", 0.01);
		
		//add the parameters representing the data (by parsing a csv file)
		//for now, I made up some numbers...
		kami.putParameter("t_i", 2000);
		kami.putParameter("n_ij", 10);
		
		//run the estimation and KAMI returns a point estimation, although it is stored by a Number array
		Number[] theKAMIResult = kami.execute();
		System.out.println("KAMI:"+theKAMIResult[0]);
	
	}
	
	
	public static void testBBIPP() {
		// create the instance of a KAMI_CTMC estimator for the unknown transtion rate r_ij.
		Estimator bbipp = new BIPP("testBBIPP"); 
		
		//add the parameters representing prior knowledge which are given by experts,
		bbipp.putParameter("epsilon1", 0.000001);
		bbipp.putParameter("theta1", 0.8);
		//add the parameters representing prior knowledge which are given by experts,
		bbipp.putParameter("epsilon2", 0.00001);
		bbipp.putParameter("theta2", 0.1);
		
		//add the parameters representing the data which is, in this case, the accumulated holding time in the safe state which may cause catastrophic failures.
		bbipp.putParameter("t", 5000);
				
		//run the estimation and KAMI returns a point estimation, although it is stored by a Number array
		Number[] theBBIPPResult = bbipp.execute();
		System.out.println("BBIPP lower:"+theBBIPPResult[0]);
		System.out.println("BBIPP upper:"+theBBIPPResult[1]);
	
	}
	
	
	public static void testIPSP() {
		//1) create instance
		Estimator ipsp = new IPSP("testIPSP");
		
		//add the parameters representing prior knowledge which are given by experts,
		//I am using the ones listed in Table 1 of the paper.
		ipsp.putParameter("t_i_prior_lower", 40000);
		ipsp.putParameter("t_i_prior_upper", 50000);
		ipsp.putParameter("r_ij_prior_lower", 0.009);
		ipsp.putParameter("r_ij_prior_upper", 0.011);
		
		//add the parameters representing the data (by parsing a csv file)
		//for now, I made up some numbers...
		ipsp.putParameter("t_i", 2000);
		ipsp.putParameter("n_ij", 10);
		
		//3) execute and get results which are bounded estimates
		Number[] estimatorResults = ipsp.execute();
		
		System.out.println("IPSP_lower:"+estimatorResults[0]);
		System.out.println("IPSP_upper:"+estimatorResults[1]);
		
		
		//4) get bounds for properties
		PRISMPSY_API_Runner runner = new PRISMPSY_API_Runner();
		
		String modelFile			= "models/Cluster/cluster2.sm";
		String propertiesFile		= "models/Cluster/cluster.csl";
		String paramsWithRanges	= "ws_fail=0.09081:0.0918,ws_check=1.81105:1.86105,ws_repair=0.86676:0.87666";

		//for each property, this function results its minimum and maximum values
		Property[] properties = runner.calculatePropertiesBounds(modelFile, propertiesFile, paramsWithRanges);
		for (Property p : properties) {
			System.out.println(Arrays.toString(p.getMinMax()));
		}

//		Object[] results = runner.run(modelFile, propertiesFile, paramsWithRanges);
//		System.out.println(Arrays.toString(results));		
	}

}

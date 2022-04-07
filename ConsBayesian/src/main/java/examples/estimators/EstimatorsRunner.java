package examples.estimators;

import java.util.Arrays;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import approach.estimators.BIPP;
import approach.estimators.CBI;
import approach.estimators.Estimator;
import approach.estimators.IPSP;
import approach.estimators.KAMI;

public class EstimatorsRunner {


	public static void main(String[] args) {
//		testIPSP();
		testBIPP();
	}
	
	
	public static void testBIPP() {
		// create the instance of a KAMI_CTMC estimator for the unknown transtion rate r_ij.
		Estimator bbipp = new BIPP("testBBIPP"); 
//		epsilon2=0.5, t=94.90442502664612, theta1=0.1, epsilon1=0.1, theta2=0.9
		//add the parameters representing prior knowledge which are given by experts,
		bbipp.putParameter("epsilon1", 0.1);
		bbipp.putParameter("theta1", 0.1);
		//add the parameters representing prior knowledge which are given by experts,
		bbipp.putParameter("epsilon2", 0.5);
		bbipp.putParameter("theta2", 0.9);
		
		bbipp.putParameter("t", 94.90442502664612);
		Number[] bIPPResult = bbipp.execute();		
		System.out.printf("%d\t[BBIPP_L, BBIPP_U]=[%.6f,%.6f]\n", 1, bIPPResult[0], bIPPResult[1]);

		
		for (int i=1; i<5; i++) {
			//add the parameters representing the data which is, in this case, the accumulated holding time in the safe state which may cause catastrophic failures.
			bbipp.putParameter("t", i);
					
			//run the estimation and KAMI returns a point estimation, although it is stored by a Number array
			Number[] theBBIPPResult = bbipp.execute();
		
			System.out.printf("%d\t[BBIPP_L, BBIPP_U]=[%.6f,%.6f]\n", i, theBBIPPResult[0], theBBIPPResult[1]);

		}
//		System.out.println("BBIPP lower:"+theBBIPPResult[0]);
//		System.out.println("BBIPP upper:"+theBBIPPResult[1]);
	
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
	}
	
	
	
	
	public static void testIPSPContinuous() {
		double r = 0.4;
		ExponentialDistribution dExponential = new ExponentialDistribution(1/r);
		double[] xValues = dExponential.sample(1000);
		
//		System.out.println(Arrays.toString(xValues));
		
		//1) create instance
		Estimator ipsp = new IPSP("continuousIPSP");
				
		//add the parameters representing prior knowledge which are given by experts,
		//I am using the ones listed in Table 1 of the paper.
		ipsp.putParameter("t_i_prior_lower", 40);
		ipsp.putParameter("t_i_prior_upper", 50);
		ipsp.putParameter("r_ij_prior_lower", 0.4);
		ipsp.putParameter("r_ij_prior_upper", 0.6);
		
		double xSum =0;
		for (int i=0; i<xValues.length; i++) {
			xSum += xValues[i];
			ipsp.putParameter("t_i", xSum);
			ipsp.putParameter("n_ij", i+1);
						
			Number[] estimatorResults = ipsp.execute();
			
			System.out.printf("%.3f\t[IPSP_L, IPSP_U]=[%.3f,%.3f]\n", xValues[i], estimatorResults[0], estimatorResults[1]);
//			System.out.println("IPSP_upper:"+estimatorResults[1]);
		}
		System.out.println(xValues.length/xSum);
	}
	
	
	public static void testIPSPDiscrete() {
		double x_OK = 0.3;
		BinomialDistribution xBinomial = new BinomialDistribution(1, x_OK);
		int[] xValues = xBinomial.sample(50000);
		
		System.out.println(Arrays.toString(xValues));
		
		//1) create instance
		Estimator ipsp = new IPSP("discreteIPSP");
				
		//add the parameters representing prior knowledge which are given by experts,
		//I am using the ones listed in Table 1 of the paper.
		ipsp.putParameter("t_i_prior_lower", 10);
		ipsp.putParameter("t_i_prior_upper", 300);
		ipsp.putParameter("r_ij_prior_lower", 0.2);
		ipsp.putParameter("r_ij_prior_upper", 0.4);
		
		int xSum =0;
		for (int i=0; i<xValues.length; i++) {
			xSum += xValues[i];
			ipsp.putParameter("t_i", i+1);
			ipsp.putParameter("n_ij", xSum);
						
			Number[] estimatorResults = ipsp.execute();
			
			System.out.printf("[IPSP_L, IPSP_U]=[%.3f,%.3f]\n", estimatorResults[0], estimatorResults[1]);
//			System.out.println("IPSP_upper:"+estimatorResults[1]);
		}
		System.out.println(xSum/(xValues.length+0.0));
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

}

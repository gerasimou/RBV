package examples.distributions;

import java.util.Arrays;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.AbstractWell;
import org.apache.commons.math3.random.Well19937c;


public class ExponentialExample {

	public static void main(String[] args) {
//		int sum = Arrays.stream(new int []{1,2,3,4}, 0, 2).sum(); //prints 3
//		double avg = Arrays.stream(new int []{1,2,3,4}, 0, 2).sum(); //prints 3
//		System.out.println(sum +"\t"+ avg);
		runExponential(0.3, 1/30.0);
	}

	
	private static void runExponential(double pOK, double rTravel) {
		int seed		 = 25000;
		AbstractWell rng  = new Well19937c(seed);
//		Random rand;

		
		BinomialDistribution pBinomial = new BinomialDistribution(rng, 1, pOK);
		//Exponential distribution for r_travel
		ExponentialDistribution travelExponential = new ExponentialDistribution(rng, rTravel);


		int size=100;
		double[] travel = travelExponential. sample(size);
		System.out.println(travelExponential.getNumericalMean());
		System.out.println(travelExponential.getMean()); 
		System.out.println(Arrays.stream(travel).average().getAsDouble());
		System.out.println(Arrays.toString(travel));
		System.out.println(travelExponential.probability(rTravel)); 
		System.out.println(travelExponential.cumulativeProbability(rTravel));
		
	}
	
}

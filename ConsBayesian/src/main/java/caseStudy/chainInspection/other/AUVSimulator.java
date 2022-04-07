package caseStudy.chainInspection.other;

import java.util.Random;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import approach.estimators.BIPP;
import approach.estimators.Estimator;
import approach.estimators.IPSP;

public class AUVSimulator {

	public static void main(String[] args) {
		
		
		
		Estimator ipspFailClean = new IPSP("r_fail_clean");
		//add the parameters representing prior knowledge which are given by experts,
		//Simos: I am using the ones listed in Table 1 of the paper.
		//XZ: Ok, now I change it to ``based on 10-20 previous experiments, the rate of r_fail_clean is .., ..''
		ipspFailClean.putParameter("t_i_prior_lower",  10);
		ipspFailClean.putParameter("t_i_prior_upper",  20);
		ipspFailClean.putParameter("r_ij_prior_lower", 0.9/60.0);
		ipspFailClean.putParameter("r_ij_prior_upper", 0.98/60.0);
	
		
		Estimator bbippDamage = new BIPP("r_damage"); 		
		//add the parameters representing prior knowledge which are given by experts,
		//XZ: the expert says ``80% confident that the cata failure rate is better than 10^-6''
		bbippDamage.putParameter("epsilon1", 0.000001);
		bbippDamage.putParameter("theta1", 0.8);
		//add the parameters representing prior knowledge which are given by experts,
		//XZ: the expert also says ``90% confident that the cata failure rate is better than 10^-5''
		//that means: ``10% confident that the cata failure rate is between 10^-6 to 10^-5''
		bbippDamage.putParameter("epsilon2", 0.00001);
		bbippDamage.putParameter("theta2", 0.1);
		
		
		Estimator bbippClean = new BIPP("r_clean"); // another bipp estimator for r_clean
		//add the parameters representing prior knowledge which are given by experts,
		//XZ: the expert says ``99% confident that the r_clean rate is bigger than 0.05''
		bbippClean.putParameter("epsilon1", 0.05);
		bbippClean.putParameter("theta1", 0.01);
		//add the parameters representing prior knowledge which are given by experts,
		//that means: ``95% confident that the r_clean rate is between 0.1 and 0.05''
		bbippClean.putParameter("epsilon2", 0.1);
		bbippClean.putParameter("theta2", 0.95);
			
		
		AUVInner auv = new AUVInner();
		auv.run(ipspFailClean, bbippDamage,bbippClean);
	}
}


class AUVInner{
	
	boolean    X        = true;
	double x_OK         = 0.9999; // a place holder for the verification result
	double p_OK         = 0.1; // the probability that a chain is not dirty
	double r_inspect    = 1/30.0; // assuming the average chain inspection time is 30 seconds
	double r_travel     = 1/120.0; // assuming the average travel time between chains is 2*60 seconds

	// oK, let us say average cleaning time is 60 seconds,  
	// after the cleaning, the 3 outcomes with associated prob: fail (0.95), succ(1-0.95-10^{-5}), damage (0.00001)
	// in this scenario, we have the rates as follows:
	double r_fail_clean = 0.95/60.0;
	double r_damage     = 0.000001/60.0;
	double r_clean      = (1-0.95-0.000001)/60.0;
	
	double r_retry      = 1; // wait 1 seconds in and then retry..

    //Bernoulli distribution for X
	BinomialDistribution xBinomial = new BinomialDistribution(1, x_OK);
    //Bernoulli distribution for P
	BinomialDistribution pBinomial = new BinomialDistribution(1, p_OK);
    //Exponential distribution for r_inspect
	ExponentialDistribution inspectExponential = new ExponentialDistribution(1/r_inspect);// Note, the function requires a mean of the distribution
    //Exponential distribution for r_travel
	ExponentialDistribution travelExponential = new ExponentialDistribution(1/r_travel);
    //Exponential distribution for r_retry
	ExponentialDistribution retryExponential = new ExponentialDistribution(1/r_retry);
    //Exponential distribution for holding time at dirty stage
	ExponentialDistribution dirtyExponential = new ExponentialDistribution(1/(r_damage + r_clean + r_fail_clean));
	// Above is yours Simos, which is correct .. :-)
	// And it represents the Distribution of ``the minimum of exponential random variables'' 
	//which is indeed what we want \\url{https://en.wikipedia.org/wiki/Exponential_distribution#Distribution_of_the_minimum_of_exponential_random_variables}

	Random rand = new Random(System.currentTimeMillis());
	

	boolean checkX (){ 
	    int newX = xBinomial.sample(1)[0]; 	    		
	    return newX==1? true : false;
//	    return true;
	}
	
	
	public void run(Estimator ipsp, Estimator bbipp, Estimator bbipp2) {
		double inspectT = inspectExponential.sample();
	    int pOK    		= pBinomial.sample(1)[0];
	    double travelT;
	    double dirtyHoldingTSum = 0;
	   // double dirtyHoldingRSum = 0; // this variable seems not relevant and you are not using it.
	    int dirtyHoldingTCount 	= 0;

	    if (pOK==1){//go to DONE
	    	travelT = travelExponential.sample(); 
	        System.out.printf( "Chain is OK, going to the next, Travelling time: %.3f\n", travelT );
	    }
	    else{//not OK --> chain is dirty
	        int dirtyResult = 0;//      = 0;
	        do {
	            boolean X= checkX(); // get from the verification result
	            if (X){//try to clean the chain
	                double sumProb              = r_clean + r_fail_clean + r_damage;
	                double normCleanProb        = r_clean/sumProb;
	                double normFailCleanProb    = r_fail_clean/sumProb;
	                double normDamageProb       = r_damage/sumProb;
	                double v                    = rand.nextDouble(); //a random number to determine the 3 possible outcome after cleaning: cata fail, succ or fail.
	                
	                double holdingT		= dirtyExponential.sample();
	                //double holdingR		=  1/holdingT;

	                System.out.printf("Num. of fails in cleaning: %d  Holding_time=%.3f\t ", dirtyHoldingTCount,holdingT);
//	                System.out.printf("P_clean=%.5f\t P_dam=%.5f\t P_failClean=%.5f\t P_sample=%.5f", normCleanProb, normDamageProb, normFailCleanProb, v);
	                
	                dirtyHoldingTSum   += holdingT;
	               // dirtyHoldingRSum   += holdingR;

	                //IPSP estimator for the r_fail_clean
	        		ipsp.putParameter("t_i",  dirtyHoldingTSum);
	        		ipsp.putParameter("n_ij", dirtyHoldingTCount);
	        		Number[] ipspResults = ipsp.execute();	        		
	        		System.out.printf(" *  r_fail_clean=%.8f:%.8f, ", ipspResults[0], ipspResults[1]);

	        		//BIPP estimator
	        		bbipp.putParameter("t", dirtyHoldingTSum);	        				
	        		Number[] bbippDamResults = bbipp.execute();
	        		System.out.printf("r_damage=%.8e:%.8e, ", bbippDamResults[0], bbippDamResults[1]);
	        		
	        		//BIPP estimator
	        		bbipp2.putParameter("t", dirtyHoldingTSum);	        				
	        		Number[] bbippCleanResults = bbipp2.execute();
	        		System.out.printf("r_clean=%2.8e:%2.8e\t", bbippCleanResults[0], bbippCleanResults[1]);


	                if (v <= normDamageProb){
	                    dirtyResult = -1; //catastrophic failure
	                    System.out.println("Catastrophic failure");
	                }
	                else if (v <= normCleanProb + normDamageProb){
	                    dirtyResult = +1; //chain cleaned successfully
	                    System.out.println("Chain cleaned");
	                }
	                else{
	                    dirtyResult = 0; //failed cleaning the chain
		                dirtyHoldingTCount ++;
	                }
	            }
	            else { //skip cleaning the chain, go to the next chain
                    dirtyResult = 2;
	            	System.out.println("Skipping current chain");
	            }

	        } while (X && (dirtyResult == 0));
	        
	        if (dirtyResult > -1){ //chain cleaned successfully
	        	travelT = travelExponential.sample();
                System.out.printf("Travelling to next chain with time %.3f\n", travelT);
	        }

	    }
	    
	    if (dirtyHoldingTCount >0)
	    	System.out.printf("Dirty state avg holding time=%.4f\t holding rate=%.4f",dirtyHoldingTSum/dirtyHoldingTCount, dirtyHoldingTCount/dirtyHoldingTSum);
	}
	
	
}

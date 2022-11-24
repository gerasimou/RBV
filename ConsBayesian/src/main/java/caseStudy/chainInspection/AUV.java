package caseStudy.chainInspection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.AbstractWell;
import org.apache.commons.math3.random.Well19937c;

import _main.PRISMPSY_API_Runner;
import _main.Property;
import approach.estimators.Estimator;
import utilities.Utility;



public class AUV{

	List<WindTurbineChain> chainsList;
	
	boolean    X        = true;
	double p_OK         ;		// the probability that a chain is not dirty
	double r_inspect    ;		// the average chain inspection rate 
	double r_travel     ;		// the average travel time between chains
	double r_prepare    ;		// the average waiting time before retrying

	// oK, let us say average cleaning time is 60 seconds,  
	// after the cleaning, the 3 outcomes with associated prob: fail (0.95), succ(1-0.95-10^{-5}), damage (0.00001)
	double p_fail_clean 		;	// the probability of failing to clean a dirty chain
	double p_damage     		;	// the probability of damaging the UUV or the infrastructure
	double p_clean      		;	// the probability of successfully cleaning the chain, i.e., 1-p_fail_clean-p_damage
	double averageHoldingTime	;	// the average time spent in the state where the UUV cleans the i-th chain

	int seed		 = 25000;
	
	String verificationTime = "";
	
	//Random number generator with seed for reproducibility
	AbstractWell rng;

    //Bernoulli distribution for X
	BinomialDistribution xBinomial;// = new BinomialDistribution(rng, 1, x_OK);

	//Bernoulli distribution for P
	BinomialDistribution pBinomial;// = new BinomialDistribution(rng, 1, p_OK);
    
	//Exponential distribution for r_inspect
	ExponentialDistribution inspectExponential;// = new ExponentialDistribution(rng, 1/r_inspect);// Note, the function requires a mean of the distribution
    
	//Exponential distribution for r_travel
	ExponentialDistribution travelExponential;// = new ExponentialDistribution(rng, 1/r_travel);
    
	//Exponential distribution for r_retry
	ExponentialDistribution prepareExponential;// = new ExponentialDistribution(rng, 1/r_retry);
    
	//Exponential distribution for holding time at dirty stage
	//It represents the Distribution of ``the minimum of exponential random variables'' 
	//which is indeed what we want \\url{https://en.wikipedia.org/wiki/Exponential_distribution#Distribution_of_the_minimum_of_exponential_random_variables}
	ExponentialDistribution dirtyExponential;// = new ExponentialDistribution(rng, 1/((r_damage + r_clean + r_fail_clean)/averageHoldingTime));

	
	int currentlyInspectedChain = 0;
	PRISMPSY_API_Runner prismPSY;
	ModelGenerator mg;

	
	
	public AUV (int seed, int chainsNum, double x_OK, double p_OK, double r_inspect, double r_travel, 
				double r_prepare, double p_fail_clean, double p_damage, double averageHoldingTime) {
		
		this.seed 			= seed; 
		
		//create random generators
		rng  = new Well19937c(seed);
		mg 	 = new ModelGenerator();

		this.chainsList 	= new ArrayList<>();
		for (int i=0; i<chainsNum; i++) 
			chainsList.add(new WindTurbineChain(i+1, rng));
		
		prismPSY 				= new PRISMPSY_API_Runner();
		
		
		//initialise chain condition variables
		this.p_fail_clean 		= p_fail_clean;
		this.p_damage			= p_damage;
		this.p_clean			= 1-p_fail_clean-p_damage;//(1-0.5-0.0000001)/90.0;
		this.averageHoldingTime	= averageHoldingTime;

		//initialise distributions
		xBinomial 			= new BinomialDistribution(rng, 1, x_OK);
		pBinomial 			= new BinomialDistribution(rng, 1, p_OK);
		inspectExponential 	= new ExponentialDistribution(rng, r_inspect);// Note: the function requires a mean of the distribution
		travelExponential 	= new ExponentialDistribution(rng, r_travel);
		prepareExponential 	= new ExponentialDistribution(rng, r_prepare);
		dirtyExponential 	= new ExponentialDistribution(rng, (p_damage + p_clean + p_fail_clean)/averageHoldingTime); //->1/averageHoldingTime
		
		
		//initialise CTMC model parameters
		this.p_OK 		= p_OK;
		this.r_travel	= r_travel;
		this.r_inspect	= r_inspect;
		this.r_prepare	= r_prepare;
	}
	
	
	
	public void doMission() {
		int chainsNum = chainsList.size();
		
		for (; currentlyInspectedChain<chainsNum; currentlyInspectedChain++) {
			writeToFile("Chain " + currentlyInspectedChain, true);
			
			
			Estimator rFailClean = chainsList.get(currentlyInspectedChain).failCleanIPSP;
			Estimator rDamage	 = chainsList.get(currentlyInspectedChain).damageBIPP;
			Estimator rClean 	 = chainsList.get(currentlyInspectedChain).cleanBIPP;
			
			run(rFailClean, rDamage, rClean, currentlyInspectedChain==chainsNum-1, currentlyInspectedChain);
		}
		
		try {
			Utility.exportToFile("verificationTime.txt", "\n"+verificationTime, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void run(Estimator ipspFailClean, Estimator bbippDamage, Estimator bbippClean, boolean finalChain, int currentlyInspectedChain) {
		double inspectTime = 1.0/inspectExponential.sample();
	    double travelT;
	    double holdingTSum = 0;
	    int dirtyHoldingTCount 	= 0;
	    
	    int verificationCount = 0;

	    int pOK    		= pBinomial.sample(1)[0];
	    if (pOK==1){//go to DONE
	    	travelT = 1.0/travelExponential.sample(); 
	    	writeToFile(String.format("\tClean, Going to the next, Travelling time: %.3f", travelT ), true);
	    }
	    else{//not OK --> chain is dirty
	        int dirtyResult = 0;//      = 0;
	        writeToFile("\tDirty, Cleaning needed\n", true);
	        do {
	        	long start = System.currentTimeMillis();
	            boolean X  = checkX(dirtyHoldingTCount); // get from the verification result
	            long end   = System.currentTimeMillis();
	            verificationTime += "Chain"+ (currentlyInspectedChain) +"-E"+ 
	            					(verificationCount++) +","+ (end-start) +"\n";
	            
	            if (X){//try to clean the chain
	                dirtyHoldingTCount ++;
	                
	                double holdingT		= 1.0/dirtyExponential.sample();

	                writeToFile(String.format("Cleaning:%d  Time=%.3f\t ", dirtyHoldingTCount,holdingT).toString(), true);
	                
	                holdingTSum   += holdingT;

	                //Run IPSP estimator r_fail_clean
	        		ipspFailClean.putParameter("t_i",  holdingTSum);
	        		ipspFailClean.putParameter("n_ij", dirtyHoldingTCount);
	        		Number[] ipspResults = ipspFailClean.execute();	        		
	        		//writeToFile(String.format(" *  r_fail_clean=%.8f:%.8f, ", ipspResults[0], ipspResults[1]));

	        		//Run BIPP estimator r_damage
	        		bbippDamage.putParameter("t", holdingTSum);	        				
	        		Number[] bbippDamResults = bbippDamage.execute();
	        		//writeToFile(String.format("r_damage=%.8e:%.8e, ", bbippDamResults[0], bbippDamResults[1]));
	        		
	        		//Run BIPP estimator r_clean
	        		bbippClean.putParameter("t", holdingTSum);	        				
	        		Number[] bbippCleanResults = bbippClean.execute();
	        		//writeToFile(String.format("r_clean=%2.8e:%2.8e\t", bbippCleanResults[0], bbippCleanResults[1]));

	                double v                    = rng.nextDouble(); //a random number to determine the 3 possible outcome after cleaning: cata fail, succ or fail.
	                if (v <= p_damage){
	                    dirtyResult = -1; //catastrophic failure
	                    writeToFile("Catastrophic failure ", true);
	                }
	                else if (v <= p_clean + p_damage){
	                    dirtyResult = +1; //chain cleaned successfully
	                    writeToFile("\nChain cleaned - ", true);
	                }
	                else{
	                    dirtyResult = 0; //failed cleaning the chain
	                }
	            }
	            else { //skip cleaning the chain, go to the next chain
                    dirtyResult = 2;
                    writeToFile("Skipping current chain - ", true);
	            }

	        } while (X && (dirtyResult == 0));
	        
	        if (dirtyResult > -1){ //chain cleaned successfully
	        	travelT = 1.0/travelExponential.sample();
	        	if (finalChain)
		        	writeToFile(String.format("Travelling back to base with time %.3f\n", travelT).toString(), true);
	        	else
	        		writeToFile(String.format("Travelling to next chain with time %.3f\n", travelT).toString(), true);
	        }

	    }
	    
	    writeToFile("\n\n", true);
	}
	
	
	
	boolean checkX (int times){
		//simulation
//	    int newX = xBinomial.sample(1)[0]; 	    		
//	    return newX==1? true : false;

//		if (times==0)
//			return true;
		
		Map<Integer[], Property[]> resultsMap = new HashMap<>();

		double P_DAMAGE = 1;
		double E_LEFT = 0;	  
		try {
			P_DAMAGE = Double.parseDouble(Utility.getValue("P_DAMAGE_THRESHOLD"));
			E_LEFT	 = Double.parseDouble(Utility.getValue("E_LEFT_THRESHOLD"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		boolean xResult    = false;
		int     maxChains  = -1;
		
		//prepare configurations for remaining chains
		List<Integer[]> configsList = getConfigsList();
		List<WindTurbineChain> chainsToBeInspected = chainsList.subList(currentlyInspectedChain, chainsList.size());
		
		//prepare properties and get its temporary filename
		String propsFileName = mg.getPropsFileName(chainsToBeInspected);

		//prepare ranges for the parameters of this model
		String paramsWithRanges = mg.getRangeCommand(chainsToBeInspected);
		writeToFile("\t\t" + paramsWithRanges, true);

		for (int i=configsList.size()-1; i>=(configsList.size()-1)/2; i--) {
			Integer[] config		 = configsList.get(i);

			//prepare model and get its temporary filename
			String modelFileName = mg.getModelFileName(chainsToBeInspected, config, p_OK, r_inspect, r_travel, r_prepare);

			
			//for each property, this function results its minimum and maximum values
 			Property[] properties = prismPSY.calculatePropertiesBounds(modelFileName, propsFileName, paramsWithRanges);
			writeToFile("\n\t\t" +Arrays.toString(config), false);
			
 			for (Property p : properties) {
				writeToFile(Arrays.toString(p.getMinMax()) +"\t", false);
			}
 			
 			//evaluate
//			if (properties[0].getMin().doubleValue()>(1-P_DAMAGE) && config[0].equals("1")) {
//				writeToFile("\t" + Arrays.toString(config) +"\n");		
//				xResult = true;
//			}
 			if (properties[0].getMin().doubleValue()>(1-P_DAMAGE)) { //R1
 				if ( (properties.length >1) && (properties[1].getMax().doubleValue()<=(E_LEFT))	) { //R2 
	 				resultsMap.put(config, properties);
	 				int chainsCleaned = Arrays.stream(config).mapToInt(Integer::intValue).sum();
	 				if (chainsCleaned > maxChains)
	 					maxChains = chainsCleaned;
					xResult = true;
 				}
 			}
		}
				
		writeToFile("\n", true);
		return xResult;
	}
	
	
	
	private List<Integer[]> getConfigsList (){
		List<Integer[]> configsList = new ArrayList<>();
		
		int chainsToInspect = chainsList.size() - currentlyInspectedChain;
		int totalConfigs = (int)Math.pow(2, chainsToInspect);
		int bitsSize = Integer.toBinaryString(totalConfigs).length()-1; 
		for (int i=1; i<totalConfigs; i++) {
			String binaryStr = String.format("%"+bitsSize+"s", Integer.toString(i,2)).replace(' ', '0');//.replace("",",");
//			String config[] = binaryStr.split("");
			Integer[] config = Arrays.stream(binaryStr.split("")).mapToInt(Integer::parseInt).boxed().toArray(Integer[]::new);
			configsList.add(config);
		}
		
		return configsList;
	}

	
	
	private void writeToFile (String msg, boolean systemOut) {
		try {
			if (systemOut)
				System.out.print(msg);
			Utility.exportToFile("log.txt", msg, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static void main(String[] args) throws IOException {
		try {
			
			// SEED for reproducibility 
			int seed 					= Integer.parseInt(Utility.getValue("SEED").strip());

			//number of chains
			int chains 					= Integer.parseInt(Utility.getValue("CHAINS"));
			
			//the probability that a chain is not dirty
			double p_OK         		= Double.parseDouble(Utility.getValue("CHAIN_OK"));
		
			//the average chain inspection rate is ...
			double r_inspect    		= Double.parseDouble(Utility.getValue("R_INSPECT"));
		
			// the average travel rate between chains is ..
			double r_travel     		= Double.parseDouble(Utility.getValue("R_TRAVEL"));	

			// the average rate before trying to clean a dirt chain again 
			double r_prepare      		= Double.parseDouble(Utility.getValue("R_PREPARE"));

			// the average time for cleaning a chain
			double averageHoldingTime	= Double.parseDouble(Utility.getValue("CLEAN_DURATION"));

			// the probability of failing to clean a dirty chain
			double p_fail_clean 		= Double.parseDouble(Utility.getValue("P_FAIL_CLEAN"));		

			// the probability of damaging the UUV or the infrastructure
			double p_damage     		= Double.parseDouble(Utility.getValue("P_DAMAGE"));

			
			double x_OK         		= 0.9999; 	// a place holder for the verification result, i.e., to clean the chain
	
			
			Utility.exportToFile("log.txt", "Chains " + chains +"\n", false);
			
			AUV auv 		 = new AUV(seed, chains, x_OK, p_OK, r_inspect, r_travel, r_prepare, 
									   p_fail_clean, p_damage, averageHoldingTime);
	
			auv.doMission();	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
}

package caseStudy.chainInspection;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import _main.Utility;

public class ModelGenerator {

	int modelID = 1;
	
	private String buildModel(List<WindTurbineChain> chainsList, double p_OK, double r_inspect, double r_travel, double r_prepare) {
		StringBuilder model = new StringBuilder();
		model.append("ctmc\n");
		model.append("//Fixed params for all chains\n");
		model.append("const double p_OK			= " + p_OK +";     // the probability that a chain is not dirty\n");
		model.append("const double r_inspect	= " + r_inspect +";// assuming the average chain inspection time is 30 seconds\n");
		model.append("const double r_travel     = " + r_travel  +";// assuming the average travel time between chains is 2*60 seconds\n");
		model.append("const double r_prepare      = " + r_prepare   +";\n");

		for (WindTurbineChain chain : chainsList) {
			model.append(chain.getChainCommands());
		}

		model.append("\n//Dummy variable for chain 0, i.e., starting the model from chain 1\n");
		model.append("const int chain" + (chainsList.get(0).getID()-1) +" = 5;\n\n");

		
		modelBuilderTransitionRewards(model, chainsList);
//		modelBuilderStateRewards(model, chainsList);
		
		
//		System.out.println(model.toString());
		return model.toString();
	}
	
	
	private void modelBuilderStateRewards (StringBuilder model, List<WindTurbineChain> chainsList) {
		for (WindTurbineChain chain : chainsList) {
			int ID = chain.getID();
			model.append("//Module for chain " + ID +"\n");
			model.append("module chain"+ID +"\n");				////module chain1
			model.append("chain"+ID +": [0..5] init 0;\n");	//	auvS : [0..5] init 0;
			model.append("[] chain"+ID +"=0 & chain"+(ID-1) +"=5 -> p_OK * r_inspect 	: (chain"+ID + "'=1);\n");			//	[] auvS=0 -> p_OK * r_inspect 		: (auvS'=1);
			model.append("[] chain"+ID +"=0 & chain"+(ID-1) +"=5-> (1-p_OK) * r_inspect 	: (chain"+ID + "'=2);\n");		//	[] auvS=0 -> (1-p_OK) * r_inspect 	: (auvS'=2);
			model.append("[] chain"+ID +"=1 -> r_travel 	: (chain"+ID + "'=5);\n");					//	[] auvS=1 -> r_travel				: (auvS'=5);
			model.append("[] chain"+ID +"=2 -> x"+ID + "* r_clean"+ID +": (chain"+ID + "'=1);\n");		//	[] auvS=2 -> x1 * r_clean1 			: (auvS'=1); 
			model.append("[] chain"+ID +"=2 -> x"+ID + "* r_fail_clean"+ID +": (chain"+ID + "'=3);\n");//	[] auvS=2 -> x1 * r_fail_clean1   	: (auvS'=3);  
			model.append("[] chain"+ID +"=2 -> (1-x"+ID + ")* r_travel : (chain"+ID + "'=5);\n");		//	[] auvS=2 -> (1-x1) * r_travel   	: (auvS'=5);
			model.append("[] chain"+ID +"=2 -> x"+ID + "* r_damage"+ID +": (chain"+ID + "'=4);\n");	//	[] auvS=2 -> r_damage1 				: (auvS'=4);
			model.append("[] chain"+ID +"=3 -> r_prepare : (chain"+ID + "'=2);\n");						//	[] auvS=3 -> r_prepare				: (auvS'=2);
			model.append("endmodule\n\n");
		}
		
		
		//add energy cost
		model.append("//energy\n");
		model.append("rewards \"energy\"\n");
		for (WindTurbineChain chain : chainsList) {
			int ID = chain.getID();
			model.append("\tchain"+ID +"=2 : x"+ +ID +"*e"+ ID +"clean + (1-x"+ ID +")*e"+ ID +"travel;\n");
		}
		model.append("endrewards\n\n");
	}
	
	
	private void modelBuilderTransitionRewards (StringBuilder model, List<WindTurbineChain> chainsList) {
		for (WindTurbineChain chain : chainsList) {
			int ID = chain.getID();
			model.append("//Module for chain " + ID +"\n");
			model.append("module chain"+ID +"\n");				////module chain1
			model.append("chain"+ID +": [0..5] init 0;\n");	//	auvS : [0..5] init 0;

			model.append("[chain" +ID+ "inspect] chain"+ID +"=0 & chain"+(ID-1) +"=5 -> p_OK * r_inspect 	: (chain"+ID + "'=1);\n");			//	[] auvS=0 -> p_OK * r_inspect 		: (auvS'=1);
			model.append("[chain" +ID+ "inspectB] chain"+ID +"=0 & chain"+(ID-1) +"=5-> (1-p_OK) * r_inspect 	: (chain"+ID + "'=2);\n");		//	[] auvS=0 -> (1-p_OK) * r_inspect 	: (auvS'=2);
			model.append("[chain" +ID+ "travel] chain"+ID +"=1 -> r_travel 	: (chain"+ID + "'=5);\n");
			model.append("[chain" +ID+ "clean] chain"+ID +"=2 -> x"+ID + "* r_clean"+ID +": (chain"+ID + "'=1);\n");		//	[] auvS=2 -> x1 * r_clean1 			: (auvS'=1); 
			model.append("[chain" +ID+ "cleanB] chain"+ID +"=2 -> x"+ID + "* r_fail_clean"+ID +": (chain"+ID + "'=3);\n");//	[] auvS=2 -> x1 * r_fail_clean1   	: (auvS'=3);  
			model.append("[chain" +ID+ "cleanC] chain"+ID +"=2 -> (1-x"+ID + ")* r_travel : (chain"+ID + "'=5);\n");		//	[] auvS=2 -> (1-x1) * r_travel   	: (auvS'=5);
			model.append("[chain" +ID+ "cleanD] chain"+ID +"=2 -> x"+ID + "* r_damage"+ID +": (chain"+ID + "'=4);\n");	//	[] auvS=2 -> r_damage1 				: (auvS'=4);
			model.append("[chain" +ID+ "prepare] chain"+ID +"=3 -> r_prepare : (chain"+ID + "'=2);\n");						//	[] auvS=3 -> r_prepare				: (auvS'=2);
			model.append("endmodule\n\n");
		}
		
//		for (WindTurbineChain chain : chainsList) {
//			int ID = chain.getID();
//			model.append("//Module for chain " + ID +"\n");
//			model.append("module chain"+ID +"\n");				////module chain1
//			model.append("chain"+ID +": [0..5] init 0;\n"); 
//			model.append("[chain" +ID+ "inspect] chain"+ID +"=0 & chain"+(ID-1) +"=5 -> p_OK * r_inspect 	: (chain"+ID + "'=1) + (1-p_OK) * r_inspect : (chain"+ID + "'=2);\n");
//			model.append("[chain" +ID+ "travel] chain"+ID +"=1 -> r_travel 	: (chain"+ID + "'=5);\n");
//			model.append("[chain" +ID+ "clean] chain"+ID +"=2 -> x"+ID + "* r_clean"+ID +": (chain"+ID + "'=1) + x"+ID + "* r_fail_clean"+ID +": (chain"+ID + "'=3) + (1-x"+ID + ")* r_travel : (chain"+ID + "'=5) +  x"+ID + "* r_damage"+ID +": (chain"+ID + "'=4);\n");
//			model.append("[chain" +ID+ "prepare] chain"+ID +"=3 -> r_prepare : (chain"+ID + "'=2);\n");						//	[] auvS=3 -> r_prepare				: (auvS'=2);
//			model.append("endmodule\n\n");
//		}
		
		
		//add energy cost
		model.append("//energy\n");
		model.append("rewards \"energy\"\n");
		for (WindTurbineChain chain : chainsList) {
			int ID = chain.getID();
			model.append("[chain" +ID+ "inspect]  true : e" + ID +"inspect;\n");
			model.append("[chain" +ID+ "travel] true : e" + ID +"travel;\n");
			model.append("[chain" +ID+ "prepare] true : e" + ID +"prepare;\n");
			model.append("[chain" +ID+ "clean] true : x"+ +ID +"*e"+ ID +"clean + (1-x"+ ID +")*e"+ ID +"travel;\n");
		}
		model.append("endrewards\n\n");
	}
	
	
	private String buildConfiguration(List<WindTurbineChain> chainsList, Object[] configX) {
		StringBuilder config = new StringBuilder();
		config.append("//Configuration parameters\n");
		for (int i=0; i<configX.length; i++) {
			config.append("const int x"+ chainsList.get(i).getID() + "=" + configX[i] +";\n"); 
		}
		
//		System.out.println(config.toString());
		return config.toString();
	}
	
	
	public String getModelFileName (List<WindTurbineChain> chainsList, Integer[] configX, double p_OK, double r_inspect, double r_travel, double r_prepare) {
		StringBuilder model = new StringBuilder();
		model.append(buildModel(chainsList, p_OK, r_inspect, r_travel, r_prepare));
		model.append(buildConfiguration(chainsList, configX));
		
		String path = "models/auv/other/temp";
		File file = new File(path);
		if (!file.exists() || (file.exists() && !file.isDirectory())){
			file.mkdirs();
		}

		String modelFileName = path + "/auv"+ modelID +".sm";
		Utility.exportToFile(modelFileName, model.toString(), false);
		modelID++;
		return modelFileName;
	}
	
	
	public String getPropsFileName(List<WindTurbineChain> chainsList) {
		StringBuilder props = new StringBuilder();

		//add reliability property
		String reliabilityProp = "P=? [true U[0, 60] chain" + chainsList.get(chainsList.size()-1).getID() +"=5]\n";
		props.append(reliabilityProp);
		
		//add energy property
//		String energyProp = "R{\"energy\"}=? [ F chain6=5 ";//C<= 60*60 ]\n";
//		Iterator<WindTurbineChain> it = chainsList.iterator();
//		while (it.hasNext()) {
//			energyProp += " | chain" + it.next().getID() +"=4";
//		}
//		energyProp += "]\n";

		String energyProp = "R{\"energy\"}=? [ C <= 60]";//C<= 60*60 ]\n";
		props.append(energyProp);

//		String propsFileName = "models/auv/other/temp/auv" + modelID +".csl";
		String propsFileName = "models/auv/other/temp/auv.csl";
		Utility.exportToFile(propsFileName, props.toString(), false);
		
		return propsFileName;
	}
	
	
	public String getRangeCommand(List<WindTurbineChain> chainsList) {
		StringBuilder model = new StringBuilder();

		Iterator<WindTurbineChain> it = chainsList.iterator();
		while (it.hasNext()) {
			model.append(it.next().getChainRangeCommands());
			if (it.hasNext())
				model.append(",");
		}

		return model.toString();
	}
}
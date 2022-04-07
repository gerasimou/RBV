package examples.estimators;

import java.util.Arrays;

import _main.PRISMPSY_API_Runner;
import _main.Property;

public class PSYRunner {

	public static void main(String[] args) {
//		testPSYGoogle();
		testPSYAUV();
	}

	public static void testPSYAUV() {
		//4) get bounds for properties
		PRISMPSY_API_Runner runner = new PRISMPSY_API_Runner();

		//AUV
//		String modelFile			= "models/auv/auv.sm";
//		String propertiesFile		= "models/auv/auv.csl";
//		String paramsWithRanges		= "r_fail_clean=0.03014513:0.3511981";
//		String paramsWithRanges		= "r_fail_clean=0.02023898:0.02087312,r_damage=1.11018659e-06:4.93366091e-04,r_clean=2.33993466e-02:4.80370380e+00";
//		String paramsWithRanges		= "r_fail_clean=0.02607224:0.02671408,r_damage=1.10930221e-06:2.53191929e-04,r_clean=4.92594216e-04:4.80004189e+00";
		//Test
		String propertiesFile		= "models/auv/other/temp/auv.csl";
		String modelFile			= "models/auv/other/temp/auv26.sm";
		String paramsWithRanges		= "r_fail_clean2=0.006829092129209808:0.006966786081121085,r_damage2=1.1110233987425101E-8:5.180088606043916E-5,r_clean2=0.0013798972824028866:0.16666666666666669," +
//				"r_fail_clean3=0.006829092129209808:0.006966786081121085,r_damage3=1.1110233987425101E-8:5.180088606043916E-5,r_clean3=0.0013798972824028866:0.16666666666666669";
				"r_fail_clean3=0.013333333333333332:0.01633333333333333,r_damage3=1.111111012345683E-8:0.04598495310627847,r_clean3=0.4757412950760129:0.59729667957001";
		
		
		//for each property, this function results its minimum and maximum values
		Property[] properties = runner.calculatePropertiesBounds(modelFile, propertiesFile, paramsWithRanges);
		for (Property p : properties) {
			System.out.println(Arrays.toString(p.getMinMax()));
		}
	}
	
	public static void testPSYGoogle() {
		//4) get bounds for properties
		PRISMPSY_API_Runner runner = new PRISMPSY_API_Runner();
		
		String modelFile			= "models/Google/googleNoEvolvables.sm";
		String propertiesFile		= "models/Google/google.csl";
		String paramsWithRanges		= "c_hw_repair_rate=0.8:1.0,c_hw_fail_rate=0.5:1.0";
		
		//for each property, this function results its minimum and maximum values
		Property[] properties = runner.calculatePropertiesBounds(modelFile, propertiesFile, paramsWithRanges);
		for (Property p : properties) {
			System.out.println(Arrays.toString(p.getMinMax()));
		}
	}
	
	public static void testPSYCluster() {
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
	}
}

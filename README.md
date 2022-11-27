# Robust Bayesian Verification

 ![Robust Bayesian Verification Framework](images/diagramFramework.png "Robust Bayesian Verification Framework")

* We present a novel **Bayesian learning framework** for the runtime verification of autonomous robots operating in uncertain environments.
* We consider **regular** (occurring regularly during operation) and **singular** events occurring zero/once (catastrophic failures, completion of difficult one-off tasks).
* Our **BIPP Bayesian estimator** (using partial priors) uses prior knowledge and the lack of runtime data to learn expected ranges of rates for singular events.
* Our **IPSP Bayesian estimator** (using imprecise probability with sets of priors) uses prior knowledge and runtime data to learn expected ranges of rates for regural events.
* The learnt rate ranges instrument **interval continuous-time Markov models**.
* Quantitative verification on these models enables **computing expected intervals for key system properties** capturing the parametric uncertainy of the robotic mission.
* We illustrate the framework for the runtime verification of an **offshore wind-turbine inspection and maintenance UAV mission**.


For more information, see [the project's webpage](https://gerasimou.github.io/NMI/) :page_facing_up: and 
<a href="https://drive.google.com/file/d/1dv6EyhTIH36kcLw5ELdu4flwcn-tJC_s/view" target="_blank">
this video
</a>
:movie_camera:

--- 

## Wind-turbine Inspection and Maintenance AUV Mission

We consider an autonomous underwater vehicle (AUV) deployed to execute a structural inspection and cleaning mission of the substructure of an offshore wind farm comprising multiple floating wind turbines. 

The AUV is deployed to collect data about the condition of floating chains to enable the post-mission identification of problems that could affect the structural integrity of the chain. When the visual inspection of a chain is hindered due to accumulated biofouling, the AUV can use its on-board high-pressure water jet to clean the chain and continue with the inspection2. 

The full details of the AUV mission, including its model using continuous-time Markov chains, Quality-of-Sevice requirements, example scenario and a video illustrating the its execution can be found on the [project webpage](https://gerasimou.github.io/NMI/caseStudy/).

--- 

## Running the Robotic Mission

The robotic mission is provided as a en Eclipse-based Java tool that uses Maven for managing the project and its dependencies, and for generating the executable jars.

1. Import the project in your IDE of preference

2. Set the following environment variable (In Eclipse go to Run / Run Configurations / Environment tab / New)

        OSX: DYLD_LIBRARY_PATH = libs/runtime 
        
        *NIX: LD_LIBRARY_PATH = libs/runtime

3. Specify the configuration parameters in file [config.properties](https://github.com/gerasimou/NMI/blob/main/ConsBayesian/config.properties)

4. Run the [AUV class](https://github.com/gerasimou/NMI/blob/main/ConsBayesian/src/main/java/caseStudy/chainInspection/AUV.java)



--- 

For questions and comments, please contact [Simos Gerasimou](mailto:simos.gerasimou@york.ac.uk)


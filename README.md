# Robust Bayesian Verification

 ![Robust Bayesian Verification Framework](images/diagramFramework.png "Robust Bayesian Verification Framework")

* We present a novel **Bayesian learning framework** for the runtime verification of autonomous robots operating in uncertain environments.
* We consider **regular** (occurring regularly during operation) and **singular** events occurring zero/once (catastrophic failures, completion of difficult one-off tasks).
* Our **BIPP Bayesian estimator** (using partial priors) uses prior knowledge and the lack of runtime data to learn expected ranges of rates for singular events.
* Our **IPSP Bayesian estimator** (using imprecise probability with sets of priors) uses prior knowledge and runtime data to learn expected ranges of rates for regural events.
* The learnt rate ranges instrument **interval continuous-time Markov models**.
* Quantitative verification on these models enables **computing expected intervals for key system properties** capturing the parametric uncertainy of the robotic mission.
* We illustrate the framework for the runtime verification of an **offshore wind-turbine inspection and maintenance UAV mission**.


For more information, see [the project's webpage](https://gerasimou.github.io/RBV/) :page_facing_up: and 
<a href="https://drive.google.com/file/d/1fLZ3Bip8Y0KRiaWfMOMRZStPbPpCdHqy/view" target="_blank">
this video
</a>
:movie_camera:

--- 

## Wind-turbine Inspection and Maintenance AUV Mission

We consider an autonomous underwater vehicle (AUV) deployed to execute a structural inspection and cleaning mission of the substructure of an offshore wind farm comprising multiple floating wind turbines. 

The AUV is deployed to collect data about the condition of floating chains to enable the post-mission identification of problems that could affect the structural integrity of the chain. When the visual inspection of a chain is hindered due to accumulated biofouling, the AUV can use its on-board high-pressure water jet to clean the chain and continue with the inspection2. 

The full details of the AUV mission, including its model using continuous-time Markov chains, Quality-of-Sevice requirements, example scenario and a video illustrating the its execution can be found on the [project webpage](https://gerasimou.github.io/RBV/caseStudy/).

--- 

## Installing and Running the AUV Simulation

1) Download the MOOS-IvP simulator as advised 
   <a href="https://oceanai.mit.edu/moos-ivp/pmwiki/pmwiki.php?n=Site.Download" target="_blank">here</a>.

2) Install the MOOS-IvP simulator by following the instructions provided in the simulator for your target operating system (Linux or OSX).

3) Download the AUV simulation code available in the [moos-ivp-rbv](https://github.com/gerasimou/RBV/tree/main/moos-ivp-rbv) directory.

4) Navigate to the ``moos-ivp-rbv`` directory and execute
        
        ./clean.sh
        ./build.sh sUUVRBV
   - If everything went well, you will see the message
       > ``[100%] Built target sUUVRBV``



5) Add the full path to the ``moos-ivp-rbv/bin`` directory to your shell path
   - If everything went well, you will see the message
       > OSX   -> ./bash_profile 
       > Unix  -> ./bashrc

    PATH="FULL_PATH_TO"/moos-ivp-rbv/bin:$PATH

       > replace "FULL_PATH_TO" with the full path leading to the ``moos-ivp-rbv/bin`` directory

6) Navigate to ``moos-ivp-rbv/missions/s3_uuvrbv`` and run the mission by executing

    ./launch.sh 2

       > 2 indicates how fast the simulation will run

**Note:** You can alter the mission by changing the sUUVRBV script in the [rbv.moos](https://github.com/gerasimou/RBV/blob/main/moos-ivp-rbv/missions/s3_uuvrbv/rbv.moos) file.
    
    

--- 

For questions and comments, please contact [Simos Gerasimou](mailto:simos.gerasimou@york.ac.uk)


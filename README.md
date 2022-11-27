# Robust Bayesian Verification

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


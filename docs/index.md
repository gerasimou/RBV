---
hide:
    - footer
    - title
---
# Robust Bayesian Verification 

<!-- Xingyu Zhao, Simos Gerasimou, Radu Calinescu, Calum Imrie, Valentin Robu, and David Flynn -->


???+ tip "Summary"
    - [x] A novel **Bayesian learning framework** enabling the runtime verification of autonomous robots performing critical missions in uncertain environments.

    - [x] Prior knowledge and observations are used to learn expected ranges of values for the occurrence rates of **regural** and **singural** events

        - [ ] **Regular events**: occurring regularly during system operation
        - [ ] **Singular events**: occurring zero/once (catastrophic failures or the completion of difficult one-off tasks)
    
    - [x] The learnt event-rate ranges instrument **interval continuous-time Markov models** 
    
    - [x] Quantitative verification on these models enables computing **expected intervals for key system properties**

    - [x] The framework is used to the case study of verification of an **autonomous robotic mission for underwater infrastructure inspection and repair**

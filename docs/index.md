---
hide:
    - footer
    - title
---
# Robust Bayesian Verification of Autonomous Robots

Xingyu Zhao, Simos Gerasimou, Radu Calinescu, Calum Imrie, Valentin Robu, and David Flynn


???+ abstract
    We develop a novel Bayesian learning framework that enables the runtime verification of autonomous robots performing critical missions in uncertain environments. Our framework exploits prior knowledge and observations of the verified robotic system to learn expected ranges of values for the occurrence rates of its events. We support both events observed regularly during system operation, and singular events such as catastrophic failures or the completion of difficult one-off tasks. Furthermore, we use the learnt event-rate ranges to assemble interval continuous-time Markov models, and we apply quantitative verification to these models to compute expected intervals of variation for key system properties. These intervals reflect the uncertainty intrinsic to many real-world systems, enabling the robust verification of their quantitative properties under parametric uncertainty. We apply the proposed framework to the case study of verification of an autonomous robotic mission for underwater infrastructure inspection and repair. 
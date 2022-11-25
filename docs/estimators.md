---
hide:
    - footer
    - title
---

# **Bayesian Interval Estimators**


## **Bayesian Learning of CTMC Transition Rates**

-  [ ] Given two states $s_i$ and $s_j$ of a continuous-time Markov chain (CTMC) such that transitions from $s_i$ to $s_j$ occur with rate $\lambda$, each transition from $s_i$ to $s_j$ is independent of how state $s_i$ was reached (the Markov property). 
-  [ ] The time spent in state $s_i$ before a transition to $s_j$ is modelled by a homogeneous Poisson process of rate $\lambda$. 
-  [ ] The likelihood that "data" collected by observing the CTMC shows $n$ such transitions occurring within a combined time $t$ spent in state $s_i$ is given by the conditional probability:

!!! quote ""
    $$
    l(\lambda) = \mathit{Pr}\left(data \mid \lambda\right)=\frac{\left(\lambda t\right)^{n}}{n!}e^{-\lambda t}
    $$


- [ ] The rate $\lambda$ is typically unknown, but prior beliefs about its value are available (e.g., from domain experts or from past experience) as a probability (density or mass) function $f(\lambda)$. 
- [ ]  Thus, the Bayes Theorem can be used to derive a _posterior probability function_ that combines the likelihood $l(\lambda)$ and the prior $f(\lambda)$ into a better estimate for $\lambda$ at time $t$:

!!! quote ""
    $$
    f(\lambda\mid data) = \frac{l(\lambda)f(\lambda)}{\int_{0}^{\infty} l(\lambda)f(\lambda) d\lambda}
    $$




## **BIPP Estimator**

The **Bayesian inference using partial priors (BIPP) estimator** is used to model _extremely rare_ events, including major failures and the successful completion of difficult one-off tasks.
Typically, no observations of these events are available and only limited domain knowledge is often available to select and justify a prior distribution for these singular events. 

The BIPP estimator requires only **limited, partial prior knowledge** instead of the complete prior distribution typically needed for Bayesian inference. 
<!-- BIPP provides bounded posterior estimates that are robust in the sense that the ground truth rate values are within the estimated intervals. -->


??? info "Partial prior knowledge"
    Instead of a prior distribution $f(\lambda)$, we assume that we only have limited partial knowledge comprising $m\geq 2$ confidence bounds on $f(\lambda)$:
    $$
    Pr(\epsilon_{i-1} < \lambda \leq \epsilon_i)=\theta_i 
    $$
    where $1\leq i \leq m$, $\theta_i>0$, and $\sum_{i=1}^{m} \theta_i=1$[^1]


[^1]: $Pr(\lambda\geq \epsilon_0)=Pr(\lambda\leq \epsilon_m)=1$; when no specific information is available, $\epsilon_0=0$ and $\epsilon_m=+\infty$.


??? info "BIPP Theorem"
    The set $S_\lambda$ of posterior estimate rates of all prior distributions $f(\lambda)$ satisfying the partial prior knowledge conditions has an infinum $\lambda_l$ and a supremum $\lambda_u$ given by:
    $$
    \lambda_l = \min \{ \frac{\sum_{i=1}^m \[\epsilon_i l(\epsilon_i)(1-x_i)\theta_i+\epsilon_{i-1}l(\epsilon_{i-1})x_i\theta_i\]}
    {\sum_{i=1}^{m} [l(\epsilon_{i})(1-x_i)\theta_i+l(\epsilon_{i-1})x_i\theta_i]} \bigg| \forall 1\leq i\leq m . x_i\in[0,1]
        \}
    $$


!!! info "BIPP closed-form formulae"
	When $m=3$, the BIPP bounds satisfy:

    $$
    \lambda_l \geq  
    \begin{cases}
        \frac{\epsilon_{1}l(\epsilon_{1})\theta_2}{\theta_1+l(\epsilon_{1})\theta_2}, & \text{if } \frac{\theta_2 (\epsilon_{1}-\epsilon_2)}{\theta_1} > \frac{\epsilon_{2}l(\epsilon_{2})- \epsilon_{1}l(\epsilon_{1}) }{l(\epsilon_{1})l(\epsilon_{2})}\\
        \frac{\epsilon_{2}l(\epsilon_{2})\theta_2}{\theta_1+l(\epsilon_{2})\theta_2}, & \text{otherwise}
    \end{cases}
    $$


    $$
    \lambda_u < 
    \begin{cases}
        \frac{\epsilon_1 l(\epsilon_1) \theta_1 +\epsilon_2 l(\epsilon_2) \theta_2+\frac{1}{t} l(\frac{1}{t})(1-\theta_1-\theta_2)} { l(\epsilon_1) \theta_1 }, & \text{if } t < \frac{1}{\epsilon_{2}}\\
        \frac{\epsilon_1 l(\epsilon_1) \theta_1 +\frac{1}{t} l(\frac{1}{t}) \theta_2+\epsilon_2 l(\epsilon_2)(1-\theta_1-\theta_2)} { l(\epsilon_1) \theta_1 }, & \text{if }  \frac{1}{\epsilon_{2}} \leq t \leq \frac{1}{\epsilon_{1}}\\
        \frac{\epsilon_1 l(\epsilon_1) (\theta_1+\theta_2) +\epsilon_2 l(\epsilon_2)(1-\theta_1-\theta_2)} { l(\epsilon_1) \theta_1 }, & \text{otherwise} %  t > \frac{1}{\epsilon_{1}}
    \end{cases}
    $$

    - When $m=2$, $\epsilon_2=\epsilon_1$ and $\theta_2=0$




## **IPSP Estimator**

The **Bayesian inference using imprecise probability with sets of priors (IPSP) estimator** is used to model _regularly occurring_ events. 

Instead of point values, the IPSP estimator uses ranges $[\underline{t}^{(0)},\overline{t}\textrm{}^{(0)}]$ and $[\underline{\lambda}^{(0)},\overline{\lambda}\textrm{}^{(0)}]$ for the prior knowledge of time and rate, respectively.


??? info "Posterior value calculation"
    The posterior value $\lambda^{(t)}$ for the transition rate after observing $n$ transitions within $t$ time units is derived using classical Bayesian theory:
    
    $$
    \lambda^{(t)} =\frac{t^{(0)}}{t+t^{(0)}}\lambda^{(0)} +\frac{t}{t+t^{(0)}}\frac{n}{t}
    $$

!!! info "IPSP closed-form formulae"
    Given uncertain prior parameters $t^{(0)}\in[\underline{t}^{(0)},$ $\overline{t}\textrm{}^{(0)}]$ and $\lambda^{(0)}\in[\underline{\lambda}^{(0)},\overline{\lambda}\textrm{}^{(0)}]$, the posterior rate $\lambda^{(t)}$ can range in the interval  $[\underline{\lambda}^{(t)},\overline{\lambda}\textrm{}^{(t)}]$, where:

    $$
    \underline{\lambda}^{(t)}=
    \begin{cases} 
        \frac{\overline{t}\text{}^{(0)}\underline{\lambda}^{(0)}+n}{\overline{t}\text{}^{(0)}+t}, & \text{if } \frac{n}{t} \geq \underline{\lambda}^{(0)}\\
        \frac{\underline{t}^{(0)}\underline{\lambda}^{(0)}+n}{\underline{t}^{(0)}+t}, & \text{otherwise} %\quad \frac{n}{t} <\underline{\lambda}^{(0)}
    \end{cases}
    $$

    $$
    \overline{\lambda}^{(t)}=
    \begin{cases} 
        \frac{\overline{t}\text{}^{(0)}\overline{\lambda}\text{}^{(0)}+n}{\overline{t}\text{}^{(0)}+t}, & \text{if } \frac{n}{t} \leq \overline{\lambda}\text{}^{(0)}\\
        \frac{\underline{t}^{(0)}\overline{\lambda}\text{}^{(0)}+n}{\underline{t}^{(0)}+t}, & \text{otherwise} %\quad \frac{n}{t} >\overline{\lambda}\text{}^{(0)}
    \end{cases}
    $$
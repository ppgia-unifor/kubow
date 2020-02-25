![Kubow Logo](/images/kubow-logo-right.png)

Kubow is an architecture-based self-adaptation service for Kubernetes applications. 

Kubow was implemented as a Docker and Kubernetes customization of the [Rainbow](https://github.com/cmu-able/rainbow) self-adaptive framework, originally developed at Carnegie Mellon University's [ABLE research group](https://www.cs.cmu.edu/~able/). 

As with Rainbow, Kubow's primary features are:

* separates out the concern of self-adaptation from the managed system;
* uses software archiecture models as the basis for reasoning about the state of the system and applicable adaptations;
* uses utility-based decision making to determine the best adaptation among a set of potentially applicable adaptations;

To watch a simple example of Kubow in action, check out this [video](https://youtu.be/_-aLNksiKXI).

## Getting started

To get started with Kubow, you first need to install some auxiliary tools (e.g., metrics-server, custom-metrics-api, prometheus, kube-state-metrics) in your Kubernetes cluster. Those tools' deployment files can be found in the [tools](./tools) folder.

After that, you can go to the [samples](./samples) folder and choose one the available sample applications to be deployed and managed by Kubow. Each application has its own subfolder containing its required deployment files.

## Publications

ADERALDO, C. M., MENDONÇA, N. C., SCHMERL, B., GARLAN, D. "Kubow: An Architecture-Based Self-Adaptation Service for Cloud Native Applications." In: 13th European Conference on Software Architecture (ECSA), Tools, Demos and Posters Track, 2019, Paris, France. [[DOI]](https://doi.org/10.1145/3344948.3344963) [[PDF]](https://www.researchgate.net/publication/334279777_Kubow_An_Architecture-Based_Self-Adaptation_Service_for_Cloud_Native_Applications) **Recipient of the Best Demo Award!**

ADERALDO, C. M., MENDONÇA, N. C. "Kubow: Um Serviço de Autoadaptação Baseada em Arquitetura para Aplicações Nativas da Nuvem." In: Sessão de Ferramentas do X Congresso Brasileiro de Software: Teoria e Prática (CBSoft), 2019, Salvador, BA. [[PDF]](https://www.researchgate.net/publication/335627220_Kubow_Um_Servico_de_Autoadaptacao_Baseada_em_Arquitetura_para_Aplicacoes_Nativas_da_Nuvem)

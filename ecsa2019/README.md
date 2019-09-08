# Kubow Samples

This repository contains the files used to showcase Kubow for [ECSA 2019 Posters, Tools, Demos](https://ecsa2019.univ-lille.fr/tracks/posters-tools-demos).

To illustrate Kubow's use, we selected a simple web-based voting app from the set of sample applications distributed with [Docker](https://github.com/dockersamples/example-voting-app). This application was chosen because its architecture is simple enough to be used for illustrative purposes, and complex enough to be representative of typical Kubernetes applications deployed in real production environments.

If you are interested in read the paper, you can find the author's version [here](https://www.researchgate.net/publication/334279777_Kubow_An_Architecture-Based_Self-Adaptation_Service_for_Cloud_Native_Applications) and the bibtex citation below.

```
@inproceedings{Aderaldo:2019:KAS:3344948.3344963,
 author = {Aderaldo, Carlos M. and Mendon\c{c}a, Nabor C. and Schmerl, Bradley and Garlan, David},
 title = {Kubow: An Architecture-based Self-adaptation Service for Cloud Native Applications},
 booktitle = {Proceedings of the 13th European Conference on Software Architecture - Volume 2},
 series = {ECSA '19},
 year = {2019},
 isbn = {978-1-4503-7142-1},
 location = {Paris, France},
 url = {http://doi.acm.org/10.1145/3344948.3344963},
 doi = {10.1145/3344948.3344963},
 acmid = {3344963},
 publisher = {ACM},
}
```

We have separate folders for the samples of individual projects and versions. Each folder has the deployment files for project itself
and a folder for kubow customizations. There is a Kubow folder with Kubow`s default for all projects.

* [voting-app](https://github.com/dockersamples/example-voting-app): K8s deployment files for voting-app

* [custom-metrics-api](https://github.com/DirectXMan12/k8s-prometheus-adapter): An implementation of the Kubernetes [resource metrics](https://github.com/kubernetes/community/blob/master/contributors/design-proposals/instrumentation/resource-metrics-api.md) API and [custom metrics](https://github.com/kubernetes/community/blob/master/contributors/design-proposals/instrumentation/custom-metrics-api.md) API.

* [kube-state-metrics](https://github.com/kubernetes/kube-state-metrics): Service that listens to the Kubernetes API server and generates metrics about the state of the objects.

* [locust](https://github.com/locustio/locust): A distributed user load testing tool.

* [metrics-server](https://github.com/kubernetes-incubator/metrics-server): An implementation of the Kubernetes [Metrics Server](https://kubernetes.io/docs/tasks/debug-application-cluster/resource-metrics-pipeline/#metrics-server).

* [prometheus](https://github.com/prometheus/prometheus): A monitoring system and time series database.

## How to use it

First of all, you need to create a Kubernetes cluster, it can be done using the script `create_cluster.sh`. Before run that, you should install [kops](https://github.com/kubernetes/kops), a tool to get a production grade Kubernetes cluster up and running.

With kops installed, configure an AWS credentials ...

```sh
export NAME=<cluster-name>
export KOPS_STATE_STORE=s3://<cluster-name>
```

And finally you can run. Kops will ask you about the size and instance type of each instance group.

```sh
sh create_cluster.sh
```

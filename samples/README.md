# Kubow Samples

This repository contains example projects to showcase Kubow and how to use the features provided. Note that it does **not** contain code for any application, only the configuration files.

## Getting Started

Kubow has some dependencies that should be installed in the cluster before running it. In `tools` folder you can find these applications. I recommend you start the process from there and after came here again and choose one of the samples bellow and follow the instructions in its readme file. For each samples, there is a kubow folder inside.

We have separate folders for the samples of individual projects and versions. Each folder has the deployment files for project itself
and a folder for kubow specialization.

* [kube-znn](./kube-znn): K8s deployment files for kube-znn. It contains pod auto scaling and pod auto tuning (rollout versions) features.

* [kube-znn-istio](./kube-znn-istio): K8s deployment files for kube-znn, running on service mesh layer provided by Istio. 

* [voting-app](./voting-app): K8s deployment files for voting-app

* [podinfo](./podinfo): Configuration files for podinfo, to be used in development mode.

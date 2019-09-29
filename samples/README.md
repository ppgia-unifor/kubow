# Kubow Samples

This repository contains example projects to showcase Kubow and how to use the features provided. Note that it does **not** contain code for any application, only the configuration files.

We have separate folders for the samples of individual projects and versions. Each folder has the deployment files for project itself
and a folder for kubow specialization.

* [kube-znn](./kube-znn): K8s deployment files for kube-znn. It contains pod auto scaling and pod auto tuning (rollout versions) features.

* [kube-znn-istio](./kube-znn-istio): K8s deployment files for kube-znn, running on service mesh layer provided by Istio. 

* [voting-app](./voting-app): K8s deployment files for voting-app

* [podinfo](./podinfo): Configuration files for podinfo, to be used in development mode.

## Getting Started

**TL;DR**

The easiest way to run a sample is running this following command. It'll deploy the samples and the kubow folder inside it. 

```sh
sh deploy-samples.sh <folder-name> # eg: sh deploy-samples.sh voting-app
```

To destroy/undeploy tools, run this following command.

```sh
sh destroy-samples.sh <folder-name> # eg: sh destroy-samples.sh voting-app
```

**The longest way**

Kubow has some dependencies that should be installed in the cluster before running it. In `tools` folder you can find these applications. I recommend you start the process from there and after came here again and choose one of the samples above and follow the instructions in its readme file. For each samples, there is a kubow folder inside. 

After the deployment of any sample app, you should deploy Kubow. Apply the manifests files in your cluster running `kubectl apply -f ./kubow/` from a sample app's folder.

The command will create these resources. To make sure that kubow is running you can check the created pods.

```sh
serviceaccount/admin-user created
clusterrolebinding.rbac.authorization.k8s.io/admin-user created
configmap/effectors created
configmap/gauges created
configmap/kubernetes-acme-family created
deployment.apps/kubow created
configmap/log4j created
configmap/op-map created
configmap/probes created
configmap/rainbow-properties created
configmap/strategies created
configmap/tactics created
configmap/target-acme-model created
configmap/utilities created
```

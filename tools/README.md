
# Kubow tools

This directory contains the deployment files for several tools that support or complement Kubow's various features.

Required tools:

To install the required tools just run `kubectl apply -k monitoring` in this folder.

* [metrics-server](./metrics-server): an implementation of the Kubernetes [Metrics Server](https://kubernetes.io/docs/tasks/debug-application-cluster/resource-metrics-pipeline/#metrics-server).

* [prometheus](./prometheus): a monitoring system and time series database.

Optional tools:

* [kube-state-metrics](./kube-state-metrics): a service that periodically polls the Kubernetes API and generates metrics about the state of its objects.

Other tools:

* [custom-metrics-api](./custom-metrics-api): an implementation of the Kubernetes [resource metrics](https://github.com/kubernetes/community/blob/master/contributors/design-proposals/instrumentation/resource-metrics-api.md) API and [custom metrics](https://github.com/kubernetes/community/blob/master/contributors/design-proposals/instrumentation/custom-metrics-api.md) API.

* [locust](./locust): a distributed load generation and testing tool.

* [pumba](./pumba): chaos testing and network emulation tool for Docker containers.

* [istio](./istio): a service mesh tool.


## Deployment instructions

To deploy each of those tools in your cluster, follow the intructions provided in the tools' respective folder.

To deploy all the required tools, just run `sh deploy-tools.sh`

To destroy/undeploy the required tools, just run `sh destroy-tools.sh`


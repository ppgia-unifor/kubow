
# Kubow tools

This directory contains the deployment files for several tools that support or complement Kubow's various features.

Required tools:

* [custom-metrics-api](https://github.com/DirectXMan12/k8s-prometheus-adapter): an implementation of the Kubernetes [resource metrics](https://github.com/kubernetes/community/blob/master/contributors/design-proposals/instrumentation/resource-metrics-api.md) API and [custom metrics](https://github.com/kubernetes/community/blob/master/contributors/design-proposals/instrumentation/custom-metrics-api.md) API.

* [kube-state-metrics](https://github.com/kubernetes/kube-state-metrics): a service that preiodically polls the Kubernetes API and generates metrics about the state of its objects.

* [metrics-server](https://github.com/kubernetes-incubator/metrics-server): an implementation of the Kubernetes [Metrics Server](https://kubernetes.io/docs/tasks/debug-application-cluster/resource-metrics-pipeline/#metrics-server).

* [prometheus](https://github.com/prometheus/prometheus): a monitoring system and time series database.

Optional tools:

* [locust](https://github.com/locustio/locust): a distributed load generation and testing tool.

* [pumba](https://github.com/alexei-led/pumba): chaos testing and network emulation tool for Docker containers.

* [istio](https://github.com/istio/istio): a service mesh tool.

To download and deploy those tools in your cluster, follow the intructions provided in each tool's folder. 

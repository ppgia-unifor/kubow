# Kubow -- An Architecture-Based Self-Adaptation Service for Kubernetes Applications


The main goal of Kubow is to provide an architecture-based self-adaptation solution for containerized Kubernetes applications. The proposed solution is a Docker and Kubernetes customization of the Rainbow architecture-based self-adaptation framework, originally developed at Carnegie Mellon University. 

Kubow has been preliminarily evaluated both qualitatively and quantitatively. The qualitative results show that the proposed solution cab be easy to use and customize for different applications, and ease to extend to support different types of adaptation and different architectural models. The quantitative results, in turn, obtained in the Amazon cloud, show that the proposed solution is effective in dynamically adapting the behavior of a Kubernetes application, in response to change in its demand. 

### Getting started

#### Pre-requisites

- Install kubectl
- A running Kubernetes Cluster

#### Setting up namespaces
```sh
kubectl apply -f ./namespaces.yaml
```

#### Setting up the Monitoring Stack

Starting metrics server

```sh
kubectl apply -f ./metrics-server
kubectl apply -f ./custom-metrics-api
```
 
Starting Prometheus and Grafana

```sh
kubectl apply -f ./prometheus
kubectl apply -f ./grafana
```

Starting kube-state-metrics

```sh
kubectl apply -f ./kube-state-metrics
```

#### Setting up kube-znn 



#### 

...

### Target configurations

... 

#!/bin/bash

if [ ! -d = "./out" ]; then
  make certs
fi;

kubectl apply -f ./namespaces.yaml
kubectl apply -f ./prometheus
kubectl apply -f ./grafana
kubectl apply -f ./metrics-server
kubectl apply -f ./custom-metrics-api
kubectl apply -f ./kube-state-metrics
kubectl apply -f ./kube-znn
kubectl apply -f ./kubow/config
kubectl apply -f ./kubow/kubow-deployment.yaml

kubectl get all --all-namespaces


kubectl delete -f ./kubow/config
kubectl delete -f ./kubow/kubow-deployment.yaml
kubectl apply -f ./kubow/config
kubectl apply -f ./kubow/kubow-deployment.yaml

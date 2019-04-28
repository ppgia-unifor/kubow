#!/bin/bash

kubectl delete -f ./kubow/config
kubectl delete -f ./kubow/kubow-deployment.yaml
kubectl apply -f ./kubow/config
kubectl apply -f ./kubow/kubow-deployment.yaml

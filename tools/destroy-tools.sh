#! /bin/bash

kubectl delete -f ./prometheus
kubectl delete -f ./custom-metrics-api
kubectl delete -f ./metrics-server

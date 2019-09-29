#!/bin/bash

kubectl apply -f ./prometheus
kubectl apply -f ./custom-metrics-api
kubectl apply -f ./metrics-server


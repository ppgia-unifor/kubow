#! /bin/bash

kubectl delete -f ./$1
kubectl delete -f ./$1/kubow

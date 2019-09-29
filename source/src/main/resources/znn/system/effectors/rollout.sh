#!/bin/bash

set -e

DEPLOYMENT=$1
CONTAINER=$2
IMAGE=$3

kubectl set image deployment.v1.apps/$DEPLOYMENT $CONTAINER=$IMAGE --record

#!/bin/bash

set -e

VERSION=""
DEPLOYMENT="znn"
CONTAINER="znn"

if [[ "$1" = "1" ]]; then
  VERSION=text
elif [[ "$1" = "3" ]]; then
  VERSION=low
elif [[ "$1" = "5" ]]; then
  VERSION=high
fi

if [[ "$VERSION" = "" ]]; then
    echo "Invalid version '$1'. Allowed values '1' means text or '3' means low or '5' means high"
    exit 1;
fi

IMAGE=cmendes/znn:$VERSION

kubectl set image deployment.v1.apps/$DEPLOYMENT $CONTAINER=$IMAGE --record

#!/usr/bin/env bash

set -e
cd "$(dirname "$0")"

REGISTRY="${REGISTRY:-docker.io/youruser}"

# Build nested deps: infra's Bitnami charts first, then the umbrella's file:// deps.
helm dependency build ../infra
helm dependency build .

helm lint .
helm package . -d dist

if [ "$1" = "push" ]; then
  chart=$(ls -t dist/*.tgz | head -n 1)
  helm push "$chart" "oci://${REGISTRY}"
fi

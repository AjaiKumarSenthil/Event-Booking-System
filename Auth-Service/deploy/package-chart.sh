#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"

REGISTRY="${REGISTRY:-docker.io/youruser}"

helm lint .
helm package . -d dist

if [ "$1" = "push" ]; then
  chart=$(ls -t dist/*.tgz | head -n 1)
  helm push "$chart" "oci://${REGISTRY}"
fi

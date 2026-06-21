#!/usr/bin/env bash
# Deploy BookMyShow to the current Kubernetes cluster via the umbrella Helm chart.
# Usage: ./deploy/deploy.sh <path-to-auth-private.pem> [local]
#   (no flag) -> install the published chart from the repo: oci://$REGISTRY/bookmyshow
#   local     -> install directly from the local chart sources (builds deps first)
#
#   REGISTRY=docker.io/youruser   image registry/namespace (and OCI chart location)
#   NAMESPACE=bookmyshow          target namespace
#   VERSION=0.1.0                 chart version to pull (repo mode)
set -euo pipefail
cd "$(dirname "$0")"

JWT_KEY="${1:?Usage: deploy.sh <path-to-auth-private.pem> [local]}"
MODE="${2:-repo}"
NAMESPACE="${NAMESPACE:-bookmyshow}"
REGISTRY="${REGISTRY:-}"

ARGS=(--set-file auth.jwt.privateKey="$JWT_KEY")
[ -n "$REGISTRY" ] && ARGS+=(--set global.image.registry="${REGISTRY}/")

if [ "$MODE" = "local" ]; then
  helm dependency build infra
  helm dependency build bookmyshow
  CHART="bookmyshow"
else
  : "${REGISTRY:?Set REGISTRY=docker.io/youruser to install from the repo}"
  CHART="oci://${REGISTRY}/bookmyshow"
  ARGS+=(--version "${VERSION:-0.1.0}")
fi

helm upgrade --install bookmyshow "$CHART" \
  --namespace "$NAMESPACE" --create-namespace \
  "${ARGS[@]}"

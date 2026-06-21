# BookMyShow

A microservices-based **movie‑ticket booking platform** built with Spring Boot.
Browse movies, theaters and shows, authenticate, book seats and receive
notifications — all behind a single API gateway, containerized with Docker and
deployed to Kubernetes via Helm.

---

## Table of contents

- [Architecture](#architecture)
- [Services](#services)
- [Prerequisites](#prerequisites)
- [1. Build (Maven)](#1-build-maven)
- [2. Build Docker images](#2-build-docker-images)
- [3. Push Docker images](#3-push-docker-images)
- [4. Build Helm charts](#4-build-helm-charts)
- [5. Push Helm charts](#5-push-helm-charts)
- [6. Deploy](#6-deploy)
- [Configuration reference](#configuration-reference)

---

## Architecture

```
                        ┌────────────────────────┐
        client  ───────▶│   Ingress (nginx)      │  host: bookmyshow.local
                        └───────────┬────────────┘
                                    ▼
                        ┌────────────────────────┐
                        │      API-Gateway        │  :8080  (JWT validation, rate limiting)
                        └───┬───────┬───────┬─────┘
                  ┌─────────┘       │       └─────────┐
                  ▼                 ▼                 ▼
            ┌──────────┐     ┌────────────┐    ┌────────────┐
            │   Auth   │     │ Inventory  │    │  Booking   │
            │  :8083   │     │   :8081    │    │   :8082    │
            └──────────┘     └────────────┘    └─────┬──────┘
                                                     │ events
                                                     ▼
                                              ┌────────────┐
                                              │Notification│  :8084 (email)
                                              └────────────┘

   Backing infra (in-cluster Bitnami, or managed/external):
   PostgreSQL · Redis · Kafka
```

Every service verifies JWTs using a shared **`jwt-auth-starter`** library and the
Auth service's **public** key; only `Auth-Service` holds the **private** signing key.

## Services

| Service                | Port | Context path        | Backing stores            | Role |
|------------------------|------|---------------------|---------------------------|------|
| **API-Gateway**        | 8080 | `/`                 | Redis (rate limiting)     | Single entrypoint, routing, JWT auth |
| **Auth-Service**       | 8083 | `/auth/v1`          | PostgreSQL, Redis         | Login/register, JWT issuance |
| **Inventory-Service**  | 8081 | `/inventory/v1`     | PostgreSQL, Redis         | Cities, movies, theaters, shows |
| **Booking-Service**    | 8082 | `/booking/v1`       | PostgreSQL                | Seat booking |
| **Notification-Service**| 8084| `/notification/v1`  | PostgreSQL, Kafka, SMTP   | Email notifications (worker) |
| **jwt-auth-starter**   | —    | —                   | —                         | Shared JWT validation library |

## Prerequisites

- **JDK 21** and **Maven 3.9+**
- **Docker** (with `docker login` to your registry)
- **Helm 3.8+** and **kubectl**, pointed at a cluster (kind / minikube / cloud)
- An **nginx ingress controller** in the cluster (for external access)
- A registry namespace, e.g. **`docker.io/<your-user>`** (Docker Hub)

> Throughout this README, replace `docker.io/youruser` with your own Docker Hub
> namespace.

---

## 1. Build (Maven)

The shared `jwt-auth-starter` must be installed into your local Maven repo
**first**, because every service depends on it. Then build the services.

```bash
# 1. shared library (install to local ~/.m2)
mvn -f jwt-auth-starter/pom.xml clean install

# 2. each service (produces target/*.jar)
mvn -f API-Gateway/pom.xml          clean package
mvn -f Auth-Service/pom.xml         clean package
mvn -f Inventory-Service/pom.xml    clean package
mvn -f Booking-Service/pom.xml      clean package
mvn -f Notification-Service/pom.xml clean package
```

> **One-time setup for Auth-Service:** generate the RSA signing key
> (`auth-private.pem`) — it is intentionally **not** committed. See
> [`Auth-Service/README.md`](Auth-Service/README.md) for details.

## 2. Build Docker images

Each service builds its image with the **fabric8 `docker-maven-plugin`** using the
`Dockerfile` under `<Service>/deploy/`. Images are slim, runtime‑only (JRE + jar).

The image name is `${docker.registry}${docker.image}`:

- `docker.image` defaults to `bookmyshow-<service>:latest` (flat, Docker Hub‑safe)
- `docker.registry` is **empty by default** (builds a local image)

```bash
# Local image only (e.g. for kind/minikube): docker.io/library/bookmyshow-auth:latest
mvn -f Auth-Service/pom.xml package docker:build

# Tagged for your registry: docker.io/youruser/bookmyshow-auth:latest
mvn -f Auth-Service/pom.xml package docker:build \
  -Ddocker.registry=docker.io/youruser/
```

> Note the **trailing slash** on `docker.registry`.

## 3. Push Docker images

```bash
docker login

mvn -f Auth-Service/pom.xml         docker:push -Ddocker.registry=docker.io/youruser/
mvn -f API-Gateway/pom.xml          docker:push -Ddocker.registry=docker.io/youruser/
mvn -f Inventory-Service/pom.xml    docker:push -Ddocker.registry=docker.io/youruser/
mvn -f Booking-Service/pom.xml      docker:push -Ddocker.registry=docker.io/youruser/
mvn -f Notification-Service/pom.xml docker:push -Ddocker.registry=docker.io/youruser/
```

You can also do it in one shot per service:
`mvn -f <svc>/pom.xml package docker:build docker:push -Ddocker.registry=docker.io/youruser/`.

## 4. Build Helm charts

Each module owns a chart under `<Service>/deploy/`, plus a top‑level **umbrella**
chart at `deploy/bookmyshow/` that bundles all services + infra. Every chart has a
`package-chart.sh` that lints and packages into a local `dist/` folder.

```bash
# A single service chart
./Auth-Service/deploy/package-chart.sh

# The umbrella chart (resolves infra's Bitnami deps + all subcharts first)
./deploy/bookmyshow/package-chart.sh
```

## 5. Push Helm charts

Charts publish to the **same registry as the images** (Docker Hub supports OCI
charts). Pass `push` as the argument; set `REGISTRY` to your namespace.

```bash
docker login   # OCI charts use the same credentials

# Per service  -> docker.io/youruser/auth, .../inventory, ...
REGISTRY=docker.io/youruser ./Auth-Service/deploy/package-chart.sh push

# Umbrella     -> docker.io/youruser/bookmyshow
REGISTRY=docker.io/youruser ./deploy/bookmyshow/package-chart.sh push
```

## 6. Deploy

`deploy/deploy.sh` installs the whole platform via the umbrella chart. It needs the
Auth signing key (injected as a Secret) and your `REGISTRY` (so pods pull your
images). It runs `helm upgrade --install`, so it is **idempotent**.

```bash
# From the published repo chart (default) — run the pushes in steps 3 & 5 first
REGISTRY=docker.io/youruser ./deploy/deploy.sh ./auth-private.pem

# From local chart sources (dev/kind) — no push required, builds deps on the fly
REGISTRY=docker.io/youruser ./deploy/deploy.sh ./auth-private.pem local
```

Then map the ingress host and hit the gateway:

```bash
# add the ingress host to /etc/hosts (point at your ingress controller IP)
echo "127.0.0.1 bookmyshow.local" | sudo tee -a /etc/hosts

curl http://bookmyshow.local/auth/v1/actuator/health
```

Check rollout:

```bash
kubectl get pods -n bookmyshow
```

---

## Configuration reference

### Script / build knobs

| Variable / flag                | Used by                         | Meaning |
|--------------------------------|---------------------------------|---------|
| `-Ddocker.registry=`           | `mvn docker:build/push`         | Image registry + namespace, **trailing slash** (e.g. `docker.io/youruser/`) |
| `REGISTRY=`                    | `package-chart.sh`, `deploy.sh` | OCI registry + namespace, **no** trailing slash (e.g. `docker.io/youruser`) |
| `NAMESPACE=`                   | `deploy.sh`                     | Target Kubernetes namespace (default `bookmyshow`) |
| `VERSION=`                     | `deploy.sh` (repo mode)         | Umbrella chart version to pull (default `0.1.0`) |
| `local` (positional arg)       | `deploy.sh`                     | Install from local chart sources instead of the repo |
| `push` (positional arg)        | `package-chart.sh`              | Push the packaged chart to the OCI registry |

### Image registry is global

All service images resolve their registry from a single Helm **global**, so one
flag points the whole platform at your namespace:

```yaml
# deploy/bookmyshow/values.yaml
global:
  image:
    registry: ""   # e.g. "docker.io/youruser/" ; set via --set global.image.registry=...
```

### Switching from in-cluster infra to managed services

The default deploy runs PostgreSQL, Redis and Kafka in‑cluster via Bitnami
subcharts. To use managed/enterprise endpoints (e.g. AWS RDS, ElastiCache, MSK),
set `infra.enabled=false` and point each service's `database` / `redis` / `kafka`
block at the managed host — no image rebuild required.

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Minimal Spring Boot 3.3.4 (Java 17) "Hello World" app whose entire purpose is demonstrating a deployment to OpenShift. All app logic lives in two files: `HelloOpenshiftApplication.java` (main) and `HelloController.java` (the only controller).

## Commands

```bash
# Run locally
mvn spring-boot:run
curl http://localhost:8080/

# Build the jar (produces target/hello-openshift.jar, per <finalName> in pom.xml)
mvn -DskipTests package

# Docker build/run
docker build -t hello-openshift:latest .
docker run -p 8080:8080 hello-openshift:latest
```

There is no `src/test` directory yet, despite `spring-boot-starter-test` being declared — there are no tests to run.

## Architecture

- `src/main/java/com/example/hello/HelloController.java` exposes:
  - `GET /` → plain-text `app.message`
  - `GET /api/hello` → JSON `{ "message": ..., "hostname": ... }`
  - Actuator adds `GET /actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness` (enabled via `management.*` properties for OpenShift probes).
- `app.message` (in `application.properties`) reads from env var `APP_MESSAGE`, falling back to a hardcoded default. In OpenShift, `APP_MESSAGE` is injected from a ConfigMap — this is the only piece of runtime config in the whole app.
- `Dockerfile` is a two-stage UBI9 build: stage 1 compiles with `registry.access.redhat.com/ubi9/openjdk-17` (`mvn package`), stage 2 copies `target/hello-openshift.jar` into a `ubi9/openjdk-17-runtime` image as `/deployments/app.jar`, runs as `USER 185` (OpenShift's arbitrary-UID/root-group convention).
- `openshift/openshift.yaml` is a single multi-document manifest defining the full deployment chain, in dependency order: **ConfigMap → ImageStream → BuildConfig (Git strategy, Docker build of this repo) → Deployment → Service → Route**.
  - The Deployment carries an `image.openshift.io/triggers` annotation so it auto-redeploys whenever the BuildConfig pushes a new `ImageStreamTag`.
  - The Deployment's container image reference contains a placeholder namespace segment (`CHANGE_ME_NAMESPACE`) that must be set to the target OpenShift project before applying.
  - **Naming mismatch to be aware of**: the Maven artifact/jar and Docker image are named `hello-openshift` (see `pom.xml` `finalName`), but every resource in `openshift/openshift.yaml` (ConfigMap, ImageStream, BuildConfig, Deployment, Service, Route) is named `springboot-openshift-2`. These are two different naming schemes for the same app — don't assume they'll match when cross-referencing.

## Deploying to OpenShift

Two approaches (see `README.md` for full command sequences):
- **Binary build** (`oc start-build --from-dir=.`): builds directly from local sources, no Git dependency.
- **Git-based BuildConfig** (what's currently configured in `openshift/openshift.yaml`): OpenShift pulls this repo directly via the `source.git.uri` field and rebuilds on `oc start-build <name> --follow`.

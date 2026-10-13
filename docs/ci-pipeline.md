# Jenkins CI Pipeline

The Jenkins pipeline validates the Employee Leave Request System and builds
Docker images for the backend and frontend.

## Pipeline Stages

1. Checkout source code from GitHub.
2. Check Java, Maven, Node.js, npm, and Docker versions.
3. Run Spring Boot backend tests.
4. Install Angular frontend dependencies.
5. Build the Angular frontend for production.
6. Build the Spring Boot backend Docker image.
7. Build the Angular frontend Docker image.

## Automatic SCM Polling

Jenkins is configured to poll the GitHub repository every five minutes
using the schedule `H/5 * * * *`.

When Jenkins detects changes in the configured branch, it triggers a build.
If no changes are detected, it normally does not trigger a new build.

## Purpose

- Detect backend test failures automatically.
- Detect Angular production build errors.
- Verify that backend and frontend Docker images can be built.
- Identify integration problems before changes are accepted into the main branch.

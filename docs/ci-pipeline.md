# Jenkins CI Pipeline

The Jenkins pipeline validates the Employee Leave Request System.

Pipeline stages:
- Checkout source code from GitHub.
- Check Java, Maven, Node.js, and npm versions.
- Run Spring Boot backend tests.
- Install Angular dependencies.
- Build the Angular frontend for production.

Purpose: Detect backend test failures and frontend build errors automatically.

# Stage 1: Build the Spring Boot application
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

# Copy Maven configuration and Java source code
COPY pom.xml .
COPY src ./src

# Build the executable Spring Boot JAR
RUN mvn -B -DskipTests package

# Select the executable JAR
RUN set -eux; \
    JAR_FILE="$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*.original' | head -n 1)"; \
    test -n "$JAR_FILE"; \
    cp "$JAR_FILE" /workspace/app.jar


# Stage 2: Run the application using Java 17
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the executable JAR from the build stage
COPY --from=build --chown=10001:10001 /workspace/app.jar /app/app.jar

# Run as a non-root user
USER 10001:10001

# Spring Boot port
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
# Q-Insight: Dockerfile for the Spring Boot Application.
# This file defines the steps to build a container image for the application.
# It uses a multi-stage build for efficiency and a smaller final image.

# --- Build Stage ---
# Use a specific JDK version for building the application.
FROM gradle:8.5.0-jdk21 AS build

# Set the working directory inside the container.
WORKDIR /home/gradle/src

# Copy the build file and download dependencies.
# This layer is cached by Docker if the build file doesn't change.
COPY build.gradle .
RUN gradle build --no-daemon > /dev/null 2>&1 || true

# Copy the rest of the source code.
COPY . .

# Build the application, skipping tests for a faster build.
# The executable JAR will be created.
RUN gradle build -x test --no-daemon

# --- Run Stage ---
# Use a minimal base image with just the Java Runtime Environment.
FROM eclipse-temurin:21-jre-jammy

# Set the working directory for the application.
WORKDIR /app

# Copy the executable JAR from the build stage.
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

# Expose the port the application runs on.
EXPOSE 8080

# The command to run the application when the container starts.
ENTRYPOINT ["java", "-jar", "app.jar"]

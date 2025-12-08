# Q-Insight: Dockerfile for the Spring Boot Application.
# This file defines the steps to build a container image for the application.
# It uses a multi-stage build for efficiency and a smaller, more secure final image.

# --- Build Stage ---
# Use a specific Gradle version with a specific JDK for building the application.
# Using a specific image tag ensures reproducible builds.
FROM gradle:8.7-jdk21 AS build

# Set the working directory inside the container.
WORKDIR /home/gradle/src

# Q-Insight: Copy only the necessary files to resolve dependencies first.
# This leverages Docker's layer caching. If these files don't change,
# the dependency download step is skipped, speeding up builds.
COPY build.gradle settings.gradle* ./
COPY gradle ./gradle

# Q-Insight: Download dependencies without executing any other tasks.
# This creates a separate Docker layer with just the dependencies.
RUN gradle dependencies --no-daemon

# Q-Insight: Copy the rest of the application's source code.
COPY src ./src

# Q-Insight: Run the build. This will compile the code, run tests, and create the executable JAR.
# The tests are run here to act as a gatekeeper for the image build.
RUN gradle build --no-daemon


# --- Run Stage ---
# Use a minimal, non-root base image with just the Java Runtime Environment.
# The 'jammy' tag is based on Ubuntu 22.04 LTS.
FROM eclipse-temurin:21-jre-jammy

# Define arguments for user and group IDs to avoid hardcoding.
ARG UID=1001
ARG GID=1001

# Create a group and user with the specified IDs.
RUN groupadd --gid ${GID} spring && useradd --uid ${UID} --gid ${GID} -m spring

# Set the user for the rest of the build and for running the application.
USER spring

# Set the working directory for the application.
WORKDIR /app

# Q-Insight: Copy the executable JAR from the build stage.
# The JAR is renamed to a consistent 'app.jar'.
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

# Expose the port the application runs on. This should match the server.port in application.yml.
EXPOSE 8080

# Q-Insight: The command to run the application when the container starts.
# exec form is used to ensure proper signal handling.
ENTRYPOINT ["java", "-jar", "app.jar"]

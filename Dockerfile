# Stage 1: Build stage
FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# Copy Gradle files
COPY build.gradle .
COPY gradle gradle
COPY gradlew .
COPY gradlew.bat .

# Download dependencies
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src src

# Build application
RUN gradle clean bootJar -x test --no-daemon

# Stage 2: Runtime stage
FROM openjdk:21-jdk-slim
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Change ownership
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]

# ==============================================================================
# Multi-Stage Dockerfile for RIT GPA Calculator Spring Boot Backend
# Production-ready for Render, AWS, GCP, and local Docker Engine
# ==============================================================================

# --- Stage 1: Build Stage ---
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Cache Maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source code and compile production JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Stage 2: Runtime Stage ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Install font libraries for Apache PDFBox coordinate text processing
RUN apk add --no-cache fontconfig ttf-dejavu

# Non-root user for security best practices
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy executable Spring Boot JAR from builder
COPY --from=builder --chown=appuser:appgroup /workspace/target/rit-gpa-calculator-*.jar app.jar

# Render assigns dynamic PORT environment variable (default fallback: 8080)
ENV PORT=8080
EXPOSE 8080

# Production container memory flags and headless graphics mode
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.awt.headless=true", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]


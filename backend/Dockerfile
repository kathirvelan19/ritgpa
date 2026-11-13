# ==============================================================================
# Multi-Stage Dockerfile for RIT GPA Calculator Spring Boot Backend
# Optimized for Render, Railway, and Container Cloud Environments
# ==============================================================================

# --- Stage 1: Build Stage ---
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Allocate specific heap and 2MB thread stack to prevent native memory exhaustion & SIGSEGV on Render
ENV MAVEN_OPTS="-Xms256m -Xmx512m -Xss2m -XX:+UseContainerSupport"

# Copy Maven descriptor and source code
COPY pom.xml .
COPY src ./src

# Compile and package production executable JAR
RUN mvn clean package -DskipTests -B

# --- Stage 2: Runtime Stage (Ubuntu Jammy glibc for Firebase Admin/Netty native JNI compatibility) ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Non-root user for security best practices
RUN groupadd -r appgroup && useradd -r -g appgroup appuser
USER appuser

# Copy built JAR from builder stage
COPY --from=builder --chown=appuser:appgroup /workspace/target/rit-gpa-calculator-*.jar app.jar

# Render dynamic port support (defaults to 8080)
ENV PORT=8080
EXPOSE 8080

# Production container JVM flags
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.awt.headless=true", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]

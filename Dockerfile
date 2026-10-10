# Base image: Ultra-lightweight Eclipse Temurin JRE 21 on Alpine Linux (< 180MB)
FROM eclipse-temurin:21-jre-alpine

# Set application working directory
WORKDIR /app

# Create a non-root system group and user for container security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy pre-packaged jar artifact
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Ensure proper ownership of the app directory
RUN chown -R appuser:appgroup /app

# Switch to the non-root user
USER appuser

# Configure JVM memory limits for 1GB RAM VPS server
ENV JAVA_OPTS="-Xms128m -Xmx350m -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

# Expose HTTP port
EXPOSE 8080

# Execute Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]


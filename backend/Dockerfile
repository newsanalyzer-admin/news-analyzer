# NewsAnalyzer Backend - Docker Image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy JAR file
COPY target/newsanalyzer-backend-*.jar app.jar

# Download OpenTelemetry Java agent (pinned version for reproducibility)
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.12.0/opentelemetry-javaagent.jar /opt/opentelemetry-javaagent.jar
RUN chmod 644 /opt/opentelemetry-javaagent.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application with OTel Java agent
ENTRYPOINT ["java", "-javaagent:/opt/opentelemetry-javaagent.jar", "-jar", "app.jar"]

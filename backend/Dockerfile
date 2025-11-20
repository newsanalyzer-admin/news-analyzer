# NewsAnalyzer Backend - Docker Image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy JAR file
COPY target/newsanalyzer-backend-*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

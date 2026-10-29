# Multi-stage Dockerfile for SentinelDesk AI Backend
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build application
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create unprivileged user
RUN groupadd -r sentinel && useradd -r -g sentinel sentinel

COPY --from=build /app/target/sentineldesk-backend-*.jar app.jar
RUN chown -R sentinel:sentinel /app

USER sentinel
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]

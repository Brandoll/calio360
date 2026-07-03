# ==========================================
# CALIO360 — Identity Service Dockerfile
# Multi-stage build for production
# ==========================================

# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN apk add --no-cache maven && mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Production
FROM eclipse-temurin:21-jre-alpine AS production
WORKDIR /app

RUN addgroup -S calio && adduser -S calio -G calio
RUN apk add --no-cache curl

COPY --from=build /app/target/*.jar app.jar
RUN chown -R calio:calio /app
USER calio

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/api/v1/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]

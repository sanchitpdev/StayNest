# ── Stage 1: Build ─────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Stage 2: Run ───────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create non-root user and logs directory BEFORE switching user
RUN addgroup --system staynest \
    && adduser --system --group staynest \
    && mkdir -p /app/logs \
    && chown -R staynest:staynest /app

# Now switch to non-root user
USER staynest

# Copy the jar from build stage
COPY --from=builder --chown=staynest:staynest /app/target/staynest-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=docker", \
  "-jar", "app.jar"]

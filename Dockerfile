# ──────────────────────────────────────────────────────────────────────────────
# Stage 1 — Build
# ──────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

# Install Maven
RUN apk add --no-cache maven

WORKDIR /app

# Cache dependencies first (layer-friendly)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Build the project
COPY src ./src
RUN mvn package -DskipTests -q

# ──────────────────────────────────────────────────────────────────────────────
# Stage 2 — Runtime
# ──────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="GOOD GOV IT <contact@goodgovit.ma>"
LABEL description="SMART RH 4.0 — Intelligent HR Information System"

WORKDIR /app

# Non-root user for security
RUN addgroup -S smartrh && adduser -S smartrh -G smartrh

COPY --from=builder /app/target/smart-rh-*.jar app.jar

# Upload volume (payroll PDFs, face images)
RUN mkdir -p /app/uploads && chown -R smartrh:smartrh /app

USER smartrh

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]

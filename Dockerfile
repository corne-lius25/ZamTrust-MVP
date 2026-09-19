# ---------- Stage 1: Build ----------
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

# Copy POM first for dependency caching
COPY pom.xml .

# Download dependencies (this layer is cached unless pom.xml changes)
RUN mvn -B -q dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Create a non-root user (security best practice)
RUN addgroup -S zamtrust && adduser -S zamtrust -G zamtrust

# Copy the built jar
COPY --from=build /workspace/target/*.jar app.jar

# Create storage directories owned by our user
RUN mkdir -p /app/storage/documents /app/storage/keys \
    && chown -R zamtrust:zamtrust /app

USER zamtrust

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

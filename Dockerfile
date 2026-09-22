# ---------- Stage 1: Build ----------
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Copy the built jar
COPY --from=build /workspace/target/*.jar app.jar

# Create storage paths. Both /app/storage (default local) and /data
# (Railway volume) are created. /data is where the Railway volume
# will be mounted — it must exist with permissive permissions so
# the runtime user can write to it after the mount.
RUN mkdir -p /app/storage/documents /app/storage/keys /app/storage/signatures /data/documents /data/keys /data/signatures \
    && chmod -R 777 /data \
    && mkdir -p /app/storage && chmod 777 /app/storage

# Run as root so mounted volumes are writable. Container is isolated;
# the app itself has its own auth+security layers.
USER root

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

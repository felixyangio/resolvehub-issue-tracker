# ── Stage 1: Build ───────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Cache Maven dependencies separately from source code
# (only re-downloads when pom.xml changes)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B -q

COPY src/ src/
RUN ./mvnw package -DskipTests -B -q

# ── Stage 2: Runtime ─────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Non-root user for container security best practice
RUN groupadd -r app && useradd -r -g app app

# Upload directory for file attachments
RUN mkdir -p /app/uploads && chown app:app /app/uploads

COPY --from=build /app/target/*.jar app.jar
RUN chown app:app app.jar

USER app

EXPOSE 8080

# /dev/./urandom avoids JVM blocking on entropy in containers
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]

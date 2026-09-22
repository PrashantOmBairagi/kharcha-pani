# ─────────────────────────────────────────────────────────────────────────────
# Stage 1: Build
#   Uses full JDK + Maven wrapper to compile and package the Spring Boot JAR.
#   Dependencies are cached in a separate layer so re-builds are fast when
#   only source files change (pom.xml unchanged = no re-download).
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper files first — dependency layer is cached here
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Fix line endings (Windows CRLF → LF) so mvnw is executable on Linux
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Download all dependencies offline (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Now copy source and build the fat JAR — tests skipped for CI speed
COPY src src
RUN ./mvnw package -DskipTests -B

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2: Run
#   Uses minimal JRE-only image (~180 MB vs ~500 MB for JDK).
#   Only the final JAR is copied — no Maven, no source, no build tools.
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Pull only the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Railway automatically injects PORT env var; Spring Boot listens on 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

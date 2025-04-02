# Stage 1: Build the JAR (using Gradle)
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace
COPY . .
RUN ./gradlew clean build --no-daemon

# Stage 2: Create lightweight runtime image
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=builder /workspace/build/libs/tesla-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
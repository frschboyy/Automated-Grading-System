# Stage 1: Build the JAR
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

# First copy only the gradle files to cache dependencies
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# Make gradlew executable
RUN chmod +x gradlew

# Build the application
RUN ./gradlew clean build --no-daemon

# Stage 2: Create runtime image
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=builder /workspace/build/libs/tesla-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
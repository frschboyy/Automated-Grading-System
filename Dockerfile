# Stage 1: Build the JAR
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace
COPY . .
RUN chmod +x gradlew && ./gradlew clean build -x test -x apiTest --no-daemon

# Stage 2: Create runtime image
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=builder /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
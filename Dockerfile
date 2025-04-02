FROM openjdk:21-jdk
ARG JAR_FILE=build/libs/tesla-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} tesla-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/tesla-0.0.1-SNAPSHOT.jar"]
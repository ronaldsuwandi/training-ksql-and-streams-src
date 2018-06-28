FROM gradle:4.8.0-jdk8-alpine AS builder
RUN mkdir -p /home/gradle/project/.gradle 
WORKDIR /home/gradle/project
COPY . .
RUN gradle build

FROM openjdk:8-jre
COPY --from=builder /home/gradle/project/build/libs/streams-app-0.1.0.jar /app.jar
CMD ["java", "-jar", "/app.jar"]

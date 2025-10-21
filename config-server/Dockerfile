FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app
COPY config-server /app/config-server
WORKDIR /app/config-server
RUN mvn clean install -DskipTests


FROM openjdk:21
WORKDIR /app
COPY --from=builder /app/config-server/target/*.jar ./app.jar
ENTRYPOINT java -jar app.jar
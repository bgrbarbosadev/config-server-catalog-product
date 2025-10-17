FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app/catalogo-produtos
COPY . .

RUN mvn clean install


FROM openjdk:21
WORKDIR /app
COPY --from=builder /app/catalogo-produtos/target/*.jar ./app.jar
ENTRYPOINT java -jar app.jar
# syntax=docker/dockerfile:1.7

FROM maven:3.8.8-eclipse-temurin-8 AS build

WORKDIR /app

COPY pom.xml .
RUN --mount=type=cache,id=library-lending-m2,target=/root/.m2 mvn -B dependency:go-offline

COPY src ./src

RUN --mount=type=cache,id=library-lending-m2,target=/root/.m2 mvn -B package -DskipTests

FROM eclipse-temurin:8-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]

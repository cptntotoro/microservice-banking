FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY blocker-service/pom.xml ./blocker-service/
COPY blocker-service/src ./blocker-service/src/

# Устанавливаем родительский POM в локальный репозиторий Maven
RUN mvn install -N -DskipTests

RUN mvn -f blocker-service/pom.xml clean package spring-boot:repackage -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/blocker-service/target/blocker-service.jar app.jar

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
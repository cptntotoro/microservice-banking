FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY notification-service/pom.xml ./notification-service/
COPY notification-service/src ./notification-service/src/

# Устанавливаем родительский POM в локальный репозиторий Maven
RUN mvn install -N -DskipTests

RUN mvn -f notification-service/pom.xml clean package spring-boot:repackage -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/notification-service/target/notification-service.jar app.jar

EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
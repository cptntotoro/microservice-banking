FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY cash-service/pom.xml ./cash-service/
COPY cash-service/src ./cash-service/src/

# Устанавливаем родительский POM в локальный репозиторий Maven
RUN mvn install -N -DskipTests

RUN mvn -f cash-service/pom.xml clean package spring-boot:repackage -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/cash-service/target/cash-service.jar app.jar

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
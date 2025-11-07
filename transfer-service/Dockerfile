FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY transfer-service/pom.xml ./transfer-service/
COPY transfer-service/src ./transfer-service/src/

# Устанавливаем родительский POM в локальный репозиторий Maven
RUN mvn install -N -DskipTests

RUN mvn -f transfer-service/pom.xml clean package spring-boot:repackage -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/transfer-service/target/transfer-service.jar app.jar

EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
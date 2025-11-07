FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY exchange-service/pom.xml ./exchange-service/
COPY exchange-service/src ./exchange-service/src/

# Устанавливаем родительский POM в локальный репозиторий Maven
RUN mvn install -N -DskipTests

RUN mvn -f exchange-service/pom.xml clean package spring-boot:repackage -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/exchange-service/target/exchange-service.jar app.jar

EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
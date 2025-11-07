FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY exchange-generator-service/pom.xml ./exchange-generator-service/
COPY exchange-generator-service/src ./exchange-generator-service/src/

# Устанавливаем родительский POM в локальный репозиторий Maven
RUN mvn install -N -DskipTests

RUN mvn -f exchange-generator-service/pom.xml clean package spring-boot:repackage -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/exchange-generator-service/target/exchange-generator-service.jar app.jar

EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
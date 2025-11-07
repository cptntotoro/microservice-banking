FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY serv-auth-service/pom.xml ./serv-auth-service/
COPY serv-auth-service/src ./serv-auth-service/src/

# Устанавливаем родительский POM в локальный репозиторий Maven
RUN mvn install -N -DskipTests

RUN mvn -f serv-auth-service/pom.xml clean package spring-boot:repackage -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/serv-auth-service/target/serv-auth-service.jar app.jar

EXPOSE 9000
ENTRYPOINT ["java", "-jar", "app.jar"]
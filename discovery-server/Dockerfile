FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY discovery-server/pom.xml ./discovery-server/
COPY discovery-server/src ./discovery-server/src/

# Устанавливаем родительский POM в локальный репозиторий Maven
RUN mvn install -N -DskipTests

RUN mvn -f discovery-server/pom.xml clean package spring-boot:repackage -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/discovery-server/target/discovery-server.jar app.jar

EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
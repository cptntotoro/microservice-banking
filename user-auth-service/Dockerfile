FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY user-auth-service/pom.xml ./user-auth-service/
COPY user-auth-service/src ./user-auth-service/src/

# Устанавливаем родительский POM в локальный репозиторий Maven
RUN mvn install -N -DskipTests

RUN mvn -f user-auth-service/pom.xml clean package spring-boot:repackage -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/user-auth-service/target/user-auth-service.jar app.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
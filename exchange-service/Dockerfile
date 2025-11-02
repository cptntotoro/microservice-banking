FROM openjdk:21-jdk-slim
COPY exchange-service/target/exchange-service.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
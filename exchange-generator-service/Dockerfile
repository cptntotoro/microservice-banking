FROM openjdk:21-jdk-slim
COPY exchange-generator-service/target/exchange-generator-service.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
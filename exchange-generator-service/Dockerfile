FROM openjdk:21-jdk-slim
COPY target/exchange-generator-service.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
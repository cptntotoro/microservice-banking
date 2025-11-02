FROM openjdk:21-jdk-slim
COPY transfer-service/target/transfer-service.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
FROM openjdk:21-jdk-slim
#WORKDIR /app
COPY target/exchange-generator-service.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
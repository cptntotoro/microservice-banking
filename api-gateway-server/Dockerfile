FROM openjdk:21-jdk-slim
COPY target/api-gateway-server.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.profiles.active=docker"]
FROM openjdk:21-jdk-slim
COPY target/discovery-server.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.profiles.active=docker"]
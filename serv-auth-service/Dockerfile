FROM openjdk:21-jdk-slim
#WORKDIR /app
COPY target/serv-auth-service.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
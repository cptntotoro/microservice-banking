FROM openjdk:21-jdk-slim
COPY serv-auth-service/target/serv-auth-service.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
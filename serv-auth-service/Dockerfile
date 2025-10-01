FROM openjdk:21-jdk-slim
#WORKDIR /app
COPY target/serv-auth-service.jar app.jar
EXPOSE 9000
ENTRYPOINT ["java", "-jar", "app.jar"]
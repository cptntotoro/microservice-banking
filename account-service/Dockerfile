FROM openjdk:21-jdk-slim

COPY account-service/target/account-service.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

FROM openjdk:21-jdk-slim

# Установка curl для диагностики
# RUN apk add --no-cache curl

COPY target/notification-service.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

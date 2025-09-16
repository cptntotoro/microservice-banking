FROM eclipse-temurin:21-jre-alpine

# Установка curl для диагностики
RUN apk add --no-cache curl

COPY target/cash-service.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

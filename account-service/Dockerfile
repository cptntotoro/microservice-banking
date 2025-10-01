FROM eclipse-temurin:21-jre-alpine

#WORKDIR /app
#COPY account-service/src ./account-service/src/

COPY target/account-service.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

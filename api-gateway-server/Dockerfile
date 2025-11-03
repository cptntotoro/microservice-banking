FROM eclipse-temurin:21
COPY target/api-gateway-server.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.profiles.active=docker"]
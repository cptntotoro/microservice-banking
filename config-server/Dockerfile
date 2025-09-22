FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY target/config-server-*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
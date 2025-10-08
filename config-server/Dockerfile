FROM openjdk:21-jdk-slim
#VOLUME /tmp
COPY target/config-server-*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
FROM bellsoft/liberica-openjdk-alpine:17

VOLUME /shared

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar","--spring.config.location=/share/application.yaml"]
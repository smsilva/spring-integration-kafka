FROM azul/zulu-openjdk-alpine:23-latest

RUN addgroup -S spring && \
    adduser -S spring -G spring

USER spring:spring

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-Djava.security.manager=allow", "-jar", "/app.jar"]

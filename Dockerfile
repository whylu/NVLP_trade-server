FROM openjdk:15-jdk-alpine
ARG JAR_FILE=target/trade-server.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]


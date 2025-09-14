FROM eclipse-temurin:21-jre
WORKDIR /app
ARG JAR_FILE=emailnotification-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
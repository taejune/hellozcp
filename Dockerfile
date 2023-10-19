FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD target/hellozcp-0.0.3-SNAPSHOT.jar app.jar
ENV JAVA_OPTS=""
ENTRYPOINT exec java $JAVA_OPTS -jar /app.jar
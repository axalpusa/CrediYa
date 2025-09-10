FROM eclipse-temurin:17-jdk-alpine

VOLUME /tmp

COPY applications/app-service/build/libs/*.jar Orders.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 9091

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar Orders.jar"]
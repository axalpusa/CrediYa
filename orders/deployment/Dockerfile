
# ============================
# Imagen base ligera con JDK 17
# ============================
FROM eclipse-temurin:17-jdk-alpine

# Directorio temporal opcional usado por Spring Boot
VOLUME /tmp

# Copiamos el JAR generado localmente al contenedor
COPY applications/app-service/build/libs/*.jar Orders.jar

# Configuración de JVM
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"
# Puerto expuesto
EXPOSE 8091

# Ejecutar la app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar Orders.jar"]
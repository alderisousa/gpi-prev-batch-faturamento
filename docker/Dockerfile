FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app
COPY gpi-prev-batch-faturamento-app.jar /app/app.jar

ENTRYPOINT ["java", "-Xmx1g", "-Xms512m", "-jar", "/app/app.jar"]
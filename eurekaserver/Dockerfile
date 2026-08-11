FROM eclipse-temurin:25-jre

USER root

RUN apt-get update \
    && apt-get install -y curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/eurekaserver-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8071

ENTRYPOINT ["java", "-jar", "app.jar"]
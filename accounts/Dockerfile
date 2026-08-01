FROM eclipse-temurin:25-jdk

LABEL "org.opencontainers.image.authors"="sammo.com"

COPY target/accounts-0.0.1-SNAPSHOT.jar accounts-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "accounts-0.0.1-SNAPSHOT.jar"]

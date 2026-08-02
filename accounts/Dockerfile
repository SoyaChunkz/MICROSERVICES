FROM eclipse-temurin:25-jdk

LABEL "org.opencontainers.image.authors"="sammo.com"

COPY target/accounts-0.0.1-SNAPSHOT.jar accounts-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "accounts-0.0.1-SNAPSHOT.jar"]

# ============================================================
# Steps to build and run this Spring Boot application using Docker
#
# 1. Build the Spring Boot JAR
#    mvn clean install
#
# 2. Build the Docker image
#    docker build -t soyachunkz/accounts:latest .
#    -t is tag
#
# 3. Verify the image
#    docker images
#
# 4. (Optional) Inspect the image
#    docker inspect soyachunkz/accounts:latest
#
# 5. Run a container from the image
#    docker run -d -p 8080:8080 --name accounts soyachunkz/accounts:latest
#
# 6. Verify the running container
#    docker ps
#
# 7. View application logs
#    docker logs accounts
#
# 8. Stop the container
#    docker stop accounts
#
# 9. Remove the container
#    docker rm accounts
#
# 10. Remove the image (after deleting all containers using it)
#     docker rmi soyachunkz/accounts:latest
# ============================================================

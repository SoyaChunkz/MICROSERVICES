# SammoBank Project Setup

This guide explains how to run SammoBank locally using IntelliJ IDEA or Docker Compose.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Option 1: Run Locally with IntelliJ IDEA](#option-1-run-locally-with-intellij-idea)
- [Option 2: Run with Docker Compose](#option-2-run-with-docker-compose)
- [Service Endpoints](#service-endpoints)
- [Troubleshooting](#troubleshooting)

## Prerequisites

Install the following tools:

| Tool | Purpose |
|---|---|
| Git | Clone the repository |
| IntelliJ IDEA | Develop and run Spring Boot services |
| Java 25 JDK | Build and run the applications locally |
| Docker Desktop | Run infrastructure and application containers |
| Docker Compose v2 | Manage the local container environment |

Verify your installations:

```powershell
git --version
java -version
docker --version
docker compose version
```

## Option 1: Run Locally with IntelliJ IDEA

In this setup, the Spring Boot applications run directly from IntelliJ IDEA, while their infrastructure dependencies run in Docker containers.

### Step 1: Clone the Repository

```powershell
git clone https://github.com/SoyaChunkz/MICROSERVICES.git
cd MICROSERVICES
```

Open the project in IntelliJ IDEA.

Each application is an independent Maven project with its own `pom.xml` and Maven Wrapper.

### Step 2: Start the MySQL Databases

The root `docker-compose.yml` defines the three MySQL containers required by the business services:

- Accounts database
- Loans database
- Cards database

From the repository root, run:

```powershell
docker compose up -d accountsdb loansdb cardsdb
```

This starts only the three database services defined in the root Compose file.

The database configuration is:

| Database | Database name | Host port |
|---|---|---:|
| Accounts | `accountsdb` | `13306` |
| Loans | `loansdb` | `23306` |
| Cards | `cardsdb` | `33306` |

The root Compose file also defines RabbitMQ, but it is not required for the current application setup. Kafka is used for messaging.

### Step 3: Start Keycloak

Keycloak runs as a separate Docker container for local development.

```powershell
docker run -d `
  --name keycloak `
  -p 127.0.0.1:8180:8080 `
  -e KC_BOOTSTRAP_ADMIN_USERNAME=admin `
  -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin `
  quay.io/keycloak/keycloak:26.7.2 start-dev
```

Keycloak will be accessible at:

- **URL:** http://localhost:8180
- **Username:** `admin`
- **Password:** `admin`

These credentials are for local development only. Do not use them in production.

If a container named `keycloak` already exists, start it with:

```powershell
docker start keycloak
```

### Step 4: Configure the Applications

SammoBank uses Spring Cloud Config Server to retrieve configuration from an external Git repository.

Configuration repository:

[MICROSERVICES-CONFIG](https://github.com/SoyaChunkz/MICROSERVICES-CONFIG)

Ensure the repository is accessible and that any required credentials and environment-specific settings are configured.

Review the application's configuration before starting the services, especially database connection details, Keycloak settings, Kafka connectivity, and active Spring profiles.

### Step 5: Run the Spring Boot Services

Open each service in IntelliJ IDEA and run its Spring Boot application.

Follow this startup order:

1. **Config Server** — provides centralized application configuration.
2. **Eureka Server** — provides service discovery.
3. **Supporting infrastructure** — ensure Kafka is running if it is not already provided by another local setup.
4. **Business services** — start Accounts, Loans, Cards, and Message as required.
5. **Gateway Server** — start the API Gateway after its required dependencies are available.

Wait for each service to finish starting before proceeding to the next stage. Check the IntelliJ console for startup errors.

> The exact order between business services and the Gateway may depend on the configured startup dependencies. The Gateway does not necessarily need every business service to be available before it starts.

### Step 6: Verify the Application

Check the application logs in IntelliJ IDEA and confirm that:

- Config Server starts successfully and retrieves the expected configuration.
- Eureka Server starts and services register with the registry.
- The business services connect to their respective databases.
- Kafka connectivity works for components that use messaging.
- Keycloak is reachable and the application's security configuration is correct.
- The Gateway starts and routes requests to the configured services.

Use the API requests in `Microservices.postman_collection.json` to test the available endpoints.

## Option 2: Run with Docker Compose

The `docker-compose/default/` directory contains the validated local Docker Compose setup for running the application environment in containers.

### Step 1: Prepare the Container Images

The Compose configuration must reference images that are available locally or can be pulled from a registry.

If you have not built the required images, build them using the project's Maven Wrapper and Jib configuration. For example, from the Accounts project directory:

```powershell
cd accounts
.\mvnw.cmd -DskipTests compile jib:dockerBuild -Dimage=soyachunkz/accounts:latest
```

Repeat for any other service images that need to be built.

If the application configuration uses the custom Java base image, build it from the repository root:

```powershell
docker build -t soyachunkz/java25-curl:latest -f docker/java25-curl/DockerFIle .
```

Use image names and tags that match the Compose configuration.

**You do not need to push locally built images to Docker Hub** when the images are already available to the Docker engine running your Compose environment.

If the required images are already published and the Compose configuration references those published images, you can pull them instead of building them locally.

### Step 2: Start the Complete Environment

From the repository root:

```powershell
docker compose -f docker-compose/default/docker-compose.yml up -d
```

Alternatively, enter the directory first:

```powershell
cd docker-compose/default
docker compose up -d
```

Both commands target the default Compose setup.

### Step 3: Check Container Status

From the Compose directory, run:

```powershell
docker compose ps
```

Follow logs for all services:

```powershell
docker compose logs -f
```

To follow the logs of a specific service:

```powershell
docker compose logs -f accounts
```

Replace `accounts` with the relevant Compose service name.

### Step 4: Stop the Environment

From the Compose directory:

```powershell
docker compose down
```

This stops and removes the Compose containers and network. Named volumes are normally retained unless explicitly removed.

## Service Endpoints

These are the documented default application ports. Confirm them against the active configuration if they have changed.

| Component | Default port |
|---|---:|
| Config Server | `8071` |
| Eureka Server | `8070` |
| Gateway Server | `9090` |
| Accounts Service | `8080` |
| Loans Service | `8090` |
| Cards Service | `9000` |
| Keycloak | `8180` |

For normal client API requests, use the Gateway where the relevant routes are configured.

## Troubleshooting

### A MySQL container fails to start

Inspect its logs:

```powershell
docker logs accountsdb
docker logs loansdb
docker logs cardsdb
```

Check for port conflicts, database initialization errors, or invalid environment variables.

### Keycloak is not accessible

Check the container status and logs:

```powershell
docker ps -a --filter "name=keycloak"
docker logs keycloak
```

If the container already exists but is stopped, run:

```powershell
docker start keycloak
```

### Config Server cannot load configuration

Check the external configuration repository URL, repository access, required credentials, and the selected Spring profile.

### A service cannot connect to its database

When running Spring Boot directly from IntelliJ, the database hostname should resolve to the local machine, and the connection should use the published host port.

For example, the Accounts database is exposed on port `13306`.

When running services inside Docker Compose, use the appropriate Compose service hostname and container port instead.

### A service fails during startup

Inspect its IntelliJ console or container logs. Verify that its required infrastructure is available and that configuration, credentials, ports, and environment variables are correct.

---
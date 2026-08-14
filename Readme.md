# SammoBank Microservices

## SammoBank is a Spring Boot microservices-based banking application consisting of independent services for Accounts, Loans, and Cards.

### The project currently uses:

- Spring Boot
- Spring Cloud Config Server
- Eureka Service Discovery
- OpenFeign
- MySQL
- RabbitMQ
- Docker
- Docker Compose

The services are designed to communicate through service discovery rather than relying on hardcoded service addresses.

### Architecture
Current architecture up to Eureka Service Discovery:
```

                         ┌─────────────────────┐
                         │    Config Server    │
                         │       :8071         │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │   Eureka Server     │
                         │       :8070         │
                         └──────────┬──────────┘
                                    │
                 ┌──────────────────┼──────────────────┐
                 │                  │                  │
                 ▼                  ▼                  ▼
          ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
          │   Accounts  │    │    Loans    │    │    Cards    │
          │    :8080    │    │    :8090    │    │    :9000    │
          └──────┬──────┘    └──────┬──────┘    └──────┬──────┘
                 │                  │                  │
                 ▼                  ▼                  ▼
          ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
          │  AccountsDB │    │   LoansDB   │    │   CardsDB   │
          │    MySQL    │    │    MySQL    │    │    MySQL    │
          └─────────────┘    └─────────────┘    └─────────────┘

                         ┌─────────────────────┐
                         │      RabbitMQ       │
                         │   Message Broker    │
                         └─────────────────────┘
```

### Local Development

Local development requires 4 infrastructure containers:

- RabbitMQ
- Accounts MySQL
- Loans MySQL
- Cards MySQL

The Spring Boot services themselves can be run locally or built into Docker images.

### RabbitMQ
RabbitMQ is used by the Config Server / Spring Cloud Bus setup.
```docker
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:4-management
```

### MySQL Containers
Accounts DB
```docker
docker run -p 13306:3306 --name accountsdb -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=accountsdb -d mysql
```
Loans DB
```docker
docker run -p 23306:3306 --name loansdb -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=loansdb -d mysql
```
Cards DB
```docker
docker run -p 33306:3306 --name cardsdb -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=cardsdb -d mysql
```
Check running containers:
```docker  
docker ps
```


### Maven Build

Clean and build the projects using Maven Wrapper:

```maven
.\mvnw.cmd clean install
```

### Building Docker Images

The project currently uses different image-building approaches depending on the service.

Accounts, Config Server and Eureka Server, these services use a Dockerfile. 
After the Maven build, build the images:
```docker 
docker build -t soyachunkz/accounts:latest .
docker build -t soyachunkz/configserver:latest .
docker build -t soyachunkz/eurekaserver:latest .
```

Loans and Cards

Loans uses Spring Boot Buildpacks to create container image.
```docker 
.\mvnw.cmd jib:dockerBuild
.\mvnw.cmd spring-boot:build-image
```

Cards uses Jib for building its Docker image.
```docker 
.\mvnw.cmd jib:dockerBuild
```

### Docker Compose
```docker 
docker compose -f docker-compose/default/docker-compose.yml up
docker compose -f docker-compose/qa/docker-compose.yml up
docker compose -f docker-compose/prod/docker-compose.yml up
```

### Service Discovery

Eureka is used as the service registry.

The microservices register themselves with Eureka:

```
Accounts ──┐
Loans ─────┼──→ Eureka Server :8070
Cards ─────┘
```

Services can then discover other services using their registered service name instead of relying on fixed IP addresses.

### Configuration Server

The Config Server centralizes configuration for the microservices.

```
Config Server
│
├── Accounts
├── Loans
├── Cards
└── Eureka Server
```

The services retrieve their environment-specific configuration from the Config Server.

Config Git Repo: [https://github.com/SoyaChunkz/MICROSERVICES-CONFIG]


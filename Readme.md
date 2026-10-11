# SammoBank Microservices

## SammoBank is a Spring Boot microservices-based banking application consisting of independent services for Accounts, Loans, and Cards.

### The project currently uses:

- Spring Boot
- Spring Cloud Config Server
- Spring Cloud Eureka Service Discovery
- Spring Cloud Gateway
- Spring Cloud OpenFeign
- Spring Cloud Bus
- Spring Boot Actuator
- Spring Boot Validation
- Spring Data JPA
- Springdoc OpenAPI
- MySQL
- RabbitMQ
- Docker
- Docker Compose
- Google Jib

The services are designed to communicate through service discovery rather than relying on hardcoded service addresses.

### Architecture
Current architecture up to Eureka Service Discovery:
```
```text
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
                                    ▼
                         ┌─────────────────────┐
                         │    Gateway Server   │
                         │       :9090         │
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
#### Update!!!

Use the docker compose present in the root dir for local setup to run these containers for local setup with a single command.
```docker 
docker compose up
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

#### Update!!!
All application modules use Google Jib to build Docker images.

Jib builds the Docker image directly from the Maven project without requiring a Dockerfile for each application.

#### Custom Java 25 + CURL Base Image

Services that require curl for Docker healthchecks use the custom base image:
```docker 
soyachunkz/java25-curl:latest
```
The base image is built once from:
```docker
docker build -t soyachunkz/java25-curl:latest ./docker/java25-curl
```

If the image is being used across different machines or environments, push it to Docker Hub:
```docker
docker push soyachunkz/java25-curl:latest
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

### Gateway Server

Spring Cloud Gateway acts as the entry point for client requests.

```
                         Client
                           │
                           ▼
                    ┌─────────────┐
                    │   Gateway   │
                    │    :9090    │
                    ├─────────────┤
                    │   Routing   │
                    │   Logging   │
                    │ Correlation │
                    └──────┬──────┘
                           │
             ┌─────────────┼─────────────┐
             ▼             ▼             ▼
         Accounts        Loans         Cards
             │
             │ Feign
             ├────────────────→ Cards
             │
             └────────────────→ Loans
                    │
                    ▼
              Aggregated CustomerDetails
```

The Gateway uses Eureka service discovery and Spring Cloud LoadBalancer to locate the target services.

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

### Docker Base Image for Curl
```docker 
docker build -t soyachunkz/java25-curl:latest ./docker/java25-curl
```


```
                    ┌──────────────┐
                    │ Spring Boot  │
                    │ Microservices│
                    └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
             Logs        Metrics       Traces
              │            │            │
              ▼            ▼            ▼
            Alloy      Prometheus     OpenTelemetry
              │            ▲            │
              ▼            │            ▼
             Loki    MySQL Exporter    Tempo
              │            ▲            │
              │            │            │
              └────────────┼────────────┘
                           ▼
                        Grafana
                        
                        
            Accounts DB ──┐
            Loans DB ─────┼──→ MySQL Exporter ──→ Prometheus ──→ Grafana
            Cards DB ─────┘
            
            
  Gateway ──→ Accounts ──→ Cards
    │            │
    │            └──────→ Loans
    │
    └── OpenTelemetry ──→ Tempo ──→ Grafana
```

### OAuth2 Security with Keycloak

This section implements OAuth2-based authentication and authorization in the microservices architecture using **Keycloak** as the Authorization Server and the **Gateway Server** as an OAuth2 Resource Server.

#### OAuth2 Grant Types

A grant type describes how a client obtains an access token.

We implemented/learned two important flows:

1. Client Credentials Grant
2. Authorization Code Grant

In Local dev env, Keycloak is running as a Docker container.
```docker 
docker run -p 127.0.0.1:8180:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.7.2 start-dev
```

```
                        KEYCLOAK
                  Authorization Server
                           │
            ┌──────────────┴────────────────┐
            │                               │
            │                               │
        AC Client                       CC Client
   sammobank-callcenter-ac         sammobank-callcenter-cc
            │                               │
            │                               │
        User Flow                      Service Flow
            │                               │
        sammo user                    Service Account
            │                               │
            └──────────────┬────────────────┘
                           │
                          JWT
                           │
                           ▼
                  ┌──────────────────────┐
                  │       GATEWAY        │
                  │   Resource Server    │
                  │                      │
                  │  Spring Security     │
                  └──────────┬───────────┘
                      JWT validation
                             │
                       `kid` → JWK
                             │
                    Public key verification
                             │
                       Extract roles
                             │
              ┌──────────────┼──────────────┐
              ▼              ▼              ▼
          ACCOUNTS         CARDS           LOANS
              │              │              │
              ▼              ▼              ▼
          Accounts MS      Cards MS       Loans MS
```

| Concept                     | 🐇 RabbitMQ                                  | 🟠 Kafka                                              |
| --------------------------- | -------------------------------------------- | ----------------------------------------------------- |
| Main destination            | **Exchange**                                 | **Topic**                                             |
| Consumers receive through   | **Queues**                                   | **Consumer Groups**                                   |
| Your first destination      | `send-communication` **exchange**            | `send-communication` **topic**                        |
| Email processing            | `email-service` **queue** → `email()`        | `email-service` **consumer group** → `email()`        |
| SMS processing              | `sms-service` **queue** → `sms()`            | `sms-service` **consumer group** → `sms()`            |
| Result destination          | `communication-sent` **exchange**            | `communication-sent` **topic**                        |
| Accounts processing         | Accounts **queue** → `updateCommunication()` | Accounts **consumer group** → `updateCommunication()` |
| Message storage             | Mainly queue-based delivery                  | Messages remain in topic partitions for retention     |
| Same message to email + SMS | Separate queues bound to exchange            | Separate consumer groups                              |


``` 
                                RABBITMQ
                                    │
                                Accounts
                                    │
                                    │ sendCommunication()
                                    ▼
                        ┌──────────────────────────────┐
                        │ send-communication EXCHANGE  │
                        └──────────────┬───────────────┘
                                       │
                                ┌──────┴──────┐
                                ▼             ▼
                         ┌─────────────┐ ┌────────────┐
                         │email-service│ │ sms-service│
                         │   QUEUE     │ │   QUEUE    │
                         └──────┬──────┘ └──────┬─────┘
                                │              │
                                ▼              ▼
                              email()        sms()
                                │              │
                                └──────┬───────┘
                                       ▼
                        ┌──────────────────────────────┐
                        │ communication-sent EXCHANGE  │
                        └──────────────┬───────────────┘
                                       │
                                       ▼
                               ┌────────────────┐
                               │ Accounts QUEUE │
                               └───────┬────────┘
                                       ▼
                           updateCommunication()
                                       │
                                       ▼
                                  Update DB
                                  
----------------------------------------------------------------------------------------------------------

                                  KAFKA
                                    │
                                Accounts
                                    │
                                    │ sendCommunication()
                                    ▼
                        ┌──────────────────────────────┐
                        │ send-communication  TOPIC    │
                        └──────────────┬───────────────┘
                                       │
                                ┌──────┴──────┐
                                ▼             ▼
                         ┌──────────────┐ ┌──────────────┐
                         │email-service │ │ sms-service  │
                         │ CONSUMER     │ │ CONSUMER     │
                         │    GROUP     │ │    GROUP     │
                         └──────┬───────┘ └──────┬───────┘
                                │                │
                                ▼                ▼
                              email()          sms()
                                │                │
                                └────────┬───────┘
                                         ▼
                        ┌──────────────────────────────┐
                        │ communication-sent  TOPIC    │
                        └──────────────┬───────────────┘
                                       │
                                       ▼
                              ┌──────────────────┐
                              │ Accounts CONSUMER│
                              │      GROUP       │
                              └────────┬─────────┘
                                       ▼
                           updateCommunication()
                                       │
                                       ▼
                                  Update DB
```

```
kubectl port-forward -n kubernetes-dashboard svc/kubernetes-dashboard-kong-proxy 8443:443

kubectl create token admin-user -n kubernetes-dashboard

kubectl create configmap keycloak-realm-config --from-file=sammobank-realm.json=../docker-compose/keycloak/realm/sammobank-realm.json
```

```
kubectl apply -f .\3.2_configserver-secret.yml 


# 1. Databases
helm install databases .\sammobank-databases\

# 2. Keycloak
helm install keycloak .\keycloak\

# 3. Kafka
helm install kafka .\kafka\

# 4. SammoBank microservices
helm install dev-env .\environments\dev-env\

# 5. MySQL Exporter
helm install mysql-exporter .\mysql-exporter\

# 6. Prometheus
helm install kube-prometheus .\kube-prometheus\

# 7. Loki + MinIO
helm install grafana-loki .\grafana-loki\

# 8. Grafana Alloy
helm install grafana-alloy .\grafana-alloy\

# 9. Tempo
helm install grafana-tempo .\grafana-tempo\

# 10. Grafana
helm install grafana .\grafana\

kubectl port-forward svc/keycloak 8180:8180
```


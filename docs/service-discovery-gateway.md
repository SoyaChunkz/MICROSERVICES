# Service Discovery and Gateway Server

This document describes the service discovery and API gateway setup in SammoBank.

## 1. Eureka Service Discovery

SammoBank uses **Eureka Server** as a service registry. The microservices register themselves with Eureka so they can discover other services by their registered service names instead of relying on fixed IP addresses.

- **Eureka Server:** `8070`
- **Gateway Server:** `9090`

The documented service registration flow is:

```text
       Accounts ──┐
       Loans ─────┼──→ Eureka Server :8070
       Cards ─────┘
```

### How It Works

1. A service starts and registers with Eureka.
2. Eureka maintains the registry of service instances.
3. Services can discover other services using their registered names.
4. Spring Cloud LoadBalancer can select an available instance when configured for the call.

The exact registration and discovery behavior depends on each application's configuration.

## 2. Spring Cloud Gateway

Spring Cloud Gateway acts as the entry point for client requests.

The old README documents these responsibilities:

- Routing requests to Accounts, Loans, and Cards.
- Logging.
- Correlation identifiers.
- Using Eureka service discovery and Spring Cloud LoadBalancer to locate target services.

```text
                        Client
                          |
                          v
                   +-------------+
                   |   Gateway   |
                   |    :9090    |
                   +-------------+
                          |
            +-------------+-------------+
            |             |             |
            v             v             v
        Accounts        Loans         Cards
```

Check the current Gateway route and filter configuration to confirm which routes, logging filters, and correlation-ID features are enabled.

## 3. OpenFeign Inter-Service Communication

The old README describes the Accounts service using **OpenFeign** to call the Cards and Loans services and aggregate a `CustomerDetails` response.

```text
        Accounts
            |
            | Feign
            +------------------> Cards
            |
            +------------------> Loans
                    |
                    v
             Aggregated CustomerDetails
```

OpenFeign provides declarative HTTP clients. In a service-discovery setup, a client can use a registered service name, while Spring Cloud LoadBalancer can select a target instance if configured.

This diagram reflects the behavior described in the old README. Verify the current Feign client interfaces and aggregation code before treating the exact response flow as confirmed.

## 4. Configuration Server Relationship

The old README shows Config Server supplying configuration to the services, including Accounts, Loans, Cards, and Eureka Server.

The documented default Config Server port is `8071`.

Configuration repository:

[MICROSERVICES-CONFIG](https://github.com/SoyaChunkz/MICROSERVICES-CONFIG)

## 5. Documented Service Ports

| Component | Port |
|---|---:|
| Config Server | `8071` |
| Eureka Server | `8070` |
| Gateway Server | `9090` |
| Accounts | `8080` |
| Loans | `8090` |
| Cards | `9000` |

These ports are carried over from the old README. Verify them against the current configuration before use.

## 6. Troubleshooting

- **Service does not appear in Eureka:** Check its Eureka client configuration, startup logs, and network connectivity.
- **Gateway cannot route to a service:** Check route configuration, registered service name, port, and service health.
- **OpenFeign call fails:** Check the Feign client name, Eureka registration, LoadBalancer configuration, timeouts, and downstream service logs.
- **Works locally but not in containers:** Verify hostnames and ports for the relevant runtime environment.

## Related Documentation

- Root `Readme.md` — project overview and architecture.
- `docs/project-setup.md` — local startup instructions.
- `docs/configuration-management.md` — centralized configuration.
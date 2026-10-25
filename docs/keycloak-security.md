# OAuth2 Security with Keycloak

SammoBank implements OAuth 2.0-based authentication and authorization using **Keycloak** as the Authorization Server and the **Spring Cloud Gateway** as an OAuth2 Resource Server.

Keycloak handles identity and access token issuance, while the Gateway protects API endpoints by validating access tokens and enforcing the configured authorization rules.

## Table of Contents

- [Overview](#overview)
- [Key Concepts](#key-concepts)
- [OAuth2 Grant Types](#oauth2-grant-types)
- [Architecture](#architecture)
- [Authentication and Authorization Flow](#authentication-and-authorization-flow)
- [JWT Validation](#jwt-validation)
- [Local Keycloak Setup](#local-keycloak-setup)
- [Testing the Authentication Flow](#testing-the-authentication-flow)
- [Security Best Practices](#security-best-practices)
- [Troubleshooting](#troubleshooting)

## Overview

OAuth 2.0 is an authorization framework that allows clients to obtain access tokens and use them to access protected resources.

OpenID Connect (OIDC) builds on OAuth 2.0 and adds an identity layer for authenticating users.

In SammoBank:

- **Keycloak** acts as the Authorization Server.
- **Gateway Server** acts as an OAuth2 Resource Server.
- **Spring Security** provides security integration and token validation.
- **JWT access tokens** carry claims about the client, user, and granted permissions.
- **Backend microservices** handle business operations after requests are routed through the Gateway.

The exact authorization checks depend on the application's security configuration and the roles or authorities configured in Keycloak.

## Key Concepts

### OAuth 2.0

OAuth 2.0 allows a client to obtain an access token and use it to access protected APIs.

An access token represents the authorization granted to the client. The client sends the token with requests to protected endpoints.

```http
Authorization: Bearer <access_token>
```

### OpenID Connect

OpenID Connect (OIDC) adds authentication capabilities to OAuth 2.0.

It defines how clients can authenticate users and obtain identity information, including through ID tokens when applicable.

### JSON Web Token (JWT)

A JWT is a token format commonly used for access tokens.

A signed JWT typically contains three parts:

```text
Header.Payload.Signature
```

- **Header:** Contains token metadata, such as the signing algorithm and key identifier (`kid`).
- **Payload:** Contains claims such as issuer, subject, audience, expiry, and potentially roles or scopes.
- **Signature:** Allows the recipient to verify that the token was signed by a trusted issuer and has not been modified.

A JWT is generally encoded, not encrypted. Its payload should not contain secrets.

### JSON Web Key Set (JWKS)

Keycloak publishes public keys that resource servers can use to verify signed tokens.

A JWT's `kid` identifies the signing key used for the token. The resource server can use this identifier to select the corresponding public key from the issuer's published JWK Set.

This allows the Gateway to validate signed tokens without contacting Keycloak for every API request, provided the necessary keys are available.

## OAuth2 Grant Types

A grant type describes how a client obtains an access token.

SammoBank's documented security learning and configuration cover two important flows:

1. Client Credentials Grant
2. Authorization Code Grant

### 1. Client Credentials Grant

The Client Credentials flow is intended for machine-to-machine communication when a client acts on its own behalf rather than on behalf of an interactive user.

Typical use cases include background services, scheduled jobs, and trusted service integrations.

Flow:

1. A client authenticates to Keycloak using its configured client credentials.
2. Keycloak validates the client.
3. Keycloak issues an access token.
4. The client sends the token to the Gateway.
5. The Gateway validates the token and applies the configured authorization rules.
6. The Gateway routes the request to the appropriate backend service.

Conceptual flow:

```mermaid
sequenceDiagram
    participant Client as Client Application
    participant KC as Keycloak
    participant GW as Gateway
    participant API as Backend Service

    Client->>KC: Request token using client credentials
    KC-->>Client: Access token
    Client->>GW: API request with Bearer token
    GW->>GW: Validate token and authorization
    GW->>API: Forward authorized request
    API-->>GW: Response
    GW-->>Client: API response
```

In Keycloak, the client must be configured appropriately for service authentication, including enabling its service account where required.

### 2. Authorization Code Grant

The Authorization Code flow is designed for clients that act on behalf of a user.

It is commonly used by web applications and other clients that can participate in an interactive sign-in flow.

Flow:

1. The user attempts to access a protected resource or sign in.
2. The client redirects the user to Keycloak.
3. The user authenticates with Keycloak.
4. Keycloak redirects the user back to the registered redirect URI with an authorization code.
5. The client exchanges the authorization code for tokens.
6. The client sends the access token to the Gateway.
7. The Gateway validates the token and applies the configured authorization rules.
8. The Gateway routes the request to the appropriate backend service.

Conceptual flow:

```mermaid
sequenceDiagram
    participant User
    participant Client as Client Application
    participant KC as Keycloak
    participant GW as Gateway
    participant API as Backend Service

    User->>Client: Access protected resource
    Client->>KC: Redirect to authorize
    User->>KC: Authenticate
    KC-->>Client: Redirect with authorization code
    Client->>KC: Exchange code for tokens
    KC-->>Client: Access token and other applicable tokens
    Client->>GW: API request with Bearer token
    GW->>GW: Validate token and authorization
    GW->>API: Forward authorized request
    API-->>GW: Response
    GW-->>Client: API response
```

For production use, clients should use the appropriate secure flow for their application type. Authorization Code with PKCE is recommended for public clients and is also suitable for many other client types.

## Architecture

### SammoBank Keycloak Architecture

The following diagram represents the security architecture described in the original project documentation.

```mermaid
flowchart TB
    KC["KEYCLOAK<br/>Authorization Server"]

    subgraph Clients["OAuth2 Clients"]
        AC["AC Client<br/>sammobank-callcenter-ac"]
        CC["CC Client<br/>sammobank-callcenter-cc"]
    end

    User["sammo user"]
    SA["Service Account"]

    AC --> User
    CC --> SA

    User -->|"Authorization Code flow"| KC
    SA -->|"Client Credentials flow"| KC

    KC -->|"JWT access token"| GW

    GW["GATEWAY<br/>OAuth2 Resource Server<br/>Spring Security"]

    GW --> V["Validate JWT"]
    V --> K["Read kid and select JWK"]
    K --> P["Verify token signature and claims"]
    P --> R["Apply configured authorization rules"]

    R --> Accounts["ACCOUNTS"]
    R --> Cards["CARDS"]
    R --> Loans["LOANS"]

    Accounts --> AMS["Accounts Microservice"]
    Cards --> CMS["Cards Microservice"]
    Loans --> LMS["Loans Microservice"]
```

The client names and user label above are retained from the original project notes. Confirm that these clients, users, service accounts, and roles match the currently imported Keycloak realm before relying on them.

### Component Responsibilities

| Component | Responsibility |
|---|---|
| Keycloak | Authenticates users and clients and issues tokens |
| OAuth2 client | Obtains tokens and sends them with API requests |
| Gateway Server | Validates access tokens and routes API requests |
| Spring Security | Integrates token authentication and authorization into the Gateway |
| Backend microservices | Perform business operations behind the configured API routes |

## Authentication and Authorization Flow

Authentication and authorization are related but different.

- **Authentication:** Establishes the identity represented by a valid token.
- **Authorization:** Determines whether that identity has permission to perform a requested operation.

A successful token validation does not automatically mean that every API operation is permitted.

The Gateway can validate token claims such as the issuer, expiry, and signature. Role- or scope-based authorization depends on the application's configured security rules and how Keycloak claims are mapped to Spring Security authorities.

## JWT Validation

When a client sends a protected API request, it includes its access token in the `Authorization` header.

```http
GET /api/accounts
Authorization: Bearer <access_token>
```

The Gateway's resource-server configuration validates the token before allowing the request through.

Typical validation steps include:

1. Extract the Bearer token from the request.
2. Verify the token signature using a trusted signing key.
3. Validate standard claims, including issuer and expiration.
4. Validate the audience where the application is configured to require it.
5. Convert configured roles or scopes into authorities.
6. Apply endpoint authorization rules.
7. Route authorized requests to the relevant backend service.

### How `kid` and JWKs Work Together

The JWT header may contain a key identifier:

```json
{
  "alg": "RS256",
  "kid": "example-key-id",
  "typ": "JWT"
}
```

The `kid` value identifies the signing key. The Gateway can use the trusted issuer's published JWK Set to select the corresponding public key and verify the token signature.

The example above is illustrative; the actual algorithm and key identifier depend on the token issued by Keycloak.

A valid signature alone is not sufficient. The token must also satisfy the resource server's issuer, expiry, and other configured claim checks.

## Local Keycloak Setup

During local development, Keycloak can run as a Docker container.

### Start Keycloak

Run this command in PowerShell:

```powershell
docker run -d `
  --name keycloak `
  -p 127.0.0.1:8180:8080 `
  -e KC_BOOTSTRAP_ADMIN_USERNAME=admin `
  -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin `
  quay.io/keycloak/keycloak:26.7.2 start-dev
```

Keycloak will be available at:

- **Admin Console:** http://localhost:8180
- **Username:** `admin`
- **Password:** `admin`

These are development credentials only.

If the container already exists and is stopped, start it with:

```powershell
docker start keycloak
```

To inspect the logs:

```powershell
docker logs -f keycloak
```

### Configure the Realm

SammoBank includes a realm configuration file in the repository:

```text
docker-compose/keycloak/realm/sammobank-realm.json
```

The realm configuration may define clients, roles, users, service accounts, and other identity settings.

To use the project's intended authentication flows:

1. Start Keycloak.
2. Open the Admin Console.
3. Import the realm configuration using Keycloak's realm import functionality.
4. Verify the client IDs and their enabled authentication flows.
5. Verify the registered redirect URIs for Authorization Code clients.
6. Verify the service account configuration for Client Credentials clients.
7. Confirm the roles and permissions expected by the Gateway.

The exact realm-import procedure can vary with the Keycloak version and deployment setup.

**Important:** Realm exports can contain sensitive configuration. Never commit real passwords, client secrets, or production credentials to the repository.

## Testing the Authentication Flow

The following is a conceptual example for testing a protected API. The actual token endpoint, client IDs, secrets, scopes, and API route must match the active realm and application configuration.

### Client Credentials

A typical token request uses the Keycloak token endpoint:

```http
POST http://localhost:8180/realms/<realm-name>/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded
```

Form parameters:

```text
grant_type=client_credentials
client_id=<client-id>
client_secret=<client-secret>
```

If the client is configured correctly, Keycloak returns an access token.

Send that token to the Gateway:

```http
GET http://localhost:9090/<configured-api-route>
Authorization: Bearer <access_token>
```

### Authorization Code

For Authorization Code, use a client configured with the appropriate redirect URI and flow settings.

The user signs in through Keycloak, the client receives an authorization code, and the client exchanges that code for tokens. The access token is then sent to the Gateway in the Bearer header.

Use the actual redirect URI, client settings, and token endpoint from the imported realm configuration.

### Expected Results

| Scenario | Expected behavior |
|---|---|
| Valid token with sufficient permissions | Request can proceed to the configured route |
| Missing token on a protected endpoint | Authentication failure, commonly HTTP `401` |
| Invalid or expired token | Authentication failure, commonly HTTP `401` |
| Valid token without required permissions | Authorization failure, commonly HTTP `403` |
| Token valid but backend unavailable | A downstream error, depending on Gateway configuration |

The exact status codes and responses depend on the endpoint and error-handling configuration.

## Security Best Practices

- Do not use development credentials in production.
- Keep client secrets and passwords outside source control.
- Use HTTPS for production traffic.
- Register exact redirect URIs rather than broad or wildcard URIs.
- Use Authorization Code with PKCE where appropriate.
- Give clients only the roles and permissions they require.
- Validate token issuer, expiry, signature, and applicable audience claims.
- Use trusted issuer metadata and signing keys.
- Avoid logging access tokens, refresh tokens, or client secrets.
- Rotate credentials and signing keys according to operational requirements.
- Keep Keycloak and application dependencies updated.

## Troubleshooting

### Invalid Token or HTTP 401

Check that:

- The token has not expired.
- The token was issued by the expected Keycloak realm.
- The Gateway trusts the correct issuer.
- The token signature can be verified using the issuer's signing keys.
- The client is sending the access token rather than an ID token.

### HTTP 403 Forbidden

The token may be valid, but the authenticated principal may not have the required role, scope, or authority.

Verify the realm's role configuration and the Gateway's authority mapping and authorization rules.

### Token Endpoint Returns an Error

Check that:

- The realm name is correct.
- The client ID is correct.
- The client supports the requested grant type.
- The service account is enabled for Client Credentials.
- The client secret is correct where required.
- The redirect URI is registered for Authorization Code.

### Gateway Cannot Validate Tokens

Verify that Keycloak is reachable from the Gateway's runtime environment and that the issuer URI and JWK discovery configuration are correct.

Remember that `localhost` inside a container refers to that container itself, not automatically to the Keycloak container or the host machine.

---

For related topics, see:

- [Project Setup](project-setup.md)
- [Kafka Messaging](kafka-messaging.md)
- [Service Discovery and Gateway](service-discovery-gateway.md)
- [Configuration Management](configuration-management.md)
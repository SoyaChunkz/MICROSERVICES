# SammoBank shared Maven parent

This POM is the shared parent for the SammoBank microservices.
It inherits Spring Boot's parent and centralizes common dependency versions and Maven plugin configuration.

Each service declares `com.sammo:sammobank-bom:1.0.0-SNAPSHOT` as its parent and resolves it from `../sammobank-bom/pom.xml`.
In this repository, Maven reads that POM directly from the checkout; it does not need to be separately built, installed, or published.

From a service directory, run its Maven wrapper to verify the parent and build:

```powershell
.\mvnw.cmd clean verify
```

The CI workflow treats changes anywhere under `sammobank-bom/` as shared-parent changes, so it runs build-and-test jobs for all seven services.
Docker images are published only on a push to `main` after all service builds and tests pass; a pull request validates the builds but does not publish images.

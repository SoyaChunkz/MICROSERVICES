# Observability in SammoBank

The old SammoBank README documents an observability stack built around **Grafana Alloy, Loki, Prometheus, MySQL Exporter, OpenTelemetry, Tempo, and Grafana**.

The component descriptions below preserve the original README's intended data flow. Check the current Compose files, Helm values, Alloy pipelines, Prometheus targets, and application instrumentation to confirm which integrations are enabled.

## 1. Components

| Component | Role |
|---|---|
| Grafana Alloy | Collects and forwards configured logs and telemetry |
| Loki | Log storage and querying |
| Prometheus | Metrics collection and storage |
| MySQL Exporter | Exposes database metrics for Prometheus |
| OpenTelemetry | Instrumentation and trace telemetry |
| Tempo | Trace storage and querying |
| Grafana | Dashboards and exploration across configured data sources |

## 2. Intended Data Flow

The old README presents this general relationship between application telemetry and the observability components:

```text
                   +------------------+
                   | Spring Boot      |
                   | Microservices    |
                   +--------+---------+
                            |
              +-------------+-------------+
              |             |             |
             Logs         Metrics       Traces
              |             |             |
              v             v             v
            Alloy       Prometheus   OpenTelemetry
              |             ^             |
              v             |             v
             Loki      MySQL Exporter    Tempo
              |             ^             |
              |             |             |
              +-------------+-------------+
                            |
                            v
                         Grafana

           Accounts DB ----+
           Loans DB -------+--> MySQL Exporter
           Cards DB -------+          |
                                      v
                                 Prometheus
                                      |
                                      v
                                   Grafana

           Gateway --> Accounts --> Cards
               |          |
               |          +--------> Loans
               |
               +--> OpenTelemetry --> Tempo --> Grafana
```

This is a conceptual representation of the old README, not a guarantee that every arrow is currently configured exactly this way.

## 3. Logs: Alloy and Loki

The old README describes application logs flowing through Alloy into Loki, with Grafana used to query and visualize the logs.

When troubleshooting missing logs, check:

- Alloy configuration and pipelines.
- Whether the relevant containers or log files are accessible to Alloy.
- Loki's configured endpoint.
- Labels and queries used in Grafana.

## 4. Metrics: Prometheus and MySQL Exporter

The old README shows MySQL Exporter exposing database metrics for Prometheus to scrape, covering the Accounts, Loans, and Cards databases.

When troubleshooting missing metrics, check:

- Prometheus scrape configuration and target health.
- MySQL Exporter availability and database connection settings.
- Network access and ports.
- Grafana's Prometheus data source and query.

Application metrics depend on the instrumentation and Actuator endpoints enabled in each service.

## 5. Traces: OpenTelemetry and Tempo

The old README shows OpenTelemetry tracing flowing to Tempo, with Grafana used for exploration.

When troubleshooting traces, check:

- Whether the application is instrumented to emit traces.
- The configured telemetry exporter endpoint.
- Any Alloy or collector pipeline used to forward trace data.
- Tempo availability and Grafana's data source configuration.

## 6. Repository Locations

The old README identifies these deployment resources:

- `docker-compose/observability/`
- `helm/grafana/`
- `helm/grafana-alloy/`
- `helm/grafana-loki/`
- `helm/grafana-tempo/`
- `helm/kube-prometheus/`
- `helm/mysql-exporter/`

## 7. Related Documentation

- Root `Readme.md` — project overview and architecture.
- `helm/README.md` — Helm deployment sequence.
- `k8/README.md` — basic Kubernetes manifests.
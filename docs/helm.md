# Helm Deployment

This document captures the Helm chart structure and installation order.

The commands are preserved as documented, but chart dependencies, paths, namespaces, values, and release names should be verified against the current repository before running them.

## 1. Chart Structure

```text
helm/
├── environments/
│   ├── dev-env/
│   ├── qa-env/
│   └── prod-env/
├── grafana/
├── grafana-alloy/
├── grafana-loki/
├── grafana-tempo/
├── kafka/
├── keycloak/
├── kube-prometheus/
├── mysql-exporter/
├── sammobank-common/
├── sammobank-databases/
└── sammobank-services/
```

The `dev-env` setup is a complete setup. The QA and production environments exist, but may need further updates or validation.

## 2. Before Installing

1. Confirm your current Kubernetes context points to the intended cluster.
2. Ensure required application images are available to the cluster.
3. Review the chart values and templates.
4. Supply any required secrets securely.
5. Confirm required storage classes and persistent volumes are available.
6. Check the namespace and resource requirements.

Helm does not build application images. Build and publish them, or otherwise make them available to the cluster, before installing charts that reference them.

### Required Kubernetes Secret

Before deploying the SammoBank services, create the `configserver-secret`
Kubernetes Secret containing:

- GitHub credentials for Config Server to access `MICROSERVICES-CONFIG`.
- Email credentials for Message Service to send email notifications.



Refer to [ Kubernetes Manifests ](k8.md) for the Secret template and creation instructions. 

The Secret must exist in the same namespace as the workloads that consume it.
The Helm charts should reference this existing Secret.

## 3. Installation Order from the Old README

Run these commands from the repository's `helm/` directory.

```powershell
# 1. Databases
helm install databases .\sammobank-databases

# 2. Keycloak
helm install keycloak .\keycloak

# 3. Kafka
helm install kafka .\kafka

# 4. SammoBank microservices
helm install dev-env .\environments\dev-env

# 5. MySQL Exporter
helm install mysql-exporter .\mysql-exporter

# 6. Prometheus
helm install kube-prometheus .\kube-prometheus

# 7. Loki + MinIO
helm install grafana-loki .\grafana-loki

# 8. Grafana Alloy
helm install grafana-alloy .\grafana-alloy

# 9. Tempo
helm install grafana-tempo .\grafana-tempo

# 10. Grafana
helm install grafana .\grafana
```

Review chart dependencies and values before installing. Some charts may have prerequisites or require a namespace, values file, or specific release name.

## 4. Keycloak Port Forwarding

```powershell
kubectl port-forward svc/keycloak 8180:8180
```

This assumes a service named `keycloak` exposes port `8180`.

Verify the actual service name, namespace, and port:

```powershell
kubectl get services
kubectl get services -A
```

## 5. Observability Charts

The chart list includes:

- `mysql-exporter`
- `kube-prometheus`
- `grafana-loki`
- `grafana-alloy`
- `grafana-tempo`
- `grafana`

See `docs/observability.md` for the intended roles of these components.

## 6. Useful Helm and Kubernetes Commands

```powershell
helm list -A
helm status <release-name>
helm get values <release-name>
kubectl get pods
kubectl get services
kubectl get events --sort-by=.metadata.creationTimestamp
```

Use the appropriate namespace where required.

## 7. Troubleshooting

- **Chart install fails:** Check chart path, dependencies, values, and Helm output.
- **Pods cannot pull images:** Check image tag, registry access, and image pull secrets.
- **Pods fail configuration:** Verify ConfigMaps, Secrets, and environment-specific values.
- **Service unavailable:** Check Kubernetes Service ports, selectors, endpoints, and namespaces.
- **Storage issue:** Check PVCs and storage class support.

---
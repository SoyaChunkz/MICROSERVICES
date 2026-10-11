{{- define "common.configmap" -}}
apiVersion: v1
kind: ConfigMap
metadata:
  name: {{ .Values.global.configMapName }}

data:
  SPRING_PROFILES_ACTIVE: {{ .Values.global.activeProfile | quote }}
  SPRING_CONFIG_IMPORT: {{ .Values.global.configServerURL | quote }}
  EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: {{ .Values.global.eurekaServerURL | quote }}

  SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK-SET-URI: {{ .Values.global.keyCloakURL | quote }}

  JAVA_TOOL_OPTIONS: {{ .Values.global.openTelemetryJavaAgent | quote }}
  OTEL_EXPORTER_OTLP_ENDPOINT: {{ .Values.global.otelExporterEndPoint | quote }}
  OTEL_METRICS_EXPORTER: {{ .Values.global.otelMetricsExporter | quote }}
  OTEL_LOGS_EXPORTER: {{ .Values.global.otelLogsExporter | quote }}

  SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS: {{ .Values.global.kafkaBrokerURL | quote }}

  ACCOUNTS_DATASOURCE_URL: {{ .Values.global.accountsDatasourceURL | quote }}
  ACCOUNTS_DATASOURCE_USERNAME: {{ .Values.global.accountsDatasourceUsername | quote }}
  ACCOUNTS_DATASOURCE_PASSWORD: {{ .Values.global.accountsDatasourcePassword | quote }}

  LOANS_DATASOURCE_URL: {{ .Values.global.loansDatasourceURL | quote }}
  LOANS_DATASOURCE_USERNAME: {{ .Values.global.loansDatasourceUsername | quote }}
  LOANS_DATASOURCE_PASSWORD: {{ .Values.global.loansDatasourcePassword | quote }}

  CARDS_DATASOURCE_URL: {{ .Values.global.cardsDatasourceURL | quote }}
  CARDS_DATASOURCE_USERNAME: {{ .Values.global.cardsDatasourceUsername | quote }}
  CARDS_DATASOURCE_PASSWORD: {{ .Values.global.cardsDatasourcePassword | quote }}

{{- end -}}
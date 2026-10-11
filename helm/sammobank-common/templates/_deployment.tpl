{{- define "common.deployment" -}}
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ .Values.deploymentName }}
  labels:
    app: {{ .Values.appLabel }}
spec:
  replicas: {{ .Values.replicaCount }}
  selector:
    matchLabels:
      app: {{ .Values.appLabel }}
  template:
    metadata:
      labels:
        app: {{ .Values.appLabel }}
    spec:
      containers:
        - name: {{ .Values.appLabel }}
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
          ports:
            - containerPort: {{ .Values.containerPort }}
              protocol: TCP

          env:
            {{- if .Values.appname_enabled }}
            - name: SPRING_APPLICATION_NAME
              value: {{ .Values.appName | quote }}
            {{- end }}

            {{- if .Values.profile_enabled }}
            - name: SPRING_PROFILES_ACTIVE
              valueFrom:
                configMapKeyRef:
                  name: {{ .Values.global.configMapName }}
                  key: SPRING_PROFILES_ACTIVE
            {{- end }}

            {{- if .Values.config_enabled }}
            - name: SPRING_CONFIG_IMPORT
              valueFrom:
                configMapKeyRef:
                  name: {{ .Values.global.configMapName }}
                  key: SPRING_CONFIG_IMPORT
            {{- end }}

            {{- if .Values.eureka_enabled }}
            - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
              valueFrom:
                configMapKeyRef:
                  name: {{ .Values.global.configMapName }}
                  key: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
            {{- end }}

            {{- if .Values.resourceserver_enabled }}
            - name: SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK-SET-URI
              valueFrom:
                configMapKeyRef:
                  name: {{ .Values.global.configMapName }}
                  key: SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK-SET-URI
            {{- end }}

            {{- if .Values.database_enabled }}
            - name: SPRING_DATASOURCE_URL
              valueFrom:
                configMapKeyRef:
                  name: {{ .Values.global.configMapName }}
                  key: {{ .Values.database.configMapKey }}
            - name: SPRING_DATASOURCE_USERNAME
              valueFrom:
                configMapKeyRef:
                  name: {{ .Values.global.configMapName }}
                  key: {{ .Values.database.usernameConfigMapKey }}

            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                configMapKeyRef:
                  name: {{ .Values.global.configMapName }}
                  key: {{ .Values.database.passwordConfigMapKey }}
            {{- end }}

            {{- if .Values.otel_enabled }}
            - name: JAVA_TOOL_OPTIONS
              valueFrom:
                configMapKeyRef:
                  name: {{ .Values.global.configMapName }}
                  key: JAVA_TOOL_OPTIONS

            - name: OTEL_EXPORTER_OTLP_ENDPOINT
              valueFrom:
                configMapKeyRef:
                  name: {{ .Values.global.configMapName }}
                  key: OTEL_EXPORTER_OTLP_ENDPOINT

            - name: OTEL_METRICS_EXPORTER
              valueFrom:
                configMapKeyRef:
                  name: {{ .Values.global.configMapName }}
                  key: OTEL_METRICS_EXPORTER

            - name: OTEL_LOGS_EXPORTER
              valueFrom:
                configMapKeyRef:
                  name: {{ .Values.global.configMapName }}
                  key: OTEL_LOGS_EXPORTER

            - name: OTEL_SERVICE_NAME
              value: {{ .Values.appName | quote }}
            {{- end }}

            {{- if .Values.kafka_enabled }}
            - name: SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS
              valueFrom:
                configMapKeyRef:
                  name: {{ .Values.global.configMapName }}
                  key: SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS
            {{- end }}

            {{- if .Values.github_secret_enabled }}
            - name: GITHUB_USERNAME
              valueFrom:
                secretKeyRef:
                  name: configserver-secret
                  key: GITHUB_USERNAME

            - name: GITHUB_TOKEN
              valueFrom:
                secretKeyRef:
                  name: configserver-secret
                  key: GITHUB_TOKEN
            {{- end }}

            {{- if .Values.mail_credentials_enabled }}
            - name: MAIL_USERNAME
              valueFrom:
                secretKeyRef:
                  name: configserver-secret
                  key: MAIL_USERNAME

            - name: MAIL_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: configserver-secret
                  key: MAIL_PASSWORD
            {{- end }}

{{- end -}}
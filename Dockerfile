FROM gcr.io/distroless/java17-debian11:latest
ENV APPD_ENABLED=false \
    APP_NAME=sykefravarsstatistikk-api \
    JAVA_TOOL_OPTIONS="-XX:+UseParallelGC -XX:MaxRAMPercentage=75" \
    TZ="Europe/Oslo"

COPY import-vault-secrets.sh /init-scripts
COPY /target/sykefravarsstatistikk-api-0.0.1-SNAPSHOT.jar app.jar

CMD ["app.jar"]
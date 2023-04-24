FROM ghcr.io/navikt/baseimages/temurin:17
ENV APPD_ENABLED=false
ENV APP_NAME=sykefravarsstatistikk-api
COPY import-vault-secrets.sh /init-scripts
COPY /target/sykefravarsstatistikk-api-0.0.1-SNAPSHOT.jar app.jar

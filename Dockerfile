FROM navikt/java:11-appdynamics
ENV APPD_ENABLED=true
COPY import-vault-token.sh /init-scripts
COPY /target/sykefravarsstatistikk-api-0.0.1-SNAPSHOT.jar app.jar

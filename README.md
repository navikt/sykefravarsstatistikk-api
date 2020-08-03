Sykefraværsstatistikk api
================

Denne appen eksponerer statistikk om sykefravær.
 Arbeidsgivere skal kunne se sitt eget sykefravær sammenlignet med sykefraværet i egen bransje, sektor og i hele landet.
 Hensikten er å gjøre arbeidsgivere mer engasjert i eget sykefravær.

# Komme i gang

Koden kan kjøres som en vanlig Spring Boot-applikasjon fra SykefraværsstatistikkApplication.
 Åpnes i browser: [http://localhost:8080/sykefravarsstatistikk-api/internal/healthcheck](http://localhost:8080/sykefravarsstatistikk-api/internal/healthcheck)

 Default spring-profil er local, og da er alle avhengigheter mocket på localhost:8081. 

## Docker
Bygg image
`docker build -t sykefravarsstatistikk-api .`

Kjør container
`docker run -d -p 8080:8080 sykefravarsstatistikk-api`

## Koble til H2-database lokalt
Åpne H2-konsollen på `http://localhost:8080/sykefravarsstatistikk-api/h2` og fyll inn det som står under `applikasjon.datasource` i `application.yaml`.

---

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan rettes mot:

* Thomas Dufourd, thomas.dufourd@nav.no
* Lars Andreas Tveiten, lars.andreas.van.woensel.kooy.tveiten@nav.no
* Malaz Alkoj, malaz.alkoj@nav.no

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #arbeidsgiver-teamia.

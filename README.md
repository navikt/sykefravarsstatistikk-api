Sykefraværsstatistikk api
================

Denne appen eksponerer statistikk om sykefravær.
Arbeidsgivere skal kunne se sitt eget sykefravær sammenlignet med sykefraværet i egen bransje,
sektor og i hele landet.
Hensikten er å gjøre arbeidsgivere mer engasjert i eget sykefravær.

[![Build, push & deploy](https://github.com/navikt/sykefravarsstatistikk-api/actions/workflows/build-deploy.yaml/badge.svg?branch=master)](https://github.com/navikt/sykefravarsstatistikk-api/actions/workflows/build-deploy.yaml)


# Kjøre lokalt i docker compose

Pass på at docker-compose er satt opp og at docker kjører. Appen burde da svare på `http://localhost:8080/sykefravarsstatistikk-api/internal/readiness`

Stå i rotmappa til prosjeketet og: 

- Kjør opp avhengigheter med `docker-compose up -d`.
- Kjør opp appen med `SPRING_PROFILES_ACTIVE=compose mvn clean spring-boot:run` (evt kjør i eclipse med aktiv profil = compose) 

Hent ut token:

```bash
curl -H "Content-Type: application/x-www-form-urlencoded" --request POST  \
  -d grant_type="client_credentials"                                      \
  -d client_id="localhost:arbeidsgiver:ia-tjenester-metrikker"            \
  -d client_secret="client_secret"                                        \
  "http://localhost:6969/tokenx/token" | jq -r '.access_token' | pbcopy
```

Nå kan du kjøre request mot appen med tokenet i clipboardet.

Noen eksempel requests:

```bash
curl -X GET \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $(pbpaste)" \
  "http://localhost:8080/sykefravarsstatistikk-api/811076112/sykefravarshistorikk/kvartalsvis"
```


# Komme i gang

Koden kan kjøres som en vanlig Spring Boot-applikasjon fra SykefraværsstatistikkApplication. Du må
ha kjørende [naisdevice](https://doc.nais.io/device/) for at appen skal fungere.

Default spring-profil er local, og da er alle avhengigheter mocket på localhost:8081.

Koden er formatert med Google Java Format. Anbefaler å installere Google Java Format plugin for
IntelliJ. På et tidspunkt ble hele kodebasen formatert, og disse formateringscommitene ligger
i `.git-blame-ignore-revs`. For at `git blame` (og dermed f.eks. IntelliJs `Annotate`) skal ignorere
dem, endre git config med følgende kommando:

```
git config blame.ignoreRevsFile .git-blame-ignore-revs
```

## Koble til H2-database lokalt

Åpne H2-konsollen på `http://localhost:8080/sykefravarsstatistikk-api/h2` og fyll inn det som står
under `applikasjon.datasource` i `application.yaml`:

```
url: jdbc:h2:mem:db-local;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
username: SA
password:
driver-class-name: org.h2.Driver
```

## Docker

Bygg image
`docker build -t sykefravarsstatistikk-api .`

Kjør container
`docker run -d -p 8080:8080 sykefravarsstatistikk-api`

---

# Kontakt

* For spørsmål eller henvendelser, opprett gjerne et issue her på GitHub.
* Koden utvikles og driftes av Team IA i [Produktområde arbeidsgiver](https://navno.sharepoint.com/sites/intranett-prosjekter-og-utvikling/SitePages/Produktomr%C3%A5de-arbeidsgiver.aspx)
* Slack-kanal [#team-pia](https://nav-it.slack.com/archives/C02DL347ZT2)

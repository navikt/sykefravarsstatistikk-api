Sykefraværsstatistikk api
================

Denne appen eksponerer statistikk om sykefravær.
Arbeidsgivere skal kunne se sitt eget sykefravær sammenlignet med sykefraværet i egen bransje,
sektor og i hele landet.
Hensikten er å gjøre arbeidsgivere mer engasjert i eget sykefravær.

# Komme i gang

Koden kan kjøres som en vanlig Spring Boot-applikasjon fra SykefraværsstatistikkApplication. Du må
ha kjørende [naisdevice](https://doc.nais.io/device/) for at appen skal fungere.

Helsesjekk (åpnes i
browser): [http://localhost:8080/sykefravarsstatistikk-api/internal/healthcheck](http://localhost:8080/sykefravarsstatistikk-api/internal/healthcheck)

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

## Grafisk fremstilling av API-ene (swagger-ui)

API-et kan sees og testes på:

* Lokalt: `http://localhost:8080/sykefravarsstatistikk-api/swagger-ui/index.html`
* På server:
    - `{host}/sykefravarsstatistikk-api/swagger-ui/index.html?configUrl=/sykefravarsstatistikk-api/v3/api-docs/swagger-config#/`
    - `{host}/sykefravarsstatistikk-api/swagger-ui/index.html` og lim
      inn `/sykefravarsstatistikk-api/v3/api-docs` i __Explore__ søkefelt

## Local Auth

`GET` request som la deg hente en Cookie som kan brukes lokalt finner du i
swagger `mock-login-controller`

Bruk `try it out` og fyll ut følgende parametre i GUI:

* `issuerId: selvbetjening`
* `audience: someaudience`
* `cookiename: selvbetjening-idtoken`

Kjør `execute`. Etter det blir cookie `selvbetjening-idtoken` satt opp og du kan kjøre andre
requests i Swagger GUI.

## Docker

Bygg image
`docker build -t sykefravarsstatistikk-api .`

Kjør container
`docker run -d -p 8080:8080 sykefravarsstatistikk-api`

---

# Henvendelser

## For Nav-ansatte

* Dette Git-repositoriet eies
  av [Team IA i Produktområde Arbeidsgiver](https://navno.sharepoint.com/sites/intranett-prosjekter-og-utvikling/SitePages/Produktomr%C3%A5de-arbeidsgiver.aspx).
* Slack-kanaler:
    * [#arbeidsgiver-teamia-utvikling](https://nav-it.slack.com/archives/C016KJA7CFK)
    * [#arbeidsgiver-general](https://nav-it.slack.com/archives/CCM649PDH)
    * [#arbeidsgiver-utvikling](https://nav-it.slack.com/archives/CD4MES6BB)

## For folk utenfor Nav

* Opprett gjerne en issue i Github for alle typer spørsmål
* IT-utviklerne i Github-teamet https://github.com/orgs/navikt/teams/arbeidsgiver
* IT-avdelingen
  i [Arbeids- og velferdsdirektoratet](https://www.nav.no/no/NAV+og+samfunn/Kontakt+NAV/Relatert+informasjon/arbeids-og-velferdsdirektoratet-kontorinformasjon)

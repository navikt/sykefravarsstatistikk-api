Sykefraværsstatistikk api
================

Denne appen eksponerer statistikk om sykefravær.
 Arbeidsgivere skal kunne se sitt eget sykefravær sammenlignet med sykefraværet i egen bransje, sektor og i hele landet.
 Hensikten er å gjøre arbeidsgivere mer engasjert i eget sykefravær.

# Komme i gang

Koden kan kjøres som en vanlig Spring Boot-applikasjon fra SykefraværsstatistikkApplication. Du må ha kjørende [naisdevice](https://doc.nais.io/device/) for at appen skal fungere.

Helsesjekk (åpnes i browser): [http://localhost:8080/sykefravarsstatistikk-api/internal/healthcheck](http://localhost:8080/sykefravarsstatistikk-api/internal/healthcheck)

Default spring-profil er local, og da er alle avhengigheter mocket på localhost:8081.

## Koble til H2-database lokalt
Åpne H2-konsollen på `http://localhost:8080/sykefravarsstatistikk-api/h2` og fyll inn det som står under `applikasjon.datasource` i `application.yaml`.

## Grafisk fremstilling av API-ene (swagger-ui)
API-et kan sees og testes på: 
 * Lokalt: `http://localhost:8080/sykefravarsstatistikk-api/swagger-ui/index.html` 
 * På server:
   - `{host}/sykefravarsstatistikk-api/swagger-ui/index.html?configUrl=/sykefravarsstatistikk-api/v3/api-docs/swagger-config#/`
   - `{host}/sykefravarsstatistikk-api/swagger-ui/index.html` og lim inn `/sykefravarsstatistikk-api/v3/api-docs` i __Explore__ søkefelt

## Docker
Bygg image
`docker build -t sykefravarsstatistikk-api .`

Kjør container
`docker run -d -p 8080:8080 sykefravarsstatistikk-api`
---------

# Henvendelser

## For Nav-ansatte
* Dette Git-repositoriet eies av [Team IA i Produktområde arbeidsgiver](https://navno.sharepoint.com/sites/intranett-prosjekter-og-utvikling/SitePages/Produktomr%C3%A5de-arbeidsgiver.aspx).
* Slack-kanaler:
 * [#arbeidsgiver-teamia-utvikling](https://nav-it.slack.com/archives/C016KJA7CFK)
 * [#arbeidsgiver-general](https://nav-it.slack.com/archives/CCM649PDH)
 * [#arbeidsgiver-utvikling](https://nav-it.slack.com/archives/CD4MES6BB)

## For folk utenfor Nav
* Opprett gjerne en issue i Github for alle typer spørsmål
* IT-utviklerne i Github-teamet https://github.com/orgs/navikt/teams/arbeidsgiver
* IT-avdelingen i [Arbeids- og velferdsdirektoratet](https://www.nav.no/no/NAV+og+samfunn/Kontakt+NAV/Relatert+informasjon/arbeids-og-velferdsdirektoratet-kontorinformasjon)

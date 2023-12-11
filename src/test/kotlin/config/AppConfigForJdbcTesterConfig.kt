package config

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.AggregertStatistikkService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringMetadataVirksomhetService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringPerStatistikkKategoriService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.VirksomhetMetadataService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.SykefraværsstatistikkImporteringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangsstyring.TilgangskontrollService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangsstyring.TokenService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.WebClientConfiguration
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.PrometheusMetrics
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api.InternalController
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api.OrganisasjonerController
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api.SykefraværshistorikkController
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron.EksportAvGradertStatistikkCron
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron.FjernStatistikkEldreEnnFemÅrCron
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron.ImportOgEksportAvEnkeltkvartalerCron
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron.ImporterOgEksporterStatistikkCron
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.EnhetsregisteretClient
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.KafkaClient
import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType

/**
 * Med denne configfilen skal Spring kunne sette opp en applikasjon som bare inneholder de
 * nødvendige beans for Jdbc-testing Alt annet (servlet, filters, RestTemplate, ...osv) blir ikke
 * opprettet
 */
@SpringBootConfiguration
@ComponentScan(
    basePackages = ["no.nav.arbeidsgiver"],
    excludeFilters = [ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        value = [
            TokenService::class,
            EksporteringService::class,
            EksporteringPerStatistikkKategoriService::class,
            ImporterOgEksporterStatistikkCron::class,
            SykefraværsstatistikkImporteringService::class,
            VirksomhetMetadataService::class,
            OrganisasjonerController::class,
            SykefraværshistorikkController::class,
            OrganisasjonerController::class,
            TilgangskontrollService::class,
            AggregertStatistikkService::class,
            EksporteringMetadataVirksomhetService::class,
            InternalController::class,
            EnhetsregisteretClient::class,
            WebClientConfiguration::class,
            PrometheusMetrics::class,
            KafkaClient::class,
            ImportOgEksportAvEnkeltkvartalerCron::class,
            FjernStatistikkEldreEnnFemÅrCron::class,
            EksportAvGradertStatistikkCron::class]
    )]
)
open class AppConfigForJdbcTesterConfig

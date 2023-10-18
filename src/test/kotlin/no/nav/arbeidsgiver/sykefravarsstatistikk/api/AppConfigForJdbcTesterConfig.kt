package no.nav.arbeidsgiver.sykefravarsstatistikk.api

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.PostImporteringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.TilgangskontrollService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregering.AggregertStatistikkService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringMetadataVirksomhetService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringPerStatistikkKategoriService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importering.SykefraværsstatistikkImporteringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.TokenService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.WebClientConfiguration
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.MockServer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.PrometheusMetrics
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api.InternalController
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api.OrganisasjonerController
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api.SykefraværshistorikkController
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.EnhetsregisteretClient
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.KafkaClient
import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.data.jdbc.repository.config.JdbcRepositoryConfigExtension

/**
 * Med denne configfilen skal Spring kunne sette opp en applikasjon som bare inneholder de
 * nødvendige beans for Jdbc-testing Alt annet (servlet, filters, RestTemplate, ...osv) blir ikke
 * opprettet
 */
@SpringBootConfiguration
@EnableJdbcRepositories("no.nav.arbeidsgiver")
@ComponentScan(
    basePackages = ["no.nav.arbeidsgiver"],
    excludeFilters = [ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        value = [MockServer::class,
            TokenService::class,
            EksporteringService::class,
            EksporteringPerStatistikkKategoriService::class,
            ImporteringScheduler::class,
            SykefraværsstatistikkImporteringService::class,
            PostImporteringService::class,
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
            EksportAvEnkeltkvartalerCron::class,
            FjernStatistikkEldreEnnFemÅrCron::class,
            EksportAvGradertStatistikkCron::class]
    )]
)
open class AppConfigForJdbcTesterConfig : JdbcRepositoryConfigExtension()

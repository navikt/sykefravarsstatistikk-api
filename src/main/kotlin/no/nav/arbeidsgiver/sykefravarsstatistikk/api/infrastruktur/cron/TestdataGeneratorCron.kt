package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import arrow.core.getOrElse
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import net.javacrumbs.shedlock.core.LockConfiguration
import net.javacrumbs.shedlock.core.LockingTaskExecutor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringMetadataVirksomhetService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringPerStatistikkKategoriService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.VirksomhetMetadataService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.SykefraværsstatistikkImporteringService
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES
import org.springframework.scheduling.annotation.Scheduled

@Component
class TestdataGeneratorCron(
    registry: MeterRegistry,
    private val taskExecutor: LockingTaskExecutor,
    private val sykefraværsstatistikkImporteringService: SykefraværsstatistikkImporteringService,
    private val eksporteringPerStatistikkKategoriService: EksporteringPerStatistikkKategoriService,
    private val eksporteringMetadataVirksomhetService: EksporteringMetadataVirksomhetService,
    private val virksomhetMetadataService: VirksomhetMetadataService,
    private val environment: Environment,
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val noeFeiletCounter: Counter = registry.counter("sykefravarstatistikk_import_eller_eksport_testdata_feilet")

    private val kvartalSomSkalEksporteres = ÅrstallOgKvartal(2024, 1)

    @Scheduled(cron = "0 25 09 14 06 ?")
    fun scheduledManuellImportCron() {
        val lockAtMostFor = Duration.of(30, MINUTES)
        val lockAtLeastFor = Duration.of(1, MINUTES)
        taskExecutor.executeWithLock(
            Runnable { gjennomførJobb() },
            LockConfiguration(Instant.now(), "importering", lockAtMostFor, lockAtLeastFor)
        )
    }

    fun gjennomførJobb() {
        if (!environment.activeProfiles.contains("dev")) {
            log.warn("Denne skal bare kjøres i dev miljø, avslutter.")
            return
        }

        log.info("Starter generering av testdata for kvartal: '$kvartalSomSkalEksporteres'")
        log.info("1- Import av statistikk for kvartal '$kvartalSomSkalEksporteres'")
        sykefraværsstatistikkImporteringService.importSykefraværsstatistikkVirksomhet(årstallOgKvartal = kvartalSomSkalEksporteres)
        sykefraværsstatistikkImporteringService.importSykefraværsstatistikkVirksomhetMedGradering(årstallOgKvartal = kvartalSomSkalEksporteres)

        log.info("2- Import av metadata for kvartal '$kvartalSomSkalEksporteres'")
        virksomhetMetadataService.overskrivMetadataForVirksomheter(kvartalSomSkalEksporteres)
            .getOrElse {
                noeFeiletCounter.increment()
                return
            }
        log.info("3- Eksport av metadata for kvartal '$kvartalSomSkalEksporteres'")
        eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(kvartalSomSkalEksporteres)
            .getOrElse {
                noeFeiletCounter.increment()
                return
            }
        log.info("Eksportering av metadata for kvartal '$kvartalSomSkalEksporteres' er fullført")

        val kategorier = listOf(Statistikkategori.VIRKSOMHET, Statistikkategori.VIRKSOMHET_GRADERT)

        kategorier.forEach { kategori ->
            log.info("4- Eksport av statistikk for kategori '$kategori' for kvartal '$kvartalSomSkalEksporteres'")
            runCatching {
                eksporteringPerStatistikkKategoriService.eksporterPerStatistikkKategori(
                    kvartalSomSkalEksporteres,
                    kategori
                )
            }.getOrElse {
                log.error("Eksport av kategori '$kategori' feilet", it)
                noeFeiletCounter.increment()
                return
            }
            log.info("Eksport av statistikk '$kategori' for kvartal '$kvartalSomSkalEksporteres' er ferdig")
        }

        log.info("Generering av testdata  er ferdig for kvartal '$kvartalSomSkalEksporteres'")
    }
}

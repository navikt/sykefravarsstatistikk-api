package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import arrow.core.getOrElse
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import net.javacrumbs.shedlock.core.LockConfiguration
import net.javacrumbs.shedlock.core.LockingTaskExecutor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.PostImporteringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ImportEksportJobb
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ImportEksportJobb.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringMetadataVirksomhetService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringPerStatistikkKategoriService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importering.SykefraværsstatistikkImporteringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.ImportEksportStatusRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES

@Component
class ImporteringScheduler(
    registry: MeterRegistry,
    private val taskExecutor: LockingTaskExecutor,
    private val importeringService: SykefraværsstatistikkImporteringService,
    private val importEksportStatusRepository: ImportEksportStatusRepository,
    private val postImporteringService: PostImporteringService,
    private val eksporteringsService: EksporteringService,
    private val eksporteringPerStatistikkKategoriService: EksporteringPerStatistikkKategoriService,
    private val eksporteringMetadataVirksomhetService: EksporteringMetadataVirksomhetService,
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val vellykketImportCounter: Counter
    private val vellykketEksportCounter: Counter
    private val noeFeilet: Counter

    init {
        vellykketImportCounter = registry.counter("sykefravarstatistikk_vellykket_import")
        vellykketEksportCounter = registry.counter("sykefravarstatistikk_vellykket_eksport")
        noeFeilet = registry.counter("sykefravarstatistikk_import_eller_eksport_feilet")
    }

    @Scheduled(cron = "0 5 8 * * ?")
    fun scheduledImporteringOgEksportering() {
        val lockAtMostFor = Duration.of(30, MINUTES)
        val lockAtLeastFor = Duration.of(1, MINUTES)
        taskExecutor.executeWithLock(
            Runnable { gjennomførImportOgEksport() },
            LockConfiguration(Instant.now(), "importering", lockAtMostFor, lockAtLeastFor)
        )
    }

    fun gjennomførImportOgEksport() {
        log.info("Jobb for å importere sykefraværsstatistikk er startet.")
        val gjeldendeKvartal = importeringService.importerHvisDetFinnesNyStatistikk()

        val fullførteJobber = importEksportStatusRepository.hentFullførteJobber(gjeldendeKvartal)
        log.info("Listen over fullførte jobber dette kvartalet: ${fullførteJobber.joinToString()}")

        if (fullførteJobber.manglerJobben(IMPORTERT_STATISTIKK)) {
            importEksportStatusRepository.leggTilFullførtJobb(IMPORTERT_STATISTIKK, gjeldendeKvartal)
        }

        if (fullførteJobber.manglerJobben(IMPORTERT_VIRKSOMHETDATA)) {
            postImporteringService.overskrivMetadataForVirksomheter(gjeldendeKvartal)
                .getOrElse {
                    noeFeilet.increment()
                    return
                }
            importEksportStatusRepository.leggTilFullførtJobb(IMPORTERT_VIRKSOMHETDATA, gjeldendeKvartal)
        }

        if (fullførteJobber.manglerJobben(IMPORTERT_NÆRINGSKODEMAPPING)) {
            postImporteringService.overskrivNæringskoderForVirksomheter(gjeldendeKvartal)
                .getOrElse {
                    noeFeilet.increment()
                    return
                }
            importEksportStatusRepository.leggTilFullførtJobb(IMPORTERT_NÆRINGSKODEMAPPING, gjeldendeKvartal)
        }

        log.info("Inkrementerer counter 'sykefravarstatistikk_vellykket_import'")
        vellykketImportCounter.increment()

        if (fullførteJobber.manglerJobben(FORBEREDT_NESTE_EKSPORT_LEGACY)) {
            postImporteringService.forberedNesteEksport(gjeldendeKvartal, true)
                .getOrElse {
                    noeFeilet.increment()
                    return
                }
            importEksportStatusRepository.leggTilFullførtJobb(FORBEREDT_NESTE_EKSPORT_LEGACY, gjeldendeKvartal)
        }

        if (fullførteJobber.manglerJobben(EKSPORTERT_LEGACY)) {
            eksporteringsService.legacyEksporter(gjeldendeKvartal)
                .getOrElse {
                    noeFeilet.increment()
                    return
                }
            importEksportStatusRepository.leggTilFullførtJobb(EKSPORTERT_LEGACY, gjeldendeKvartal)
        }

        if (fullførteJobber.manglerJobben(EKSPORTERT_METADATA_VIRKSOMHET)) {
            eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(gjeldendeKvartal)
                .getOrElse {
                    noeFeilet.increment()
                    return
                }
            importEksportStatusRepository.leggTilFullførtJobb(EKSPORTERT_METADATA_VIRKSOMHET, gjeldendeKvartal)
        }

        if (fullførteJobber.manglerJobben(EKSPORTERT_PER_STATISTIKKATEGORI)) {
            Statistikkategori.entries.forEach { kategori ->
                runCatching {
                    eksporteringPerStatistikkKategoriService.eksporterPerStatistikkKategori(
                        gjeldendeKvartal,
                        kategori
                    )
                }.getOrElse {
                    log.error("Eksport av kategori $kategori feilet", it)
                    noeFeilet.increment()
                    return
                }
            }
            importEksportStatusRepository.leggTilFullførtJobb(
                EKSPORTERT_PER_STATISTIKKATEGORI,
                gjeldendeKvartal
            )

            log.info("Listen over fullførte jobber dette kvartalet: $fullførteJobber")

            log.info("Inkrementerer counter 'sykefravarstatistikk_vellykket_eksport'")
            vellykketEksportCounter.increment()
        }
    }
}

fun List<ImportEksportJobb>.manglerJobben(jobb: ImportEksportJobb) = this.none { it == jobb }

package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import arrow.core.getOrElse
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import net.javacrumbs.shedlock.core.LockConfiguration
import net.javacrumbs.shedlock.core.LockingTaskExecutor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.PostImporteringService
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
    private val taskExecutor: LockingTaskExecutor,
    private val importeringService: SykefraværsstatistikkImporteringService,
    registry: MeterRegistry,
    private val importEksportStatusRepository: ImportEksportStatusRepository,
    private val postImporteringService: PostImporteringService,
    private val eksporteringsService: EksporteringService,
    private val eksporteringPerStatistikkKategoriService: EksporteringPerStatistikkKategoriService,
    private val eksporteringMetadataVirksomhetService: EksporteringMetadataVirksomhetService,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    private val counter: Counter

    init {
        counter = registry.counter("sykefravarstatistikk_vellykket_import")
    }

    //@Scheduled(cron = "0 5 8 * * ?")
    @Scheduled(fixedDelay = Long.MAX_VALUE)
    fun scheduledImportering() {
        val lockAtMostFor = Duration.of(10, MINUTES)
        val lockAtLeastFor = Duration.of(1, MINUTES)
        taskExecutor.executeWithLock(
            Runnable { importOgEksport() },
            LockConfiguration(Instant.now(), "importering", lockAtMostFor, lockAtLeastFor)
        )
    }

    fun importOgEksport() {
        log.info("Jobb for å importere sykefraværsstatistikk er startet.")
        val gjeldendeKvartal = importeringService.importerHvisDetFinnesNyStatistikk()

        val fullførteJobber = importEksportStatusRepository.hentFullførteJobber(gjeldendeKvartal)

        if (fullførteJobber.none { it == IMPORTERT_STATISTIKK }) {
            importEksportStatusRepository.leggTilFullførtJobb(IMPORTERT_STATISTIKK, gjeldendeKvartal)
        }

        if (fullførteJobber.none { it == IMPORTERT_VIRKSOMHETDATA }) {
            postImporteringService.overskrivMetadataForVirksomheter(gjeldendeKvartal)
                .getOrElse { return }
            importEksportStatusRepository.leggTilFullførtJobb(IMPORTERT_VIRKSOMHETDATA, gjeldendeKvartal)
        }

        if (fullførteJobber.none { it == IMPORTERT_NÆRINGSKODEMAPPING }) {
            postImporteringService.overskrivNæringskoderForVirksomheter(gjeldendeKvartal)
                .getOrElse { return }
            importEksportStatusRepository.leggTilFullførtJobb(IMPORTERT_NÆRINGSKODEMAPPING, gjeldendeKvartal)
        }

        if (fullførteJobber.none { it == FORBEREDT_NESTE_EKSPORT_LEGACY }) {
            postImporteringService.forberedNesteEksport(gjeldendeKvartal, true)
                .getOrElse { return }
            importEksportStatusRepository.leggTilFullførtJobb(FORBEREDT_NESTE_EKSPORT_LEGACY, gjeldendeKvartal)
        }

        if (fullførteJobber.none { it == EKSPORTERT_LEGACY }) {
            eksporteringsService.legacyEksporter(gjeldendeKvartal)
                .getOrElse { return }
            importEksportStatusRepository.leggTilFullførtJobb(EKSPORTERT_LEGACY, gjeldendeKvartal)
        }

        if (fullførteJobber.none { it == EKSPORTERT_METADATA_VIRKSOMHET }) {
            eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(gjeldendeKvartal)
                .getOrElse { return }
            importEksportStatusRepository.leggTilFullførtJobb(EKSPORTERT_METADATA_VIRKSOMHET, gjeldendeKvartal)
        }

        if (fullførteJobber.none { it == EKSPORTERT_PER_STATISTIKKATEGORI }) {
            Statistikkategori.entries.forEach {
                eksporteringPerStatistikkKategoriService.eksporterPerStatistikkKategori(gjeldendeKvartal, it)
            }
            importEksportStatusRepository.leggTilFullførtJobb(EKSPORTERT_PER_STATISTIKKATEGORI, gjeldendeKvartal)
        }

        log.info("Inkrementerer counter 'sykefravarstatistikk_vellykket_import'")
        counter.increment()
        log.info("Counter er nå: {}", counter.count())
    }
}

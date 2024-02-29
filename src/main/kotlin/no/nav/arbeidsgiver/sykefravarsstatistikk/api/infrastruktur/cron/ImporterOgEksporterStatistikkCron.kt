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
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.SykefraværsstatistikkImporteringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoer.PubliseringsdatoerService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron.ImportEksportJobb.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.ImportEksportStatusRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit.MINUTES

@Component
class ImporterOgEksporterStatistikkCron(
    registry: MeterRegistry,
    private val taskExecutor: LockingTaskExecutor,
    private val importEksportStatusRepository: ImportEksportStatusRepository,
    private val importeringService: SykefraværsstatistikkImporteringService,
    private val virksomhetMetadataService: VirksomhetMetadataService,
    private val eksporteringPerStatistikkKategoriService: EksporteringPerStatistikkKategoriService,
    private val eksporteringMetadataVirksomhetService: EksporteringMetadataVirksomhetService,
    private val publiseringsdatoerService: PubliseringsdatoerService,
    private val clock: Clock,
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val vellykketImportCounter: Counter = registry.counter("sykefravarstatistikk_vellykket_import")
    private val vellykketEksportCounter: Counter = registry.counter("sykefravarstatistikk_vellykket_eksport")
    private val noeFeilet: Counter = registry.counter("sykefravarstatistikk_import_eller_eksport_feilet")

    @Scheduled(cron = "0 40 10 * * ?")
    fun scheduledImporteringOgEksportering() {
        val lockAtMostFor = Duration.of(30, MINUTES)
        val lockAtLeastFor = Duration.of(1, MINUTES)
        taskExecutor.executeWithLock(
            Runnable { gjennomførImportOgEksport() },
            LockConfiguration(Instant.now(), "importering", lockAtMostFor, lockAtLeastFor)
        )
    }

    fun gjennomførImportOgEksport() {
        val publiseringsdatoer = publiseringsdatoerService.hentPubliseringsdatoer()

        val nestePubliseringsdato =
            publiseringsdatoer?.nestePubliseringsdato?.also { log.info("Neste publiseringsdato er $it") }
                ?: run {
                    log.error("Neste publiseringsdato er null, avbryter import og eksport. Er publiseringskalenderen i datavarehus oppdatert?")
                    noeFeilet.increment()
                    return
                }
        val gjeldendeKvartal = publiseringsdatoer.gjeldendePeriode
        val iDag = LocalDate.now(clock)

        log.info("Jobb for å importere sykefraværsstatistikk er startet.")
        log.info("Gjeldende kvartal er $gjeldendeKvartal")

        if (iDag >= nestePubliseringsdato) {
            log.info("Neste publiseringsdato $nestePubliseringsdato er nådd i dag $iDag, importerer statistikk.")
            importeringService.importerHvisDetFinnesNyStatistikk(gjeldendeKvartal)
        }

        val fullførteJobber = importEksportStatusRepository.hentFullførteJobber(gjeldendeKvartal)
        log.info("Listen over fullførte jobber dette kvartalet: ${fullførteJobber.joinToString()}")

        if (fullførteJobber.manglerJobben(IMPORTERT_STATISTIKK)) {
            importEksportStatusRepository.leggTilFullførtJobb(IMPORTERT_STATISTIKK, gjeldendeKvartal)

            log.info("Inkrementerer counter 'sykefravarstatistikk_vellykket_import'")
            vellykketImportCounter.increment()
        }

        if (fullførteJobber.manglerJobben(IMPORTERT_VIRKSOMHETDATA)) {
            virksomhetMetadataService.overskrivMetadataForVirksomheter(gjeldendeKvartal)
                .getOrElse {
                    noeFeilet.increment()
                    return
                }
            importEksportStatusRepository.leggTilFullførtJobb(IMPORTERT_VIRKSOMHETDATA, gjeldendeKvartal)
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


            log.info("Inkrementerer counter 'sykefravarstatistikk_vellykket_eksport'")
            vellykketEksportCounter.increment()
        }
        log.info(
            "Listen over fullførte jobber dette kvartalet: ${
                importEksportStatusRepository.hentFullførteJobber(
                    gjeldendeKvartal
                )
            }"
        )
    }
}

fun List<ImportEksportJobb>.manglerJobben(jobb: ImportEksportJobb) = this.none { it == jobb }

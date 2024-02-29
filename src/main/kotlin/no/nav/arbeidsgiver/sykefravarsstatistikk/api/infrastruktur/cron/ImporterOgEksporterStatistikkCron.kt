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


    @Scheduled(cron = FEM_OVER_ÅTTE_HVER_DAG)
    fun scheduledImporteringOgEksportering() {
        val lockAtMostFor = Duration.of(30, MINUTES)
        val lockAtLeastFor = Duration.of(1, MINUTES)
        taskExecutor.executeWithLock(
            Runnable { gjennomførImportOgEksport() },
            LockConfiguration(Instant.now(), "importering", lockAtMostFor, lockAtLeastFor)
        )
    }

    fun gjennomførImportOgEksport() {

        log.info("Jobb for å importere og eksportere sykefraværsstatistikk er startet")

        val publiseringskalender = publiseringsdatoerService.hentPubliseringsdatoer()

        val nestePubliseringsdato =
            publiseringskalender?.nestePubliseringsdato?.also { log.info("Neste publiseringsdato er $it") }
                ?: run {
                    log.error("Neste publiseringsdato er null, avbryter import og eksport. Er publiseringskalenderen i datavarehus oppdatert?")
                    noeFeilet.inkrementerOgLogg()
                    return
                }

        var gjeldendeKvartal = publiseringskalender.gjeldendePeriode

        if (iDag() >= nestePubliseringsdato) {
            log.info("Neste publiseringsdato $nestePubliseringsdato er nådd i dag (${iDag()})")
            gjeldendeKvartal = publiseringskalender.gjeldendePeriode.plussKvartaler(1)
        }

        val fullførteJobber = importEksportStatusRepository.hentFullførteJobber(gjeldendeKvartal).also {
            log.info("Listen over fullførte jobber dette kvartalet: ${it.joinToString()}")
        }

        if (fullførteJobber.oppfyllerKraveneTilÅStarte(IMPORTERT_STATISTIKK)) {
            importeringService.importerHvisDetFinnesNyStatistikk(gjeldendeKvartal).map {
                importEksportStatusRepository.leggTilFullførtJobb(IMPORTERT_STATISTIKK, gjeldendeKvartal)
                vellykketImportCounter.inkrementerOgLogg()
            }
        }

        if (fullførteJobber.oppfyllerKraveneTilÅStarte(IMPORTERT_VIRKSOMHETDATA)) {
            virksomhetMetadataService.overskrivMetadataForVirksomheter(gjeldendeKvartal)
                .getOrElse {
                    noeFeilet.inkrementerOgLogg()
                    return
                }
            importEksportStatusRepository.leggTilFullførtJobb(IMPORTERT_VIRKSOMHETDATA, gjeldendeKvartal)
        }

        if (fullførteJobber.oppfyllerKraveneTilÅStarte(EKSPORTERT_METADATA_VIRKSOMHET)) {
            eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(gjeldendeKvartal)
                .getOrElse {
                    noeFeilet.inkrementerOgLogg()
                    return
                }
            importEksportStatusRepository.leggTilFullførtJobb(EKSPORTERT_METADATA_VIRKSOMHET, gjeldendeKvartal)
        }

        if (fullførteJobber.oppfyllerKraveneTilÅStarte(EKSPORTERT_PER_STATISTIKKATEGORI)) {
            Statistikkategori.entries.forEach { kategori ->
                runCatching {
                    eksporteringPerStatistikkKategoriService.eksporterPerStatistikkKategori(
                        gjeldendeKvartal,
                        kategori
                    )
                }.getOrElse {
                    log.error("Eksport av kategori $kategori feilet", it)
                    noeFeilet.inkrementerOgLogg()
                    return
                }
            }

            importEksportStatusRepository.leggTilFullførtJobb(
                EKSPORTERT_PER_STATISTIKKATEGORI,
                gjeldendeKvartal
            )

            vellykketEksportCounter.inkrementerOgLogg()
        }

        log.info(
            "Listen over fullførte jobber dette kvartalet: ${
                importEksportStatusRepository.hentFullførteJobber(
                    gjeldendeKvartal
                )
            }"
        )
    }

    fun iDag(): LocalDate = LocalDate.now(clock)

    fun Counter.inkrementerOgLogg() {
        log.info("Inkrementerer Prometheus-telleren ${this.id.name} til ${this.count()}")
        this.increment()
    }

    companion object {
        const val FEM_OVER_ÅTTE_HVER_DAG = "0 5 8 * * ?"
    }
}


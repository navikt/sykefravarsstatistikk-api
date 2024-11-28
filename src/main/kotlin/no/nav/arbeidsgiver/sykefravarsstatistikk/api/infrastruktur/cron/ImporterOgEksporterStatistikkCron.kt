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


    @Scheduled(cron = TI_OVER_HALV_NI_HVER_DAG)
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

        val gjeldendeKvartal: ÅrstallOgKvartal?

        if (iDag() >= nestePubliseringsdato) {
            log.info("Neste publiseringsdato $nestePubliseringsdato er nådd i dag (${iDag()})")
            gjeldendeKvartal = publiseringskalender.gjeldendePeriode.plussKvartaler(1)
            log.info("Neste publiseringsdato er nådd. Gjeldende kvartal blir nå $gjeldendeKvartal")
        } else {
            gjeldendeKvartal = publiseringskalender.gjeldendePeriode
            log.info("Neste publiseringsdato er ikke nådd. Gjeldende kvartal er fortsatt $gjeldendeKvartal")
        }

        if (kraveneErOppfyltForÅStarte(IMPORTERT_STATISTIKK, gjeldendeKvartal)) {
            importeringService.importerHvisDetFinnesNyStatistikk(gjeldendeKvartal)
                .map {
                    log.info("Ny statistikk har blitt importert for $gjeldendeKvartal")
                    importEksportStatusRepository.leggTilFullførtJobb(IMPORTERT_STATISTIKK, gjeldendeKvartal)
                    vellykketImportCounter.inkrementerOgLogg()
                }.mapLeft {
                    log.info("Import ble ikke gjennomført for $gjeldendeKvartal, det ble ikke funnet noen ny statistikk i DVH")
                }
        }

        if (kraveneErOppfyltForÅStarte(IMPORTERT_VIRKSOMHETDATA, gjeldendeKvartal)) {
            virksomhetMetadataService.overskrivMetadataForVirksomheter(gjeldendeKvartal)
                .getOrElse {
                    noeFeilet.inkrementerOgLogg()
                    return
                }
            importEksportStatusRepository.leggTilFullførtJobb(IMPORTERT_VIRKSOMHETDATA, gjeldendeKvartal)
        }

        if (kraveneErOppfyltForÅStarte(EKSPORTERT_METADATA_VIRKSOMHET, gjeldendeKvartal)) {
            eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(gjeldendeKvartal)
                .getOrElse {
                    noeFeilet.inkrementerOgLogg()
                    return
                }
            importEksportStatusRepository.leggTilFullførtJobb(EKSPORTERT_METADATA_VIRKSOMHET, gjeldendeKvartal)
        }

        if (kraveneErOppfyltForÅStarte(EKSPORTERT_PER_STATISTIKKATEGORI, gjeldendeKvartal)) {
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
            "Jobb for å importere og eksportere statistikk har fullført. Listen over fullførte jobber er nå: ${
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

    fun kraveneErOppfyltForÅStarte(jobb: ImportEksportJobb, kvartal: ÅrstallOgKvartal): Boolean {
        log.info("Sjekker om kravene er oppfylt for å starte jobben '${jobb.name}' i kvartal '$kvartal'")

        return importEksportStatusRepository.hentFullførteJobber(kvartal).also {
            log.info("Listen over fullførte jobber er nå: ${it.joinToString()}")
        }.oppfyllerKraveneTilÅStarte(jobb)
    }

    companion object {
        const val FEM_OVER_ÅTTE_HVER_DAG = "0 5 8 * * ?"
        const val TI_OVER_NI_HVER_DAG = "0 10 9 * * ?"
        const val TI_OVER_HALV_NI_HVER_DAG = "0 40 8 * * ?"
    }
}


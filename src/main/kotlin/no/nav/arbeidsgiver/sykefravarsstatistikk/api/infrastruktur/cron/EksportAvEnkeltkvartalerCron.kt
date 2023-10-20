package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import arrow.core.getOrElse
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import net.javacrumbs.shedlock.core.LockConfiguration
import net.javacrumbs.shedlock.core.LockingTaskExecutor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.VirksomhetMetadataService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringMetadataVirksomhetService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringPerStatistikkKategoriService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES

@Component
class EksportAvEnkeltkvartalerCron(
    registry: MeterRegistry,
    private val taskExecutor: LockingTaskExecutor,
    private val eksporteringMetadataVirksomhetService: EksporteringMetadataVirksomhetService,
    private val eksporteringPerStatistikkKategoriService: EksporteringPerStatistikkKategoriService,
    private val virksomhetMetadataService: VirksomhetMetadataService,
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val noeFeiletCounter: Counter


    init {
        noeFeiletCounter = registry.counter("sykefravarstatistikk_import_eller_eksport_feilet")
    }

    // Fjern scheduleringen etter at jobben har kjørt ÉN gang
    // Cron jobb that runs at 12 once a year
    @Scheduled(cron = "0 0 12 6 10 ?")
    fun scheduledEksportAvEnkeltkvartal() {
        val fraKvartal = ÅrstallOgKvartal(2019, 1)
        val tilKvartal = ÅrstallOgKvartal(2023, 2)
        val kategorier = listOf(Statistikkategori.BRANSJE)

        val lockAtMostFor = Duration.of(30, MINUTES)
        val lockAtLeastFor = Duration.of(1, MINUTES)
        taskExecutor.executeWithLock(
            Runnable { gjennomførJobb(fraKvartal, tilKvartal, kategorier) },
            LockConfiguration(Instant.now(), "importering", lockAtMostFor, lockAtLeastFor)
        )
    }

    fun gjennomførJobb(fraKvartal: ÅrstallOgKvartal, tilKvartal: ÅrstallOgKvartal, kategorier: List<Statistikkategori>) {
        for (kvartal in ÅrstallOgKvartal.range(fraKvartal, tilKvartal)) {

            log.info("EksportAvEnkeltkvartaler har startet for $kvartal")

            virksomhetMetadataService.overskrivMetadataForVirksomheter(kvartal)
                .getOrElse {
                    noeFeiletCounter.increment()
                    return
                }

            virksomhetMetadataService.overskrivNæringskoderForVirksomheter(kvartal)
                .getOrElse {
                    noeFeiletCounter.increment()
                    return
                }

            eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(kvartal)
                .getOrElse {
                    noeFeiletCounter.increment()
                    return
                }

            kategorier.forEach { kategori ->
                runCatching {
                    eksporteringPerStatistikkKategoriService.eksporterPerStatistikkKategori(
                        kvartal,
                        kategori
                    )
                }.getOrElse {
                    log.error("Eksport av kategori $kategori feilet", it)
                    noeFeiletCounter.increment()
                    return
                }
            }

            log.info("EksportAvEnkeltkvartaler er ferdig for $kvartal")
        }
    }
}

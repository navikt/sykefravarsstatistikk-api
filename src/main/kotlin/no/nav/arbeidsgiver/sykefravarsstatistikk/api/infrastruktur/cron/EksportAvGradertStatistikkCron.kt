package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import net.javacrumbs.shedlock.core.LockConfiguration
import net.javacrumbs.shedlock.core.LockingTaskExecutor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringPerStatistikkKategoriService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES

@Component
class EksportAvGradertStatistikkCron(
    registry: MeterRegistry,
    private val taskExecutor: LockingTaskExecutor,
    private val eksporteringPerStatistikkKategoriService: EksporteringPerStatistikkKategoriService,
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val noeFeiletCounter: Counter

    init {
        noeFeiletCounter = registry.counter("sykefravarstatistikk_import_eller_eksport_feilet")
    }

    // Fjern scheduleringen etter at jobben har kjørt ÉN gang
    // Cron jobb that runs at 12 once a year
    @Scheduled(cron = "0 45 9 19 10 ?")
    fun scheduledEksportAvGradertStatistikk() {
        val fraKvartal = ÅrstallOgKvartal(2019, 1)
        val tilKvartal = ÅrstallOgKvartal(2023, 2)

        val lockAtMostFor = Duration.of(30, MINUTES)
        val lockAtLeastFor = Duration.of(1, MINUTES)
        taskExecutor.executeWithLock(
            Runnable { gjennomførJobb(fraKvartal, tilKvartal) },
            LockConfiguration(Instant.now(), "eksport-gradert-statistikk", lockAtMostFor, lockAtLeastFor)
        )
    }

    fun gjennomførJobb(fraKvartal: ÅrstallOgKvartal, tilKvartal: ÅrstallOgKvartal) {
        for (kvartal in ÅrstallOgKvartal.range(fraKvartal, tilKvartal)) {

            log.info("eksport av gradert statistikk har startet for $kvartal")

            val kategori = Statistikkategori.VIRKSOMHET_GRADERT

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


            log.info("eksport av gradert statistikk er ferdig for $kvartal")
        }
    }
}

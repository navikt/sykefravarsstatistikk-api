package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import net.javacrumbs.shedlock.core.LockConfiguration
import net.javacrumbs.shedlock.core.LockingTaskExecutor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringPerStatistikkKategoriService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
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
    private val noeFeiletCounter: Counter = registry.counter("sykefravarstatistikk_import_eller_eksport_feilet")

    @Scheduled(cron = "-")
    fun scheduledEksportAvGradertStatistikk() {
        val tilKvartal = ÅrstallOgKvartal(2023, 2)

        val fireÅrMedKvartaler = tilKvartal inkludertTidligere 4 * 4 - 1

        val lockAtMostFor = Duration.of(30, MINUTES)
        val lockAtLeastFor = Duration.of(1, MINUTES)
        taskExecutor.executeWithLock(
            Runnable { gjennomførJobb(fireÅrMedKvartaler) },
            LockConfiguration(Instant.now(), "eksport-gradert-statistikk", lockAtMostFor, lockAtLeastFor)
        )
    }

    fun gjennomførJobb(kvartaler: List<ÅrstallOgKvartal>) {
        log.info("Eksport av gradert statistikk har startet")
        for (kvartal in kvartaler) {

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
        log.info("Eksport av gradert statistikk er ferdig")
    }
}

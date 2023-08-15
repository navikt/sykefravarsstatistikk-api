package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import net.javacrumbs.shedlock.core.LockConfiguration
import net.javacrumbs.shedlock.core.LockingTaskExecutor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importering.Importeringstatus
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importering.SykefraværsstatistikkImporteringService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class ImporteringScheduler(
    private val taskExecutor: LockingTaskExecutor,
    private val importeringService: SykefraværsstatistikkImporteringService,
    registry: MeterRegistry
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    private val counter: Counter

    init {
        counter = registry.counter("sykefravarstatistikk_vellykket_import")
    }

    @Scheduled(cron = "0 5 8 * * ?")
    fun scheduledImportering() {
        val lockAtMostFor = Duration.of(10, ChronoUnit.MINUTES)
        val lockAtLeastFor = Duration.of(1, ChronoUnit.MINUTES)
        taskExecutor.executeWithLock(
            Runnable { importering() },
            LockConfiguration(Instant.now(), "importering", lockAtMostFor, lockAtLeastFor)
        )
    }

    private fun importering() {
        log.info("Jobb for å importere sykefraværsstatistikk er startet.")
        val importeringstatus = importeringService.importerHvisDetFinnesNyStatistikk()
        if (importeringstatus == Importeringstatus.IMPORTERT) {
            log.info("Inkrementerer counter 'sykefravarstatistikk_vellykket_import'")
            counter.increment()
            log.info("Counter er nå: {}", counter.count())
        }
    }
}

package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import lombok.extern.slf4j.Slf4j
import net.javacrumbs.shedlock.core.LockConfiguration
import net.javacrumbs.shedlock.core.LockingTaskExecutor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.PubliseringsdatoerImportService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
@Slf4j
class PubliseringdatoInfoImportScheduler(
    private val taskExecutor: LockingTaskExecutor, private val importService: PubliseringsdatoerImportService
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Scheduled(cron = "0 0 2 * * ?")
    fun scheduledImport() {
        val lockAtMostFor = Duration.of(10, ChronoUnit.MINUTES)
        val lockAtLeastFor = Duration.of(1, ChronoUnit.MINUTES)
        taskExecutor.executeWithLock(
            Runnable { importer() },
            LockConfiguration(Instant.now(), "publiseringsdatoer", lockAtMostFor, lockAtLeastFor)
        )
    }

    private fun importer() {
        log.info("Jobb for å importere publiseringsdatoer fra datavarehus er startet.")
        importService.importerDatoerFraDatavarehus()
        log.info("Jobb for å importere publiseringsdatoer er avsluttet.")
    }
}

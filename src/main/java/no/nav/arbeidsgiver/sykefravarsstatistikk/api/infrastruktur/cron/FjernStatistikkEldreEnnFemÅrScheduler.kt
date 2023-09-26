package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import net.javacrumbs.shedlock.core.LockConfiguration
import net.javacrumbs.shedlock.core.LockingTaskExecutor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES

@Component
class FjernStatistikkEldreEnnFemÅrScheduler(
    private val taskExecutor: LockingTaskExecutor,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    // Fjern scheduleringen etter at jobben har kjørt ÉN gang
    //@Scheduled(fixedDelay = Long.MAX_VALUE, initialDelay = 60*1000)
    fun fjernStatistikkEldreEnnFemÅr() {
        val lockAtMostFor = Duration.of(30, MINUTES)
        val lockAtLeastFor = Duration.of(1, MINUTES)
        taskExecutor.executeWithLock(
            Runnable { gjennomførImportOgEksport() },
            LockConfiguration(Instant.now(), "importering", lockAtMostFor, lockAtLeastFor)
        )
    }

    fun gjennomførImportOgEksport() {
        log.info("FjernStatistikkEldreEnnFemÅrScheduler har startet")


    }
}
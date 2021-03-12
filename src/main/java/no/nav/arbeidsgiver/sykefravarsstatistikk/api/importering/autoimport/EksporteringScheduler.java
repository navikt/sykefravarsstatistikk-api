package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
public class EksporteringScheduler {
    private final LockingTaskExecutor taskExecutor;
    private final EksporteringService eksporteringService;

    public EksporteringScheduler(LockingTaskExecutor taskExecutor, EksporteringService eksporteringService
    ) {
        this.taskExecutor = taskExecutor;
        this.eksporteringService = eksporteringService;
    }

    @Scheduled(cron = "0 5 8 * * ?")
    public void scheduledImportering() {
        Duration lockAtMostFor = Duration.of(10, ChronoUnit.MINUTES);
        Duration lockAtLeastFor = Duration.of(1, ChronoUnit.MINUTES);

        taskExecutor.executeWithLock(
                (Runnable) this::eksportering,
                new LockConfiguration(Instant.now(), "importering", lockAtMostFor, lockAtLeastFor)
        );
    }

    private void eksportering() {
        log.info("Jobb for å ekportere sykefraværsstatistikk er startet.");
        eksporteringService.eksporterHvisDetFinnesNyStatistikk();
        // TODO gjør samme for andre typer data.

    }

}

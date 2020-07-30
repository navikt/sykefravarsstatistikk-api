package no.nav.arbeidsgiver.sykefravarsstatistikk.api.autoimport;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
public class ImporteringScheduler {
    private final LockingTaskExecutor taskExecutor;
    private final ImporteringService importeringService;

    public ImporteringScheduler(LockingTaskExecutor taskExecutor, ImporteringService importeringService) {
        this.taskExecutor = taskExecutor;
        this.importeringService = importeringService;
    }

    @Scheduled(cron = "* 5 8 * * ?")
    public void scheduledImportering() {
        Duration lockAtMostUntil = Duration.of(10, ChronoUnit.MINUTES);
        Duration lockAtLeastUntil = Duration.of(1, ChronoUnit.MINUTES);
        taskExecutor.executeWithLock(
                (Runnable) this::importering,
                new LockConfiguration("importering", lockAtMostUntil, lockAtLeastUntil)
        );
    }

    private void importering() {
        log.info("Jobb for å importere sykefraværsstatistikk er startet.");
        importeringService.importerHvisDetFinnesNyStatistikk();
    }

}

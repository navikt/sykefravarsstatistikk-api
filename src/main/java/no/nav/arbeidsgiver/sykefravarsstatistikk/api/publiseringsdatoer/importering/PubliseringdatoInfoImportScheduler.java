package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.importering;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PubliseringdatoInfoImportScheduler {

  private final LockingTaskExecutor taskExecutor;
  private final PubliseringsdatoerImportService importService;


  public PubliseringdatoInfoImportScheduler(LockingTaskExecutor taskExecutor,
      PubliseringsdatoerImportService importService
  ) {
    this.taskExecutor = taskExecutor;
    this.importService = importService;
  }


  @Scheduled(cron = "0 0 2 * * ?")
  public void scheduledImport() {
    Duration lockAtMostFor = Duration.of(10, ChronoUnit.MINUTES);
    Duration lockAtLeastFor = Duration.of(1, ChronoUnit.MINUTES);

    taskExecutor.executeWithLock(
        (Runnable) this::importer,
        new LockConfiguration(
            Instant.now(),
            "publiseringsdatoer",
            lockAtMostFor,
            lockAtLeastFor)
    );
  }


  private void importer() {
    log.info("Jobb for å importere publiseringsdatoer fra datavarehus er startet.");
    importService.importerDatoerFraDatavarehus();
    log.info("Jobb for å importere publiseringsdatoer er avsluttet.");
  }
}

package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importering.Importeringstatus;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importering.SykefraværsstatistikkImporteringService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ImporteringScheduler {

  private final LockingTaskExecutor taskExecutor;
  private final SykefraværsstatistikkImporteringService importeringService;
  private final Counter counter;

  public ImporteringScheduler(
      LockingTaskExecutor taskExecutor,
      SykefraværsstatistikkImporteringService importeringService,
      MeterRegistry registry) {
    this.taskExecutor = taskExecutor;
    this.importeringService = importeringService;
    this.counter = registry.counter("sykefravarstatistikk_vellykket_import");
  }

  @Scheduled(cron = "0 5 8 * * ?")
  public void scheduledImportering() {
    Duration lockAtMostFor = Duration.of(10, ChronoUnit.MINUTES);
    Duration lockAtLeastFor = Duration.of(1, ChronoUnit.MINUTES);

    taskExecutor.executeWithLock(
        (Runnable) this::importering,
        new LockConfiguration(Instant.now(), "importering", lockAtMostFor, lockAtLeastFor));
  }

  private void importering() {
    log.info("Jobb for å importere sykefraværsstatistikk er startet.");
    Importeringstatus importeringstatus = importeringService.importerHvisDetFinnesNyStatistikk();
    if (importeringstatus.equals(Importeringstatus.IMPORTERT)) {
      log.info("Inkrementerer counter 'sykefravarstatistikk_vellykket_import'");
      counter.increment();
      log.info("Counter er nå: {}", counter.count());
    }
  }
}
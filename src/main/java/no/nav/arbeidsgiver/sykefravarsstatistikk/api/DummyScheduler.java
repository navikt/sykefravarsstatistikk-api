package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.Importeringstatus;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.SykefraværsstatistikkImporteringService;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DummyScheduler {

  private final Counter counter;

  public DummyScheduler(MeterRegistry registry) {
    this.counter = registry.counter("dummy_counter");
  }

  @Scheduled(cron = "*/30 * * * * ?")
  public void scheduledImportering() {
    log.info("Kjører dummy-jobb, og inkrementerer counter ");
    log.info("Counter er nå (pre-increment): {}", counter.count());
    counter.increment();
    log.info("Counter er nå (post-increment): {}", counter.count());
  }
}

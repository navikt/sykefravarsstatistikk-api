package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DummyScheduler {

  private final Counter counter;

  public DummyScheduler(MeterRegistry registry) {
    this.counter = registry.counter("dummy_counter");
  }

  @Scheduled(cron = "* */15 * * * ?")
  public void scheduledImportering() {
    log.info("Kjører dummy-jobb, og inkrementerer counter ");
    log.info("Counter er nå (pre-increment): {}", counter.count());
    counter.increment();
    log.info("Counter er nå (post-increment): {}", counter.count());
  }
}

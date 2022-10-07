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
    this.counter = registry.counter("dummy_counter_4");
  }

  @Scheduled(cron = "0 */15 * * * ?")
  public void scheduledImportering() {
    log.info("Kjører dummy-jobb, og inkrementerer dummy_counter_4");
    counter.increment();
    log.info("Counter er nå (post-increment): {}", counter.count());
  }
}

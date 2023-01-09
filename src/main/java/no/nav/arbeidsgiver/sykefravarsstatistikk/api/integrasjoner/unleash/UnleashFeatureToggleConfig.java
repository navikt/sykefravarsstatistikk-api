package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.unleash;

import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.Unleash;
import no.finn.unleash.strategy.GradualRolloutSessionIdStrategy;
import no.finn.unleash.util.UnleashConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UnleashFeatureToggleConfig {

  private final String APP_NAME = "sykefravarsstatistikk-api";
  private final ByClusterStrategy byClusterStrategy;

  @Value("${unleash.url}")
  private String unleashUrl;

  @Autowired
  public UnleashFeatureToggleConfig(ByClusterStrategy byClusterStrategy) {
    this.byClusterStrategy = byClusterStrategy;
  }

  @Bean
  public Unleash initializeUnleash() {
    UnleashConfig config = UnleashConfig.builder().appName(APP_NAME).unleashAPI(unleashUrl).build();

    return new DefaultUnleash(config, byClusterStrategy, new GradualRolloutSessionIdStrategy());
  }
}

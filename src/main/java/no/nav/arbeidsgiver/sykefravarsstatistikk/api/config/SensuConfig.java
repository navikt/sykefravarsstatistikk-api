package no.nav.arbeidsgiver.sykefravarsstatistikk.api.config;

import no.nav.metrics.MetricsClient;
import no.nav.metrics.MetricsConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "prod"})
public class SensuConfig {
    public SensuConfig() {
        String miljø = System.getenv("NAIS_CLUSTER_NAME");
        MetricsClient.enableMetrics(MetricsConfig.resolveNaisConfig().withEnvironment(miljø));
    }
}

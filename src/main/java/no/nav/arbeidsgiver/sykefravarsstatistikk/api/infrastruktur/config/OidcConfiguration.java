package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config;

import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OidcConfiguration {
    @Bean
    public ProxyAwareResourceRetriever resourceRetriever() {
        return new ProxyAwareResourceRetriever();
    }
}

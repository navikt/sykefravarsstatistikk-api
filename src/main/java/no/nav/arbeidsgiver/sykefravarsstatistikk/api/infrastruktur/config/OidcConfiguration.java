package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config;

import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!local")
public class OidcConfiguration {
    @Bean
    public ProxyAwareResourceRetriever resourceRetriever() {
        return new ProxyAwareResourceRetriever();
    }
}

package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config;

import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!local")
public class OidcConfiguration {
    @Bean
    @Primary
    public ProxyAwareResourceRetriever resourceRetriever() {
        return new ProxyAwareResourceRetriever();
    }
}

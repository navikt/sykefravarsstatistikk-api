package no.nav.arbeidsgiver.sykefravarsstatistikk.api.config

import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class OidcConfiguration {
    @Bean
    open fun resourceRetriever(): ProxyAwareResourceRetriever {
        return ProxyAwareResourceRetriever()
    }
}

package no.nav.arbeidsgiver.sykefravarsstatistikk.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
open class RestTemplateConfig {
    @Bean
    open fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}

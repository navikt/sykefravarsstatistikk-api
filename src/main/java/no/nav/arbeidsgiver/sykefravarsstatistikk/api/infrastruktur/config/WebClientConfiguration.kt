package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
open class WebClientConfiguration {

    @Bean
    open fun webClient(builder: WebClient.Builder): WebClient {
        val exchangeStrategies = ExchangeStrategies.builder()
            .codecs { configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) }
            .build()
        return builder
            .exchangeStrategies(exchangeStrategies)
            .build()
    }
}

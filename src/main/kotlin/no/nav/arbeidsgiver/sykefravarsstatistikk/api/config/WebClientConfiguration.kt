package no.nav.arbeidsgiver.sykefravarsstatistikk.api.config

import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient


@Configuration
open class WebClientConfiguration {

    // Konfigurerer opp alle WebClients med maks responsstørrelse på 16MB
    @Bean
    open fun webClientCustomizer(): WebClientCustomizer = WebClientCustomizer { webClientBuilder ->
        val exchangeStrategies = ExchangeStrategies.builder()
            .codecs { configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) }.build()

        webClientBuilder.exchangeStrategies(exchangeStrategies)
    }

    // WebClient med proxy for onprem
    @Bean
    open fun proxyWebClient(webClientBuilder: WebClient.Builder): ProxyWebClient = ProxyWebClient(webClientBuilder)
}


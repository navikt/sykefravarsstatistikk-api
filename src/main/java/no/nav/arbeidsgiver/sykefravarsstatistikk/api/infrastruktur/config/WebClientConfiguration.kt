package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config

import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient


@Configuration
open class WebClientConfiguration {

    // Konfigurerer opp alle WebClients til å bruke webproxy, som er nødvendig på on-prem
    @Bean
    open fun webClientCustomizer(): WebClientCustomizer = WebClientCustomizer { webClientBuilder ->
        val exchangeStrategies = ExchangeStrategies.builder()
            .codecs { configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) }.build()

        webClientBuilder.clientConnector(
            ReactorClientHttpConnector(
                HttpClient.create().proxyWithSystemProperties()
            )
        ).exchangeStrategies(exchangeStrategies)
    }

    @Bean
    open fun webClient(webClientBuilder: WebClient.Builder): WebClient = webClientBuilder.build()
}

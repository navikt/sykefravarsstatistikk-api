package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config

import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

/**
 * A WebClient that uses a proxy for onprem
 */
open class ProxyWebClient private constructor(webClient: WebClient) : WebClient by webClient {
    constructor(webClientBuilder: WebClient.Builder) : this(
        webClientBuilder.clientConnector(
            ReactorClientHttpConnector(
                HttpClient.create().proxyWithSystemProperties()
            )
        ).build()
    )
}
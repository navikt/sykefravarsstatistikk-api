package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.altinn

import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlientConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.ProxyConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AltinnKlientConfig(
    @Value("\${altinn.proxy.url}") altinnProxyUrl: String,
    @Value("\${altinn.url}") altinnUrl: String,
    @Value("\${altinn.apigw.apikey}") altinnAPIGWApikey: String,
    @Value("\${altinn.apikey}") altinnApikey: String,
) {
    private val klient: AltinnrettigheterProxyKlient

    init {
        val config = AltinnrettigheterProxyKlientConfig(
            ProxyConfig("sykefraværsstatistikk", altinnProxyUrl),
            AltinnConfig(altinnUrl, altinnApikey, altinnAPIGWApikey)
        )
        klient = AltinnrettigheterProxyKlient(config)
    }

    @Bean
    open fun getKlient(): AltinnrettigheterProxyKlient = klient
}
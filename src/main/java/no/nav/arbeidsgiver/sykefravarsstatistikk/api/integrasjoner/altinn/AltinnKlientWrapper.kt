package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.altinn

import lombok.extern.slf4j.Slf4j
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlientConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.ProxyConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Fnr
import no.nav.security.token.support.core.jwt.JwtToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Slf4j
@Component
open class AltinnKlientWrapper(
    @Value("\${altinn.proxy.url}") altinnProxyUrl: String?,
    @Value("\${altinn.url}") altinnUrl: String?,
    @Value("\${altinn.apigw.apikey}") altinnAPIGWApikey: String?,
    @Value("\${altinn.apikey}") altinnApikey: String?,
    @param:Value("\${altinn.iaweb.service.code}") private val serviceCode: String,
    @param:Value("\${altinn.iaweb.service.edition}") private val serviceEdition: String
) {
    private val klient: AltinnrettigheterProxyKlient

    init {
        val config = AltinnrettigheterProxyKlientConfig(
            ProxyConfig("sykefraværsstatistikk", altinnProxyUrl!!),
            AltinnConfig(altinnUrl!!, altinnApikey!!, altinnAPIGWApikey!!)
        )
        klient = AltinnrettigheterProxyKlient(config)
    }

    open fun hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(
        idToken: JwtToken, fnr: Fnr
    ): List<AltinnOrganisasjon> {
        return mapTo(
            klient.hentOrganisasjoner(
                SelvbetjeningToken(idToken.tokenAsString),
                Subject(fnr.verdi),
                ServiceCode(serviceCode),
                ServiceEdition(serviceEdition),
                true
            )
        )
    }

    open fun hentOrgnumreDerBrukerHarTilgangTil(idToken: JwtToken, fnr: Fnr): List<AltinnOrganisasjon> {
        return mapTo(
            klient.hentOrganisasjoner(
                SelvbetjeningToken(idToken.tokenAsString), Subject(fnr.verdi), true
            )
        )
    }

    private fun mapTo(altinnReportees: List<AltinnReportee>): List<AltinnOrganisasjon> {
        return altinnReportees.stream()
            .map { (name, type, parentOrganizationNumber, organizationNumber, organizationForm, status): AltinnReportee ->
                AltinnOrganisasjon(name, type, parentOrganizationNumber, organizationNumber, organizationForm, status)
            }
            .collect(Collectors.toList())
    }
}
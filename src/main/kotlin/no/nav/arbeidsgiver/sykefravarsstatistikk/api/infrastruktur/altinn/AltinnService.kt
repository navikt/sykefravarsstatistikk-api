package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.altinn

import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangsstyring.Fnr
import no.nav.security.token.support.core.jwt.JwtToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
open class AltinnService(
    @Autowired val klient: AltinnrettigheterProxyKlient,
    @param:Value("\${altinn.iaweb.service.code}") private val serviceCode: String,
    @param:Value("\${altinn.iaweb.service.edition}") private val serviceEdition: String
) {


    open fun hentVirksomheterDerBrukerHarSykefraværsstatistikkrettighet(
        idToken: JwtToken, fnr: Fnr
    ): List<AltinnOrganisasjon> {
        return klient.hentOrganisasjoner(
            TokenXToken(idToken.tokenAsString),
            Subject(fnr.verdi),
            ServiceCode(serviceCode),
            ServiceEdition(serviceEdition),
            true
        ).map(::toAltinnOrganisasjon)
    }

    open fun hentVirksomheterDerBrukerHarTilknytning(idToken: JwtToken, fnr: Fnr): List<AltinnOrganisasjon> {
        return klient.hentOrganisasjoner(
            TokenXToken(idToken.tokenAsString),
            Subject(fnr.verdi),
            true
        ).map(::toAltinnOrganisasjon)
    }

    private fun toAltinnOrganisasjon(altinnReportee: AltinnReportee): AltinnOrganisasjon {
        return AltinnOrganisasjon(
            altinnReportee.name,
            altinnReportee.type,
            altinnReportee.parentOrganizationNumber,
            altinnReportee.organizationNumber,
            altinnReportee.organizationForm,
            altinnReportee.status
        )
    }
}
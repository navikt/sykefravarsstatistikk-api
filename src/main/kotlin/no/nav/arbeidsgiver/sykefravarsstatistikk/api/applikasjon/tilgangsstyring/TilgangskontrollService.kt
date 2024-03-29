package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangsstyring

import com.nimbusds.jose.JOSEException
import com.nimbusds.oauth2.sdk.GeneralException
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.OverordnetEnhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Virksomhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.altinn.AltinnService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.sporbarhetslog.Loggevent
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.sporbarhetslog.Sporbarhetslogg
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.tokenx.TokenXClient
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.tokenx.TokenXException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.IOException
import java.text.ParseException

@Component
class TilgangskontrollService(
    private val altinnService: AltinnService,
    private val tokenService: TokenService,
    private val sporbarhetslogg: Sporbarhetslogg,
    @param:Value("\${altinn.iaweb.service.code}") private val iawebServiceCode: String,
    @param:Value("\${altinn.iaweb.service.edition}") private val iawebServiceEdition: String,
    private val tokenXClient: TokenXClient
) {
    fun hentBrukerKunIaRettigheter(): InnloggetBruker {
        val innloggetBruker = tokenService.hentInnloggetBruker()
        try {
            val exchangedTokenToAltinnProxy =
                tokenXClient.exchangeTokenToAltinnProxy(tokenService.hentInnloggetJwtToken())
            innloggetBruker.brukerensOrganisasjoner =
                altinnService.hentVirksomheterDerBrukerHarSykefraværsstatistikkrettighet(
                    exchangedTokenToAltinnProxy, innloggetBruker.fnr
                )
        } catch (e: ParseException) {
            throw TilgangskontrollException(e.message!!)
        } catch (e: JOSEException) {
            throw TilgangskontrollException(e.message!!)
        } catch (e: GeneralException) {
            throw TilgangskontrollException(e.message!!)
        } catch (e: IOException) {
            throw TilgangskontrollException(e.message!!)
        } catch (e: TokenXException) {
            throw TilgangskontrollException(e.message!!)
        }
        return innloggetBruker
    }

    fun brukerHarIaRettigheterIVirksomheten(tilOrgnr: Orgnr?): Boolean {
        return hentBrukerKunIaRettigheter().harTilgang(tilOrgnr!!)
    }

    fun brukerRepresentererVirksomheten(orgnr: Orgnr?): Boolean {
        return hentInnloggetBrukerForAlleRettigheter().harTilgang(orgnr!!)
    }

    fun hentInnloggetBrukerForAlleRettigheter(): InnloggetBruker {
        val innloggetBruker = tokenService.hentInnloggetBruker()
        try {
            val exchangedTokenToAltinnProxy =
                tokenXClient.exchangeTokenToAltinnProxy(tokenService.hentInnloggetJwtToken())
            innloggetBruker.brukerensOrganisasjoner = altinnService.hentVirksomheterDerBrukerHarTilknytning(
                exchangedTokenToAltinnProxy, innloggetBruker.fnr
            )
        } catch (e: ParseException) {
            throw TilgangskontrollException(e.message!!)
        } catch (e: JOSEException) {
            throw TilgangskontrollException(e.message!!)
        } catch (e: GeneralException) {
            throw TilgangskontrollException(e.message!!)
        } catch (e: IOException) {
            throw TilgangskontrollException(e.message!!)
        } catch (e: TokenXException) {
            throw TilgangskontrollException(e.message!!)
        }
        return innloggetBruker
    }

    fun hentTilgangTilOverordnetEnhetOgLoggSikkerhetshendelse(
        bruker: InnloggetBruker,
        overordnetEnhet: OverordnetEnhet,
        underenhet: Virksomhet,
        httpMetode: String?,
        requestUrl: String?
    ): Boolean {
        val harTilgang = bruker.harTilgang(overordnetEnhet.orgnr)
        val kommentar = String.format(
            "Bruker ba om tilgang orgnr %s indirekte ved å kalle endepunktet til underenheten"
                    + " %s",
            overordnetEnhet.orgnr.verdi, underenhet.orgnr.verdi
        )
        sporbarhetslogg.loggHendelse(
            Loggevent(
                bruker,
                overordnetEnhet.orgnr,
                harTilgang,
                httpMetode,
                requestUrl,
                iawebServiceCode,
                iawebServiceEdition
            ),
            kommentar
        )
        return harTilgang
    }

    fun sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
        orgnr: Orgnr?, httpMetode: String?, requestUrl: String?
    ) {
        val bruker = hentBrukerKunIaRettigheter()
        sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(orgnr, bruker, httpMetode, requestUrl)
    }

    fun sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
        orgnr: Orgnr?, bruker: InnloggetBruker, httpMetode: String?, requestUrl: String?
    ) {
        val harTilgang = bruker.harTilgang(orgnr!!)
        sporbarhetslogg.loggHendelse(
            Loggevent(
                bruker,
                orgnr,
                harTilgang,
                httpMetode,
                requestUrl,
                iawebServiceCode,
                iawebServiceEdition
            )
        )
        if (!harTilgang) {
            throw TilgangskontrollException("Har ikke tilgang til statistikk for denne bedriften.")
        }
    }
}

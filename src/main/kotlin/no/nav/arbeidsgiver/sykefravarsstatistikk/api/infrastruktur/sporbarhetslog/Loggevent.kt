package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.sporbarhetslog

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangsstyring.InnloggetBruker
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Orgnr

data class Loggevent(
    val innloggetBruker: InnloggetBruker? = null,
    val orgnr: Orgnr? = null,
    val harTilgang: Boolean = false,
    val requestMethod: String? = null,
    val requestUrl: String? = null,
    val altinnServiceCode: String? = null,
    val altinnServiceEdition: String? = null,
)
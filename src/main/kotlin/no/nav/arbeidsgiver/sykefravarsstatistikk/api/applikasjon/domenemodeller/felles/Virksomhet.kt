package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.Næringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.Orgnr

interface Virksomhet {
    val orgnr: Orgnr
    val navn: String
    val næringskode: Næringskode
}
package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.importering

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.Sektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.ÅrstallOgKvartal

data class Orgenhet(
    val orgnr: Orgnr,
    val navn: String?,
    val rectype: String?,
    val sektor: Sektor?,
    val næring: String?,
    val næringskode: String?,
    val årstallOgKvartal: ÅrstallOgKvartal
)
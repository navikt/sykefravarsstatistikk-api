package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal

data class VirksomhetMetadata(
    private val orgnrObj: Orgnr,
    val navn: String,
    val rectype: String,
    val sektor: Sektor,
    val primærnæring: String,
    val primærnæringskode: String,
    val årstallOgKvartal: ÅrstallOgKvartal
) {
    val orgnr: String
        get() = orgnrObj.verdi

    val årstall: Int
        get() = årstallOgKvartal.årstall

    val kvartal: Int
        get() = årstallOgKvartal.kvartal
}


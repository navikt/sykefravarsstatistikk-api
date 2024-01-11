package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Næringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Orgnr
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

                val næringOgNæringskode5siffer: MutableList<Næringskode> = mutableListOf()

                @get:JvmName("getÅrstall")
                val årstall: Int
                get() = årstallOgKvartal.årstall

                val kvartal: Int
                get() = årstallOgKvartal.kvartal

                // TODO: Rydd opp her
                fun leggTilNæringOgNæringskode5siffer(næringOgNæringskode5siffer: List<Næringskode>?) {
                    if (!næringOgNæringskode5siffer.isNullOrEmpty()) {
                        this.næringOgNæringskode5siffer.addAll(næringOgNæringskode5siffer)
        }
    }
}


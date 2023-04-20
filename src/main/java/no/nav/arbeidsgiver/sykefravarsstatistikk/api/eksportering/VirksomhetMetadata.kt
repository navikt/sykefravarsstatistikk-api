package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal

data class VirksomhetMetadata(
    private val orgnrObj: Orgnr,
    val navn: String,
    val rectype: String,
    val sektor: String,
    val næring: String,
    val årstallOgKvartal: ÅrstallOgKvartal
) {
    val orgnr: String?
        get() = orgnrObj.verdi

    val næringOgNæringskode5siffer = mutableListOf<NæringOgNæringskode5siffer>()

    @get:JvmName("getÅrstall")
    val årstall: Int
        get() = årstallOgKvartal.årstall

    val kvartal: Int
        get() = årstallOgKvartal.kvartal

    fun leggTilNæringOgNæringskode5siffer(næringOgNæringskode5siffer: List<NæringOgNæringskode5siffer>?) {
        if (!næringOgNæringskode5siffer.isNullOrEmpty()) {
            this.næringOgNæringskode5siffer.addAll(næringOgNæringskode5siffer)
        }
    }
}


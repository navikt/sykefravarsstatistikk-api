package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Næringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal

class VirksomhetMetadataMedNæringskode(
    private val orgnr: Orgnr,
    private val årstallOgKvartal: ÅrstallOgKvartal,
    private val næringskode: Næringskode
) {

    val Årstall: Int
        get() = årstallOgKvartal.årstall
    val kvartal: Int
        get() = årstallOgKvartal.kvartal
    val næring: String
        get() = næringskode.næring.tosifferIdentifikator

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as VirksomhetMetadataMedNæringskode
        if (orgnr != that.orgnr) return false
        return if (årstallOgKvartal != that.årstallOgKvartal) false else næringskode == that.næringskode
    }

    override fun hashCode(): Int {
        var result = orgnr.hashCode()
        result = 31 * result + årstallOgKvartal.hashCode()
        result = 31 * result + næringskode.hashCode()
        return result
    }
}

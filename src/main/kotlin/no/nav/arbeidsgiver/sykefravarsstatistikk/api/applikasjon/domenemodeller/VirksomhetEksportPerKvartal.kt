package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

class VirksomhetEksportPerKvartal(
    private val orgnr: Orgnr, val ÅrstallOgKvartal: ÅrstallOgKvartal, private val eksportert: Boolean
) {
    fun getOrgnr(): String {
        return orgnr.verdi
    }

    val Årstall: Int
        get() = ÅrstallOgKvartal.årstall
    val kvartal: Int
        get() = ÅrstallOgKvartal.kvartal

    fun eksportert(): Boolean {
        return eksportert
    }
}

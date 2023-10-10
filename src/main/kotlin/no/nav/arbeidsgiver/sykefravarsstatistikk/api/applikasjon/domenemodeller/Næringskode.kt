package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

data class Næringskode(
    val femsifferIdentifikator: String
) {
    init {
        require(femsifferIdentifikator.matches("""\d{5}""".toRegex())) {
            "Næringskode skal være 5 siffer, men var $femsifferIdentifikator"
        }
    }
    val næring = Næring(femsifferIdentifikator.take(2))
}
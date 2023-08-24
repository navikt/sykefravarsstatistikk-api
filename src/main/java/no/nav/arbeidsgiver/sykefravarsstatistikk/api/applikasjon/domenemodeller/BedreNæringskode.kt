package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

data class BedreNæringskode(
    val femsifferIdentifikator: String
) {
    val næring = BedreNæring(femsifferIdentifikator.take(2))
}
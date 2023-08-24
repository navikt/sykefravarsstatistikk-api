package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

data class BedreNæringskode(
    val identifikator: String
) {
    val næring = BedreNæring(identifikator.take(2))
}
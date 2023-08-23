package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

data class Næring(
    // TODO: Rename til noe annet så vi skjønner at her får vi en næringskode
    // TODO: Legg til hent 2-siffer
    val kode: String,
    val navn: String
)
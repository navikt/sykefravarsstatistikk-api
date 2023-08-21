package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

enum class Statistikkategori {
    LAND,
    SEKTOR,
    NÆRING,
    BRANSJE,
    NÆRING5SIFFER,
    OVERORDNET_ENHET,
    VIRKSOMHET,

    // deprecated -- bruk NÆRINGSKODE i stedet
    NÆRINGSKODE
}

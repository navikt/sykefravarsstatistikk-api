package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

enum class Statistikkategori {
    LAND,
    SEKTOR,
    NÆRING,
    BRANSJE,
    VIRKSOMHET,
    OVERORDNET_ENHET,
    NÆRING5SIFFER,

    // deprecated -- bruk NÆRINGSKODE i stedet
    NÆRINGSKODE
}

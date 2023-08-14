package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

/**
 * Viser status på alle stegene som gjøres ved automatisk
 * import og eksport av sykefraværsstatistikk.
 */
interface ImportEksportStatus {
    val årstallOgKvartal: ÅrstallOgKvartal
    val importertStatistikk: Boolean
    val importertVirksomhetsdata: Boolean
    val forberedtNesteEksport: Boolean
    val eksportertPåKafka: Boolean
}
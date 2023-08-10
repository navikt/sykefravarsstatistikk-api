package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

interface ImportEksportStatus {
    val årstallOgKvartal: ÅrstallOgKvartal
    val importertStatistikk: Boolean
    val importertVirksomhetsdata: Boolean
    val forberedtNesteEksport: Boolean
    val eksportertPåKafka: Boolean
}
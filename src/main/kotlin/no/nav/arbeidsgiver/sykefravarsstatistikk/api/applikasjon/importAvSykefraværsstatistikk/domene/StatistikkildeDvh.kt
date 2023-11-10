package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene

enum class StatistikkildeDvh(@JvmField val tabell: String) {
    VIRKSOMHET("dt_p.agg_ia_sykefravar_v")
}

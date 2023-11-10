package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene

enum class StatistikkildeDvh(@JvmField val tabell: String) {
    NÆRING_5_SIFFER("dt_p.agg_ia_sykefravar_naring_kode"),
    VIRKSOMHET("dt_p.agg_ia_sykefravar_v")
}

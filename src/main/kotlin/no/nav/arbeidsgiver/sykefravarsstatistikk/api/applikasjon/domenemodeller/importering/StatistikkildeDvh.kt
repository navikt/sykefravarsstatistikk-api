package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.importering

enum class StatistikkildeDvh(@JvmField val tabell: String) {
    LAND_OG_SEKTOR("dt_p.agg_ia_sykefravar_land_v"),
    NÆRING("dt_p.v_agg_ia_sykefravar_naring"),
    NÆRING_5_SIFFER("dt_p.agg_ia_sykefravar_naring_kode"),
    VIRKSOMHET("dt_p.agg_ia_sykefravar_v")
}

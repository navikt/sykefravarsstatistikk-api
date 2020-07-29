package no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk;

public enum StatistikkildeDvh {
    LAND_OG_SEKTOR("dt_p.agg_ia_sykefravar_land_v"),
    NÆRING("dt_p.v_agg_ia_sykefravar_naring"),
    NÆRING_5_SIFFER("dt_p.agg_ia_sykefravar_naring_kode"),
    VIRKSOMHET("dt_p.agg_ia_sykefravar_v");

    public final String tabell;

    StatistikkildeDvh(String tabell) {
        this.tabell = tabell;
    }
}

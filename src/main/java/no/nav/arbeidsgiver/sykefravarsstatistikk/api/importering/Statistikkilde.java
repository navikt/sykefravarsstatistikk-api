package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering;

public enum Statistikkilde {
    LAND("sykefravar_statistikk_land"),
    SEKTOR("sykefravar_statistikk_sektor"),
    NÆRING("sykefravar_statistikk_naring"),
    NÆRING_5_SIFFER("sykefravar_statistikk_naring5siffer"),
    VIRKSOMHET("sykefravar_statistikk_virksomhet");

    public final String tabell;

    Statistikkilde(String tabell) {
        this.tabell = tabell;
    }
}

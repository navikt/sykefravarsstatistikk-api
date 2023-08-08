package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene;

public enum Statistikkilde {
  LAND("sykefravar_statistikk_land"),
  SEKTOR("sykefravar_statistikk_sektor"),
  NÆRING("sykefravar_statistikk_naring"),
  NÆRING_5_SIFFER("sykefravar_statistikk_naring5siffer"),
  VIRKSOMHET("sykefravar_statistikk_virksomhet"),
  VIRKSOMHET_MED_GRADERING("sykefravar_statistikk_virksomhet_med_gradering");

  public final String tabell;

  Statistikkilde(String tabell) {
    this.tabell = tabell;
  }
}

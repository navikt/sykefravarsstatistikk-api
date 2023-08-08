package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell;

public enum Klassifikasjonskilde {
  SEKTOR("sektor"),
  NÆRING("naring");

  public final String tabell;

  Klassifikasjonskilde(String tabell) {
    this.tabell = tabell;
  }
}

package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles;

public interface Virksomhet {
  public Orgnr getOrgnr();

  public String getNavn();

  public Næringskode5Siffer getNæringskode();
}

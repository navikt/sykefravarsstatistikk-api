package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

public class EksporteringBegrensning {

  private final Integer antallSomSkalEksporteres;
  private final boolean erBegrenset;

  public boolean erBegrenset() {
    return erBegrenset;
  }

  public static EksporteringBegrensningBuilder build() {
    return new EksporteringBegrensningBuilder();
  }

  private EksporteringBegrensning(int begrensningTil, boolean erBegrenset) {
    this.antallSomSkalEksporteres = begrensningTil;
    this.erBegrenset = erBegrenset;
  }

  private EksporteringBegrensning(boolean erBegrenset) {
    this.antallSomSkalEksporteres = 0;
    this.erBegrenset = erBegrenset;
  }

  public Integer getAntallSomSkalEksporteres() {
    return antallSomSkalEksporteres;
  }

  public static class EksporteringBegrensningBuilder {
    public EksporteringBegrensning medBegrensning(int antallSomSkalEksporteres) {
      return new EksporteringBegrensning(antallSomSkalEksporteres, true);
    }

    public EksporteringBegrensning utenBegrensning() {
      return new EksporteringBegrensning(false);
    }
  }
}

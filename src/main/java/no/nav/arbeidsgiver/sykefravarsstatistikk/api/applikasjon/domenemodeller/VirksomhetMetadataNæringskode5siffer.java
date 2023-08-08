package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller;

public class VirksomhetMetadataNæringskode5siffer {
  public VirksomhetMetadataNæringskode5siffer(
      Orgnr orgnr,
      ÅrstallOgKvartal årstallOgKvartal,
      NæringOgNæringskode5siffer næringOgNæringskode5siffer) {
    this.orgnr = orgnr;
    this.årstallOgKvartal = årstallOgKvartal;
    this.næringOgNæringskode5siffer = næringOgNæringskode5siffer;
  }

  private final Orgnr orgnr;
  private final ÅrstallOgKvartal årstallOgKvartal;
  private final NæringOgNæringskode5siffer næringOgNæringskode5siffer;

  public String getOrgnr() {
    return orgnr.getVerdi();
  }

  public int getÅrstall() {
    return årstallOgKvartal.getÅrstall();
  }

  public int getKvartal() {
    return årstallOgKvartal.getKvartal();
  }

  public String getNæring() {
    return næringOgNæringskode5siffer.getNæring();
  }

  public String getNæringskode5siffer() {
    return næringOgNæringskode5siffer.getNæringskode5Siffer();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    VirksomhetMetadataNæringskode5siffer that = (VirksomhetMetadataNæringskode5siffer) o;

    if (!orgnr.equals(that.orgnr)) return false;
    if (!årstallOgKvartal.equals(that.årstallOgKvartal)) return false;
    return næringOgNæringskode5siffer.equals(that.næringOgNæringskode5siffer);
  }

  @Override
  public int hashCode() {
    int result = orgnr.hashCode();
    result = 31 * result + årstallOgKvartal.hashCode();
    result = 31 * result + næringOgNæringskode5siffer.hashCode();
    return result;
  }
}

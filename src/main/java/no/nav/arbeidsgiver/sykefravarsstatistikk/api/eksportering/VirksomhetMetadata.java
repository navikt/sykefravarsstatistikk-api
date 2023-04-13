package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VirksomhetMetadata {
  private final Orgnr orgnr;
  private final String navn;
  private final String rectype;
  private final String sektor;
  private final String næring;
  private final ÅrstallOgKvartal årstallOgKvartal;
  private final List<NæringOgNæringskode5siffer> næringOgNæringskode5siffer;

  public VirksomhetMetadata(
      Orgnr orgnr,
      String navn,
      String rectype,
      String sektor,
      String næring,
      ÅrstallOgKvartal årstallOgKvartal) {
    this.orgnr = orgnr;
    this.navn = navn;
    this.rectype = rectype;
    this.sektor = sektor;
    this.næring = næring;
    this.årstallOgKvartal = årstallOgKvartal;
    this.næringOgNæringskode5siffer = new ArrayList<>();
  }

  public String getOrgnr() {
    return orgnr.getVerdi();
  }

  public String getNavn() {
    return navn;
  }

  public String getRectype() {
    return rectype;
  }

  public String getSektor() {
    return sektor;
  }

  public String getNæring() {
    return næring;
  }

  public int getÅrstall() {
    return årstallOgKvartal.getÅrstall();
  }

  public int getKvartal() {
    return årstallOgKvartal.getKvartal();
  }

  public List<NæringOgNæringskode5siffer> getNæringOgNæringskode5siffer() {
    return næringOgNæringskode5siffer;
  }

  public void leggTilNæringOgNæringskode5siffer(
      List<NæringOgNæringskode5siffer> næringOgNæringskode5siffer) {
    if (næringOgNæringskode5siffer != null && !næringOgNæringskode5siffer.isEmpty()) {
      this.næringOgNæringskode5siffer.addAll(næringOgNæringskode5siffer);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    VirksomhetMetadata that = (VirksomhetMetadata) o;

    if (!orgnr.equals(that.orgnr)) return false;
    if (!navn.equals(that.navn)) return false;
    if (!rectype.equals(that.rectype)) return false;
    if (!sektor.equals(that.sektor)) return false;
    if (!næring.equals(that.næring)) return false;
    if (!årstallOgKvartal.equals(that.årstallOgKvartal)) return false;
    return Objects.equals(næringOgNæringskode5siffer, that.næringOgNæringskode5siffer);
  }

  @Override
  public int hashCode() {
    int result = orgnr.hashCode();
    result = 31 * result + navn.hashCode();
    result = 31 * result + rectype.hashCode();
    result = 31 * result + sektor.hashCode();
    result = 31 * result + næring.hashCode();
    result = 31 * result + årstallOgKvartal.hashCode();
    result =
        31 * result
            + næringOgNæringskode5siffer.hashCode();
    return result;
  }
}

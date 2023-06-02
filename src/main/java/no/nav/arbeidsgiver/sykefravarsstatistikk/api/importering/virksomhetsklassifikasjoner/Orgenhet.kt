package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.virksomhetsklassifikasjoner;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;

import java.util.Objects;

public class Orgenhet {
  private final Orgnr orgnr;
  private final String navn;
  private final String rectype;
  private final String sektor;
  private final String næring;
  private final ÅrstallOgKvartal årstallOgKvartal;

  public Orgenhet(
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
  }

  public Orgnr getOrgnr() {
    return orgnr;
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

  public ÅrstallOgKvartal getÅrstallOgKvartal() {
    return årstallOgKvartal;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Orgenhet orgenhet = (Orgenhet) o;

    if (!orgnr.equals(orgenhet.orgnr)) return false;
    if (!Objects.equals(navn, orgenhet.navn)) return false;
    if (!Objects.equals(rectype, orgenhet.rectype)) return false;
    if (!Objects.equals(sektor, orgenhet.sektor)) return false;
    if (!Objects.equals(næring, orgenhet.næring)) return false;
    return årstallOgKvartal.equals(orgenhet.årstallOgKvartal);
  }

  @Override
  public int hashCode() {
    int result = orgnr.hashCode();
    result = 31 * result + (navn != null ? navn.hashCode() : 0);
    result = 31 * result + (rectype != null ? rectype.hashCode() : 0);
    result = 31 * result + (sektor != null ? sektor.hashCode() : 0);
    result = 31 * result + (næring != null ? næring.hashCode() : 0);
    result = 31 * result + årstallOgKvartal.hashCode();
    return result;
  }
}

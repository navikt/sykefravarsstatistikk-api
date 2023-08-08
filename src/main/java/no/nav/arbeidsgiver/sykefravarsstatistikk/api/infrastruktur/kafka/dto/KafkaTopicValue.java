package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.VirksomhetSykefravær;

import java.util.List;

public class KafkaTopicValue {
  private final VirksomhetSykefravær virksomhetSykefravær;
  private final List<SykefraværMedKategori> næring5SifferSykefravær;
  private final SykefraværMedKategori næringSykefravær;
  private final SykefraværMedKategori sektorSykefravær;
  private final SykefraværMedKategori landSykefravær;

  public KafkaTopicValue(
      VirksomhetSykefravær virksomhetSykefravær,
      List<SykefraværMedKategori> næring5SifferSykefravær,
      SykefraværMedKategori næringSykefravær,
      SykefraværMedKategori sektorSykefravær,
      SykefraværMedKategori landSykefravær) {
    this.virksomhetSykefravær = virksomhetSykefravær;
    this.næring5SifferSykefravær = næring5SifferSykefravær;
    this.næringSykefravær = næringSykefravær;
    this.sektorSykefravær = sektorSykefravær;
    this.landSykefravær = landSykefravær;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    KafkaTopicValue that = (KafkaTopicValue) o;

    if (!virksomhetSykefravær.equals(that.virksomhetSykefravær)) return false;
    if (!næring5SifferSykefravær.equals(that.næring5SifferSykefravær)) return false;
    if (!næringSykefravær.equals(that.næringSykefravær)) return false;
    if (!sektorSykefravær.equals(that.sektorSykefravær)) return false;
    return landSykefravær.equals(that.landSykefravær);
  }

  @Override
  public int hashCode() {
    int result = virksomhetSykefravær.hashCode();
    result = 31 * result + næring5SifferSykefravær.hashCode();
    result = 31 * result + næringSykefravær.hashCode();
    result = 31 * result + sektorSykefravær.hashCode();
    result = 31 * result + landSykefravær.hashCode();
    return result;
  }

  public VirksomhetSykefravær getVirksomhetSykefravær() {
    return virksomhetSykefravær;
  }

  public List<SykefraværMedKategori> getNæring5SifferSykefravær() {
    return næring5SifferSykefravær;
  }

  public SykefraværMedKategori getNæringSykefravær() {
    return næringSykefravær;
  }

  public SykefraværMedKategori getSektorSykefravær() {
    return sektorSykefravær;
  }

  public SykefraværMedKategori getLandSykefravær() {
    return landSykefravær;
  }
}

package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.SykefraværFlereKvartalerForEksport;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;

@Getter
@Setter
@NoArgsConstructor
public class KafkaStatistikkKategoriTopicValue {

  private Statistikkategori kategori;
  private String kode;
  private SistePubliserteKvartal sistePubliserteKvartal;
  private Siste4Kvartaler siste4Kvartal;

  public KafkaStatistikkKategoriTopicValue(
      SykefraværMedKategori sisteKvartal,
      SykefraværFlereKvartalerForEksport siste4Kvartal
  ) {
    this.kategori = sisteKvartal.getKategori();
    this.kode = sisteKvartal.getKode();
    sistePubliserteKvartal = new SistePubliserteKvartal(
        sisteKvartal.getÅrstall(),
        sisteKvartal.getKvartal(),
        sisteKvartal.getProsent(),
        sisteKvartal.getTapteDagsverk(),
        sisteKvartal.getMuligeDagsverk(),
        sisteKvartal.getAntallPersoner(),
        sisteKvartal.isErMaskert()
    );
    this.siste4Kvartal = new Siste4Kvartaler(
        siste4Kvartal.getProsent(),
        siste4Kvartal.getTapteDagsverk(),
        siste4Kvartal.getMuligeDagsverk(),
        siste4Kvartal.isErMaskert(),
        siste4Kvartal.getKvartaler()
    );
  }

  @Override
  public boolean equals(Object o) {
      if (this == o) {
          return true;
      }
      if (o == null || getClass() != o.getClass()) {
          return false;
      }

    KafkaStatistikkKategoriTopicValue that = (KafkaStatistikkKategoriTopicValue) o;

      if (!siste4Kvartal.equals(that.siste4Kvartal)) {
          return false;
      }
    return sistePubliserteKvartal.equals(that.sistePubliserteKvartal);
  }

  @Override
  public int hashCode() {
    int result = sistePubliserteKvartal.hashCode();
    result = 31 * result + (siste4Kvartal != null ? siste4Kvartal.hashCode() : 0);
    return result;
  }


    /*
    public SykefraværMedKategori getSisteKvartal() { return sistePubliserteKvartal; }
    public SykefraværOverFlereKvartaler getSiste4Kvartal() { return siste4Kvartal; }*/
}



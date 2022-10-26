package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.StatistikkDto;

public class KafkaStatistikkKategoriTopicValue {
    private final  SykefraværMedKategori sisteKvartal;
    private final  StatistikkDto siste4Kvartal;

    public KafkaStatistikkKategoriTopicValue(
            SykefraværMedKategori sisteKvartal,
            StatistikkDto siste4Kvartal
    ) {
        this.sisteKvartal = sisteKvartal;
        this.siste4Kvartal = siste4Kvartal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KafkaStatistikkKategoriTopicValue that = (KafkaStatistikkKategoriTopicValue) o;

        if(!siste4Kvartal.equals(that.siste4Kvartal)) return false;
        return sisteKvartal.equals(that.sisteKvartal);
    }

    @Override
    public int hashCode() {
        int result = sisteKvartal.hashCode();
        result = 31 * result + ( siste4Kvartal != null ? siste4Kvartal.hashCode() : 0);
        return result;
    }


    public SykefraværMedKategori getSisteKvartal() { return sisteKvartal; }
    public StatistikkDto getSiste4Kvartal() { return siste4Kvartal; }
}

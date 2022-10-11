package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.StatistikkDto;

import java.util.List;

public class KafkaTopicLandValue {
    private final  SykefraværMedKategori landSykefravær;
    private final  List<StatistikkDto> statistikkDtoList;

    public KafkaTopicLandValue(
            SykefraværMedKategori landSykefravær,
            List<StatistikkDto> statistikkDtoList
    ) {
        this.landSykefravær = landSykefravær;
        this.statistikkDtoList=statistikkDtoList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KafkaTopicLandValue that = (KafkaTopicLandValue) o;

        if(!statistikkDtoList.equals(that.statistikkDtoList)) return false;
        return landSykefravær.equals(that.landSykefravær);
    }

    @Override
    public int hashCode() {
        int result = landSykefravær.hashCode();
        result = 31 * result + ( statistikkDtoList != null ? statistikkDtoList.hashCode() : 0);
        return result;
    }


    public SykefraværMedKategori getLandSykefravær() { return landSykefravær; }
    public List<StatistikkDto> getStatistikkDtoList() { return statistikkDtoList; }
}

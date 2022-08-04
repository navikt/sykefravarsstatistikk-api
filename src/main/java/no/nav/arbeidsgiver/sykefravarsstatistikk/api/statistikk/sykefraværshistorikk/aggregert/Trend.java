package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.Aggregeringstype;

@Data
@AllArgsConstructor
public class Trend {

    public BigDecimal trendverdi;
    public int antallPersonerIBeregningen;
    public List<ÅrstallOgKvartal> kvartalerIBeregningen;

    AggregertStatistikkDto tilAggregertHistorikkDto(
          Aggregeringstype type, String label) {
        return AggregertStatistikkDto.builder()
              .type(type)
              .label(label)
              .verdi(this.trendverdi.toString())
              .antallTilfellerIBeregningen(this.antallPersonerIBeregningen)
              .kvartalerIBeregningen(this.kvartalerIBeregningen)
              .build();
    }
}

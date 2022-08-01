package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.Statistikktype;

@Data
@AllArgsConstructor
public class Trend {

    public BigDecimal trendverdi;
    public int antallTilfellerIBeregningen;
    public List<ÅrstallOgKvartal> kvartalerIBeregningen;

    AggregertHistorikkDto tilAggregertHistorikkDto(
          Statistikktype type, String label) {
        return AggregertHistorikkDto.builder()
              .type(type)
              .label(label)
              .verdi(this.trendverdi.toString())
              .antallTilfellerIBeregningen(this.antallTilfellerIBeregningen)
              .kvartalerIBeregningen(this.kvartalerIBeregningen)
              .build();
    }
}

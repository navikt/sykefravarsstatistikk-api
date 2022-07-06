package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;

@Data
@AllArgsConstructor
public class Trend {
    public BigDecimal trendverdi;
    public int antallTilfellerIBeregningen;
    public List<ÅrstallOgKvartal> kvartalerIBeregningen;

    AggregertHistorikkDto tilAggregertHistorikkDto(
            Statistikkategori type, String label) {
        return new AggregertHistorikkDto(
                type,
                label,
                this.trendverdi.toString(),
                this.antallTilfellerIBeregningen,
                this.kvartalerIBeregningen);
    }
}

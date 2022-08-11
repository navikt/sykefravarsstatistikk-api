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
    public int antallPersonerIBeregningen;
    public List<ÅrstallOgKvartal> kvartalerIBeregningen;

    StatistikkDto tilAggregertHistorikkDto(
            Statistikkategori type, String label) {
        return StatistikkDto.builder()
                .statistikkategori(type)
                .label(label)
                .verdi(this.trendverdi.toString())
                .antallPersonerIBeregningen(this.antallPersonerIBeregningen)
                .kvartalerIBeregningen(this.kvartalerIBeregningen)
                .build();
    }
}

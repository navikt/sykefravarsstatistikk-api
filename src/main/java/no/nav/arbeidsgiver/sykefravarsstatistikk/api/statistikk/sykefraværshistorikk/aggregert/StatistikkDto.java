package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatistikkDto {

    private Statistikkategori statistikkategori;
    private String label;
    private String verdi;
    private Integer antallPersonerIBeregningen;
    private List<ÅrstallOgKvartal> kvartalerIBeregningen;
}
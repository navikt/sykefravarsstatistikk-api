package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;

import java.util.List;

@Data
public class AggregertHistorikkDto {
    private final Statistikkategori type;
    private final String label;
    private final String verdi;
    private final Integer antallTilfellerIBeregningen;
    private final List<ÅrstallOgKvartal> kvartalerIBeregningen;
}
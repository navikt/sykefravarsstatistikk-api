package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.Statistikktype;

@Data
@Builder
public class AggregertHistorikkDto {

    private final Statistikktype type;
    private final String label;
    private final String verdi;
    private final Integer antallTilfellerIBeregningen;
    private final List<ÅrstallOgKvartal> kvartalerIBeregningen;
}
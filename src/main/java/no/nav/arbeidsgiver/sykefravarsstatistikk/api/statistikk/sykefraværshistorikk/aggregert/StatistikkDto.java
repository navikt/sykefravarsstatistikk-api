package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;

@Data
@Builder
public class StatistikkDto {

  private Statistikkategori statistikkategori;
  private String label;
  private String verdi;
  private Integer antallPersonerIBeregningen;
  private List<ÅrstallOgKvartal> kvartalerIBeregningen;
}

package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.ÅrstallOgKvartal;

@Data
@AllArgsConstructor
@Builder
public class Publiseringsdatoer {
  private String sistePubliseringsdato;
  private String nestePubliseringsdato;
  private ÅrstallOgKvartal gjeldendePeriode;
}

package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Publiseringsdatoer {
  private String sistePubliseringsdato;
  private String nestePubliseringsdato;
  private ÅrstallOgKvartal gjeldendePeriode;
}

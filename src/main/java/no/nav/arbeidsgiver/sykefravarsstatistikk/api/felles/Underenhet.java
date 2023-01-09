package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class Underenhet implements Virksomhet {
  private final Orgnr orgnr;
  private final Orgnr overordnetEnhetOrgnr;
  private final String navn;
  private final Næringskode5Siffer næringskode;
  private final int antallAnsatte;
}

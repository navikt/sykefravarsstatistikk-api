package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;

@FunctionalInterface
public interface DeleteSykefraværsstatistikkFunction {
  int apply(ÅrstallOgKvartal årstallOgKvartal);
}

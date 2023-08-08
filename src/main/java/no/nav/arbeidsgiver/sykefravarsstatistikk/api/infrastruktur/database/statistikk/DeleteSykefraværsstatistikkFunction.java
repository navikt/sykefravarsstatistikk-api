package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.statistikk;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.ÅrstallOgKvartal;

@FunctionalInterface
public interface DeleteSykefraværsstatistikkFunction {
  int apply(ÅrstallOgKvartal årstallOgKvartal);
}

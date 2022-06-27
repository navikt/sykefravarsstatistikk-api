package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Kvartal;

@FunctionalInterface
public interface DeleteSykefraværsstatistikkFunction {
    int apply(Kvartal kvartal);
}

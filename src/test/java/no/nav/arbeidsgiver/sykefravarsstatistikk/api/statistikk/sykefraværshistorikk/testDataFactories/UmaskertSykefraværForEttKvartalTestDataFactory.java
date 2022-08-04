package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.testDataFactories;

import java.math.BigDecimal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;

public class UmaskertSykefraværForEttKvartalTestDataFactory {

    public static UmaskertSykefraværForEttKvartal opprettTestdata(
          ÅrstallOgKvartal kvartal,
          int tapteDagsverk,
          int muligeDagsverk,
          int antallPersoner) {
        return new UmaskertSykefraværForEttKvartal(
              kvartal,
              new BigDecimal(String.valueOf(tapteDagsverk)),
              new BigDecimal(String.valueOf(muligeDagsverk)),
              antallPersoner);
    }
}


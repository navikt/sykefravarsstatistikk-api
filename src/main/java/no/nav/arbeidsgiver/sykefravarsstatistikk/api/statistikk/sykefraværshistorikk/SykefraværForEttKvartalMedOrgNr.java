package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;

import java.math.BigDecimal;

public class SykefraværForEttKvartalMedOrgNr extends SykefraværForEttKvartal {
    String orgnr;

    public SykefraværForEttKvartalMedOrgNr(ÅrstallOgKvartal årstallOgKvartal, String orgnr, BigDecimal tapte_dagsverk, BigDecimal mulige_dagsverk, int antall_personer) {
        super(årstallOgKvartal, tapte_dagsverk, mulige_dagsverk, antall_personer);
        this.orgnr = orgnr;
    }
}

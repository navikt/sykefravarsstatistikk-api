package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;

import java.math.BigDecimal;

public class SykefraværForEttKvartalMedOrgNr extends SykefraværForEttKvartal {
    String orgnr;
    String næringskode5Siffer;

    public SykefraværForEttKvartalMedOrgNr(ÅrstallOgKvartal årstallOgKvartal,
                                           String orgnr,
                                           BigDecimal tapte_dagsverk,
                                           BigDecimal mulige_dagsverk,
                                           int antall_personer,
                                           String næringskode5Siffer) {
        super(årstallOgKvartal, tapte_dagsverk, mulige_dagsverk, antall_personer);
        this.orgnr = orgnr;
        this.næringskode5Siffer=næringskode5Siffer;
    }

    public String getOrgnr() {
        return orgnr;
    }
    public String getNæringskode5Siffer() {
        return næringskode5Siffer;
    }
}

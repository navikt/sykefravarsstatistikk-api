package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import lombok.Getter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;

import java.math.BigDecimal;

@Getter
public class UmaskertSykefraværForEttKvartalMedVarighet extends UmaskertSykefraværForEttKvartal {

    private final Varighetskategori varighet;

    public UmaskertSykefraværForEttKvartalMedVarighet(
            ÅrstallOgKvartal årstallOgKvartal,
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            int antallPersoner,
            Varighetskategori varighet
    ) {
        super(årstallOgKvartal, tapteDagsverk, muligeDagsverk, antallPersoner);
        this.varighet = varighet;
    }

    public UmaskertSykefraværForEttKvartal tilUmaskertSykefraværForEttKvartal() {
        return new UmaskertSykefraværForEttKvartal(
                super.getÅrstallOgKvartal(),
                super.getTapteDagsverk(),
                super. getMuligeDagsverk(),
                super.getAntallPersoner());
    }


}

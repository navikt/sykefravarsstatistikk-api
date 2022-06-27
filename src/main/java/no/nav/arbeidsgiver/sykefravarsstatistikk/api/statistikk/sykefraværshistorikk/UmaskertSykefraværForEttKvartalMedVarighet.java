package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import lombok.Getter;
import lombok.ToString;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Kvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;

import java.math.BigDecimal;

@Getter
@ToString(callSuper = true)
public class UmaskertSykefraværForEttKvartalMedVarighet extends UmaskertSykefraværForEttKvartal {

    private final Varighetskategori varighet;

    public UmaskertSykefraværForEttKvartalMedVarighet(
            Kvartal kvartal,
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            int antallPersoner,
            Varighetskategori varighet
    ) {
        super(kvartal, tapteDagsverk, muligeDagsverk, antallPersoner);
        this.varighet = varighet;
    }

    public UmaskertSykefraværForEttKvartal tilUmaskertSykefraværForEttKvartal() {
        return new UmaskertSykefraværForEttKvartal(
                super.getKvartal(),
                super.getTapteDagsverk(),
                super. getMuligeDagsverk(),
                super.getAntallPersoner());
    }

}

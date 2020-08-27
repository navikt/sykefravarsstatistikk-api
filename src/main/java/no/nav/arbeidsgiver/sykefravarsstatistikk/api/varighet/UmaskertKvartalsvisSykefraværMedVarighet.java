package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import lombok.Getter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.common.Sykefraværsvarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;

import java.math.BigDecimal;

@Getter
public class UmaskertKvartalsvisSykefraværMedVarighet extends UmaskertKvartalsvisSykefravær  {


    private final Sykefraværsvarighet varighet;

    public UmaskertKvartalsvisSykefraværMedVarighet(
            ÅrstallOgKvartal årstallOgKvartal,
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            int antallPersoner,
            Sykefraværsvarighet varighet) {
        super(årstallOgKvartal, tapteDagsverk, muligeDagsverk, antallPersoner);
        this.varighet = varighet;
    }

    public UmaskertKvartalsvisSykefravær tilUmaskertKvartalsvisSykefravær() {
        return new UmaskertKvartalsvisSykefravær(
                super.getÅrstallOgKvartal(),
                super.getTapteDagsverk(),
                super. getMuligeDagsverk(),
                super.getAntallPersoner());
    }


}

package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Konstanter.MIN_ANTALL_PERS_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;

@Getter
@EqualsAndHashCode
public abstract class MaskerbartSykefraværOverFlereKvartaler {

    private final BigDecimal prosent;
    private final BigDecimal tapteDagsverk;
    private final BigDecimal muligeDagsverk;
    private final boolean erMaskert;

    public MaskerbartSykefraværOverFlereKvartaler(
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            List<SykefraværForEttKvartal> sykefraværForEttKvartalList,
            boolean harSykefraværData
    ) {
        erMaskert =
                harSykefraværData &&
                        sykefraværForEttKvartalList.stream().allMatch(MaskerbartSykefravær::isErMaskert);

        if (!erMaskert && harSykefraværData) {
            // TODO: Bruk kalkulerSykefraværsprosent I StatistikkUtils til å regne ut prosenten!!!!
            prosent = tapteDagsverk
                    .multiply(new BigDecimal(100))
                    .divide(muligeDagsverk, 1, RoundingMode.HALF_UP);
            this.tapteDagsverk = tapteDagsverk.setScale(1, RoundingMode.HALF_UP);
            this.muligeDagsverk = muligeDagsverk.setScale(1, RoundingMode.HALF_UP);
        } else {
            prosent = null;
            this.tapteDagsverk = null;
            this.muligeDagsverk = null;
        }
    }
}

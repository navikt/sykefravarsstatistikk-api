package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.sykefravær;

import lombok.Getter;
import lombok.Setter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.ÅrstallOgKvartal;

import java.math.BigDecimal;
import java.util.List;


@Setter
@Getter
public class SykefraværSiste4Kvartaler extends MaskerbarSykefravær {

    private final List<ÅrstallOgKvartal> kvartaler;

    public SykefraværSiste4Kvartaler(
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            int maksAntallPersonerOverPerioden,
            List<ÅrstallOgKvartal> kvartaler
    ) {
        super(
                tapteDagsverk,
                muligeDagsverk,
                maksAntallPersonerOverPerioden,
                !kvartaler.isEmpty()
        );
        this.kvartaler = kvartaler;
    }

}

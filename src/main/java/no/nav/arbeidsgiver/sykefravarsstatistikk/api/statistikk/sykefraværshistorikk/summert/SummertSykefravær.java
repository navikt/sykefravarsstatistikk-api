package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import lombok.Getter;
import lombok.Setter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.MaskerbartSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;

import java.math.BigDecimal;
import java.util.List;


@Setter
@Getter
public class SummertSykefravær extends MaskerbartSykefravær {

    private final List<ÅrstallOgKvartal> kvartaler;

    public SummertSykefravær(
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

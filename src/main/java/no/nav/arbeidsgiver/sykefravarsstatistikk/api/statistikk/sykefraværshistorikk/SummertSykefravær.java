package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import lombok.Getter;
import lombok.Setter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


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

    public static SummertSykefravær getSummertSykefravær(
            List<UmaskertSykefraværForEttKvartal> kvartalsvisSykefravær
    ) {

        BigDecimal totalTaptedagsverk = kvartalsvisSykefravær
                .stream()
                .map(UmaskertSykefraværForEttKvartal::getDagsverkTeller)
                .reduce(
                        new BigDecimal(0),
                        BigDecimal::add
                );

        BigDecimal totalMuligedagsverk = kvartalsvisSykefravær
                .stream()
                .map(UmaskertSykefraværForEttKvartal::getDagsverkNevner)
                .reduce(
                        new BigDecimal(0),
                        BigDecimal::add
                );

        int maksAntallPersoner = kvartalsvisSykefravær
                .stream()
                .map(e -> e.getAntallPersoner())
                .max(Integer::compare)
                .orElse(0);

        return new SummertSykefravær(
                totalTaptedagsverk,
                totalMuligedagsverk,
                maksAntallPersoner,
                kvartalsvisSykefravær.stream().map( k -> k.getÅrstallOgKvartal()).collect(Collectors.toList())
        );
    }

}

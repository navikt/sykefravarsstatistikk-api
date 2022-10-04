package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SummertSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartalMedVarighet;

@Data
@AllArgsConstructor
public class SummertKorttidsOgLangtidsfravær {

    private SummertSykefravær summertKorttidsfravær;

    private SummertSykefravær summertLangtidsfravær;


    public static SummertKorttidsOgLangtidsfravær getSummertKorttidsOgLangtidsfravær(
            ÅrstallOgKvartal sistePubliserteÅrstallOgKvartal,
            int antallKvartalerSomSkalSummeres,
            List<UmaskertSykefraværForEttKvartalMedVarighet> sykefraværVarighet
    ) {
        List<ÅrstallOgKvartal> kvartalerSomSkalSummeres = ÅrstallOgKvartal.range(
                sistePubliserteÅrstallOgKvartal.minusKvartaler(antallKvartalerSomSkalSummeres - 1),
                sistePubliserteÅrstallOgKvartal
        );

        Map<ÅrstallOgKvartal, List<UmaskertSykefraværForEttKvartalMedVarighet>> årstallOgKvartals =
                sykefraværVarighet.stream()
                        .filter(v -> kvartalerSomSkalSummeres.contains(v.getÅrstallOgKvartal()))
                        .collect(
                                Collectors.groupingBy(
                                        UmaskertSykefraværForEttKvartalMedVarighet::getÅrstallOgKvartal
                                )
                        );

        List<UmaskertSykefraværForEttKvartal> korttidsfravær = new ArrayList<>();
        List<UmaskertSykefraværForEttKvartal> langtidsfravær = new ArrayList<>();

        årstallOgKvartals.forEach(
                (årstallOgKvartal, sykefraværForEttKvartal) ->
                {
                    korttidsfravær.add(
                            summerSykefraværPåVarighet(
                                    årstallOgKvartal,
                                    sykefraværForEttKvartal,
                                    "korttid"
                            )
                    );
                    langtidsfravær.add(
                            summerSykefraværPåVarighet(
                                    årstallOgKvartal,
                                    sykefraværForEttKvartal,
                                    "langtid"
                            )
                    );
                }
        );

        korttidsfravær.sort(UmaskertSykefraværForEttKvartal::compareTo);
        langtidsfravær.sort(UmaskertSykefraværForEttKvartal::compareTo);

        return new SummertKorttidsOgLangtidsfravær(
                getSummertSykefravær(korttidsfravær),
                getSummertSykefravær(langtidsfravær)
        );
    }

    // TODO: bruk SummertSykefravær.getSummertSykefravær() i stedet
    @Deprecated
    private static SummertSykefravær getSummertSykefravær(
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

    private static UmaskertSykefraværForEttKvartal summerSykefraværPåVarighet(
            ÅrstallOgKvartal årstallOgKvartal,
            List<UmaskertSykefraværForEttKvartalMedVarighet> sykefraværVarighet,
            String korttidEllerLangtid
    ) {
        return sykefraværVarighet.stream()
                .filter(p -> {
                    if (p.getVarighet().equals(Varighetskategori.TOTAL)) {
                        return true;
                    }
                    if ("korttid".equals(korttidEllerLangtid)) {
                        return p.getVarighet().erKorttidVarighet();
                    } else {
                        return p.getVarighet().erLangtidVarighet();
                    }
                })
                .map(UmaskertSykefraværForEttKvartalMedVarighet::tilUmaskertSykefraværForEttKvartal)
                .reduce(
                        new UmaskertSykefraværForEttKvartal(årstallOgKvartal, ZERO, ZERO, 0),
                        UmaskertSykefraværForEttKvartal::add
                );
    }
}



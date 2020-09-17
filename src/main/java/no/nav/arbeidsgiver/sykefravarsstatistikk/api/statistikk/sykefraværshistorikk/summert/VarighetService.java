package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SummertSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartalMedVarighet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class VarighetService {

    private final VarighetRepository varighetRepository;

    public VarighetService(
            VarighetRepository varighetRepository) {
        this.varighetRepository = varighetRepository;
    }

    public SummertKorttidsOgLangtidsfravær hentSummertKorttidsOgLangtidsfravær(
            Underenhet underenhet,
            ÅrstallOgKvartal sistePubliserteÅrstallOgKvartal,
            int antallKvartalerSomSkalSummeres
    ) {
        if (antallKvartalerSomSkalSummeres < 1) {
            throw new IllegalArgumentException("Kan ikke summere færre enn ett kvartal");
        }

        List<UmaskertSykefraværForEttKvartalMedVarighet> sykefraværVarighet =
                varighetRepository.hentSykefraværForEttKvartalMedVarighet(
                        underenhet
                );

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


    private SummertSykefravær getSummertSykefravær(
            List<UmaskertSykefraværForEttKvartal> kvartalsvisSykefravær
    ) {

        BigDecimal totalTaptedagsverk = kvartalsvisSykefravær
                .stream()
                .map(e -> e.getTapteDagsverk())
                .reduce(
                        new BigDecimal(0),
                        BigDecimal::add
                );

        BigDecimal totalMuligedagsverk = kvartalsvisSykefravær
                .stream()
                .map(e -> e.getMuligeDagsverk())
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

    private UmaskertSykefraværForEttKvartal summerSykefraværPåVarighet(
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
                        UmaskertSykefraværForEttKvartal.tomtUmaskertKvartalsvisSykefravær(årstallOgKvartal),
                        UmaskertSykefraværForEttKvartal::add
                );
    }
}

package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.common.Sykefraværsvarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.sykefravær.SykefraværSiste4Kvartaler;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class VarighetService {

    private final KvartalsvisSykefraværVarighetRepository kvartalsvisSykefraværVarighetRepository;

    public VarighetService(
            KvartalsvisSykefraværVarighetRepository kvartalsvisSykefraværVarighetRepository) {
        this.kvartalsvisSykefraværVarighetRepository = kvartalsvisSykefraværVarighetRepository;
    }


    public KorttidsOgLangtidsfraværSiste4Kvartaler hentKorttidsOgLangtidsfraværSiste4Kvartaler(
            Underenhet underenhet,
            ÅrstallOgKvartal sistePubliserteÅrstallOgKvartal
    ) {

        List<UmaskertKvartalsvisSykefraværMedVarighet> sykefraværVarighet =
                kvartalsvisSykefraværVarighetRepository.hentKvartalsvisSykefraværMedVarighet(
                        underenhet
                );

        List<ÅrstallOgKvartal> gjeldendeÅrstallOgKvartal = ÅrstallOgKvartal.range(
                sistePubliserteÅrstallOgKvartal.minusKvartaler(3),
                sistePubliserteÅrstallOgKvartal
        );

        Map<ÅrstallOgKvartal, List<UmaskertKvartalsvisSykefraværMedVarighet>> årstallOgKvartals =
                sykefraværVarighet.stream()
                        .filter(v -> gjeldendeÅrstallOgKvartal.contains(v.getÅrstallOgKvartal()))
                        .collect(
                                Collectors.groupingBy(
                                        UmaskertKvartalsvisSykefraværMedVarighet::getÅrstallOgKvartal
                                )
                        );

        List<UmaskertKvartalsvisSykefravær> korttidKVartalsvisSykefravar = new ArrayList<>();
        List<UmaskertKvartalsvisSykefravær> langtidKVartalsvisSykefravar = new ArrayList<>();

        årstallOgKvartals.forEach(
                (årstallOgKvartal, kvartalsvisSykefraværMedVarighets) ->
                {
                    korttidKVartalsvisSykefravar.add(
                            getSummertSykefravær(
                                    årstallOgKvartal,
                                    kvartalsvisSykefraværMedVarighets,
                                    "korttid"
                            )
                    );
                    langtidKVartalsvisSykefravar.add(
                            getSummertSykefravær(
                                    årstallOgKvartal,
                                    kvartalsvisSykefraværMedVarighets,
                                    "langtid"
                            )
                    );
                }
        );

        korttidKVartalsvisSykefravar.sort(UmaskertKvartalsvisSykefravær::compareTo);
        langtidKVartalsvisSykefravar.sort(UmaskertKvartalsvisSykefravær::compareTo);

        return new KorttidsOgLangtidsfraværSiste4Kvartaler(
                getSiste4KvartalerSykefravær(korttidKVartalsvisSykefravar),
                getSiste4KvartalerSykefravær(langtidKVartalsvisSykefravar)
        );
    }


    private SykefraværSiste4Kvartaler getSiste4KvartalerSykefravær(
            List<UmaskertKvartalsvisSykefravær> kvartalsvisSykefravær
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

        return new SykefraværSiste4Kvartaler(
                totalTaptedagsverk,
                totalMuligedagsverk,
                maksAntallPersoner,
                kvartalsvisSykefravær.stream().map( k -> k.getÅrstallOgKvartal()).collect(Collectors.toList())
        );
    }

    private UmaskertKvartalsvisSykefravær getSummertSykefravær(
            ÅrstallOgKvartal årstallOgKvartal,
            List<UmaskertKvartalsvisSykefraværMedVarighet> sykefraværVarighet,
            String korttidEllerLangtid
    ) {
        return sykefraværVarighet.stream()
                .filter(p -> {
                    if (p.getVarighet().equals(Sykefraværsvarighet.TOTAL)) {
                        return true;
                    }
                    if ("korttid".equals(korttidEllerLangtid)) {
                        return p.getVarighet().erKorttidVarighet();
                    } else {
                        return p.getVarighet().erLangtidVarighet();
                    }
                })
                .map(UmaskertKvartalsvisSykefraværMedVarighet::tilUmaskertKvartalsvisSykefravær)
                .reduce(
                        UmaskertKvartalsvisSykefravær.tomtUmaskertKvartalsvisSykefravær(årstallOgKvartal),
                        UmaskertKvartalsvisSykefravær::add
                );
    }
}

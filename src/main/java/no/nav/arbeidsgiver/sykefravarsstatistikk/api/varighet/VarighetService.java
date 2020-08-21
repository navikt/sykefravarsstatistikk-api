package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.common.Sykefraværsvarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

        // #1 vi henter alle umaskerte kvartalsvis sykefravær med varighet
        // OBS det kan være hul i serien og vi må ta hensyn til det
        // (dvs de fire siste kvartalene skal ikke være for gamle) TODO: filtrere bort de som er for gamle
        List<UmaskertKvartalsvisSykefraværMedVarighet> sykefraværVarighet =
                kvartalsvisSykefraværVarighetRepository.hentKvartalsvisSykefraværMedVarighet(
                        underenhet
                );

        List<ÅrstallOgKvartal> gjeldendeÅrstallOgKvartal = ÅrstallOgKvartal.range(
                sistePubliserteÅrstallOgKvartal.minusKvartaler(3),
                sistePubliserteÅrstallOgKvartal
        );

        // #2 Grupper sammen disse UmaskertKvartalsvisSykefraværMedVarighet per ÅrstallOgKvartal
        Map<ÅrstallOgKvartal, List<UmaskertKvartalsvisSykefraværMedVarighet>> årstallOgKvartals =
                sykefraværVarighet.stream()
                        .filter( v -> gjeldendeÅrstallOgKvartal.contains(v.getÅrstallOgKvartal()))
                        .collect(
                                Collectors.groupingBy(
                                        UmaskertKvartalsvisSykefraværMedVarighet::getÅrstallOgKvartal
                                )
                        );

        // #3 Her skiller vi korttids sykefravære fra langtiddssykefravære i to forskjellige lister
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

        // #4 Sette sammen korttid og langtid i et objekt som vi returnerer
        KorttidsEllerLangtidsfraværSiste4Kvartaler korttidSiste4KvartalerSykefravær =
                new KorttidsEllerLangtidsfraværSiste4Kvartaler();
        korttidSiste4KvartalerSykefravær.setLangtidEllerKorttid("korttid");
        korttidSiste4KvartalerSykefravær.setSiste4KvartalerSykefravær(
                getSiste4KvartalerSykefravær(korttidKVartalsvisSykefravar
                )
        );

        KorttidsEllerLangtidsfraværSiste4Kvartaler langtidSiste4KvartalerSykefravær =
                new KorttidsEllerLangtidsfraværSiste4Kvartaler();
        langtidSiste4KvartalerSykefravær.setLangtidEllerKorttid("langtid");
        langtidSiste4KvartalerSykefravær.setSiste4KvartalerSykefravær(getSiste4KvartalerSykefravær(langtidKVartalsvisSykefravar));

        KorttidsOgLangtidsfraværSiste4Kvartaler korttidsOgLangtidsfraværSiste4Kvartaler = new KorttidsOgLangtidsfraværSiste4Kvartaler();
        korttidsOgLangtidsfraværSiste4Kvartaler.setKorttidsfraværSiste4Kvartaler(korttidSiste4KvartalerSykefravær);
        korttidsOgLangtidsfraværSiste4Kvartaler.setLangtidsfraværSiste4Kvartaler(langtidSiste4KvartalerSykefravær);

        return korttidsOgLangtidsfraværSiste4Kvartaler;
    }


    private Siste4KvartalerSykefravær getSiste4KvartalerSykefravær(
            List<UmaskertKvartalsvisSykefravær> kvartalsvisSykefravær
    ) {

        BigDecimal totalTaptedagsverk = kvartalsvisSykefravær
                .stream()
                .map( e -> e.getTapteDagsverk())
                .reduce(
                        new BigDecimal(0),
                        BigDecimal::add
                );

        BigDecimal totalMuligedagsverk = kvartalsvisSykefravær
                .stream()
                .map( e -> e.getMuligeDagsverk())
                .reduce(
                        new BigDecimal(0),
                        BigDecimal::add
                );

        int maksAntallPersoner = kvartalsvisSykefravær
                .stream()
                .map( e -> e.getAntallPersoner())
                .max(Integer::compare)
                .get();

        return new Siste4KvartalerSykefravær(totalTaptedagsverk, totalMuligedagsverk, maksAntallPersoner);
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

package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.common.Sykefraværsvarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.sykefraværshistorikk.KvartalsvisSykefravær;
import org.jetbrains.annotations.NotNull;
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


    public LangtidOgKorttidsSykefraværshistorikk hentLangtidOgKorttidsSykefraværshistorikk(Underenhet underenhet) {

        List<UmaskertKvartalsvisSykefraværMedVarighet> sykefraværVarighet =
                kvartalsvisSykefraværVarighetRepository.hentKvartalsvisSykefraværMedVarighet(
                        underenhet);

        Map<ÅrstallOgKvartal, List<UmaskertKvartalsvisSykefraværMedVarighet>> årstallOgKvartals = sykefraværVarighet.stream()
                .collect(Collectors.
                        groupingBy(UmaskertKvartalsvisSykefraværMedVarighet::getÅrstallOgKvartal));

        List<Siste4KvartalerSykefravær> korttidKVartalsvisSykefravar = new ArrayList<>();
        List<Siste4KvartalerSykefravær> langtidKVartalsvisSykefravar = new ArrayList<>();

        årstallOgKvartals.forEach(
                (årstallOgKvartal, kvartalsvisSykefraværMedVarighets) ->
                {
                    korttidKVartalsvisSykefravar.add(getKorttidSykefraværMedVarighetshistorikk
                            (kvartalsvisSykefraværMedVarighets, årstallOgKvartal));
                    langtidKVartalsvisSykefravar.add(getLangtidSykefraværMedVarighetshistorikk(
                            kvartalsvisSykefraværMedVarighets, årstallOgKvartal));
                }
        );
        SykefraværMedVarighetshistorikk korttidSykefravarMedVarighetHistorikk =
                new SykefraværMedVarighetshistorikk();
        korttidSykefravarMedVarighetHistorikk.setVarighet("korttid");



        SykefraværMedVarighetshistorikk langtidSykefravarMedVarighetHistorikk =
                new SykefraværMedVarighetshistorikk();
        langtidSykefravarMedVarighetHistorikk.setVarighet("langtid");
        langtidSykefravarMedVarighetHistorikk.setSiste4KvartalerSykefravær(langtidKVartalsvisSykefravar);

        LangtidOgKorttidsSykefraværshistorikk langtidOgKorttidsSykefraværshistorikk = new LangtidOgKorttidsSykefraværshistorikk();
        langtidOgKorttidsSykefraværshistorikk.setLangtidssykefravær(korttidSykefravarMedVarighetHistorikk);
        langtidOgKorttidsSykefraværshistorikk.setLangtidssykefravær(langtidSykefravarMedVarighetHistorikk);
        return langtidOgKorttidsSykefraværshistorikk;
        // return getLangtidOgKorttidsSykefraværshistorikk(sykefraværVarighet);
    }


    @NotNull
    private Siste4KvartalerSykefravær getLangtidSykefraværMedVarighetshistorikk
            (List<UmaskertKvartalsvisSykefraværMedVarighet> sykefraværVarighet, ÅrstallOgKvartal årstallOgKvartal) {
        BigDecimal muligeDagsverk = sykefraværVarighet.stream()
                .filter(p ->
                        p.getVarighet().equals(Sykefraværsvarighet.TOTAL)
                ).collect(Collectors.toList())
                .get(0)
                .getMuligeDagsverk();
        int antallPers = sykefraværVarighet.stream()
                .filter(p ->
                        p.getVarighet().equals(Sykefraværsvarighet.TOTAL)
                ).collect(Collectors.toList())
                .get(0)
                .getAntallPersoner();

        BigDecimal tapteDagsverkLangtid = sykefraværVarighet.stream()
                .filter(p ->
                        p.getVarighet().erLangtidVarighet()
                ).collect(Collectors.toList())
                .get(0)
                .getTapteDagsverk();
       /* SykefraværMedVarighetshistorikk langtid = new SykefraværMedVarighetshistorikk();
        langtid.setVarighet("langtid");
        langtid.setKvartalsvisSykefravær(Arrays.asList(
                new KvartalsvisSykefravær
                        (årstallOgKvartal, tapteDagsverkLangtid, muligeDagsverk, antallPers)
        ));*/
        return new Siste4KvartalerSykefravær(tapteDagsverkLangtid, muligeDagsverk, antallPers);
    }

    @NotNull
    private Siste4KvartalerSykefravær getKorttidSykefraværMedVarighetshistorikk
            (List<UmaskertKvartalsvisSykefraværMedVarighet> sykefraværVarighet, ÅrstallOgKvartal årstallOgKvartal) {

        BigDecimal muligeDagsverk = sykefraværVarighet.stream()
                .filter(p ->
                        p.getVarighet().equals(Sykefraværsvarighet.TOTAL)
                ).collect(Collectors.toList())
                .get(0)
                .getMuligeDagsverk();
        int antallPers = sykefraværVarighet.stream()
                .filter(p ->
                        p.getVarighet().equals(Sykefraværsvarighet.TOTAL)
                ).collect(Collectors.toList())
                .get(0)
                .getAntallPersoner();

        BigDecimal tapteDagsverkKorttid = sykefraværVarighet.stream()
                .filter(p ->
                        p.getVarighet().erKorttidVarighet()
                ).collect(Collectors.toList())
                .get(0)
                .getTapteDagsverk();

        return new Siste4KvartalerSykefravær(tapteDagsverkKorttid, muligeDagsverk, antallPers);
    }
}

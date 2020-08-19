package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.common.Sykefraværsvarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.sykefraværshistorikk.KvartalsvisSykefravær;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
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

        List<KvartalsvisSykefraværMedVarighet> sykefraværVarighet =
                kvartalsvisSykefraværVarighetRepository.hentKvartalsvisSykefraværMedVarighet(
                        underenhet);
        ÅrstallOgKvartal årstallOgKvartal = sykefraværVarighet.get(0).getÅrstallOgKvartal();
        BigDecimal muligeDagsverk = sykefraværVarighet.stream()
                .filter(p ->
                        p.getTypeVarighet().equals(Sykefraværsvarighet.TOTAL.kode)
                ).collect(Collectors.toList())
                .get(0)
                .getMuligeDagsverk();
        int antallPers = sykefraværVarighet.stream()
                .filter(p ->
                        p.getTypeVarighet().equals(Sykefraværsvarighet.TOTAL.kode)
                ).collect(Collectors.toList())
                .get(0)
                .getAntallPersoner();
        BigDecimal tapteDagsverkKorttid = sykefraværVarighet.stream()
                .filter(p ->
                        p.getTypeVarighet().equals(Sykefraværsvarighet._1_DAG_TIL_7_DAGER.kode)
                ).collect(Collectors.toList())
                .get(0)
                .getTapteDagsverk();
        BigDecimal tapteDagsverkLangtid = sykefraværVarighet.stream()
                .filter(p ->
                        p.getTypeVarighet().equals(Sykefraværsvarighet.MER_ENN_39_UKER.kode)
                ).collect(Collectors.toList())
                .get(0)
                .getTapteDagsverk();

        SykefraværMedVarighetshistorikk korttid = new SykefraværMedVarighetshistorikk();
        korttid.setVarighet("korttid");
        korttid.setKvartalsvisSykefravær(Arrays.asList(
                new KvartalsvisSykefravær
                        (årstallOgKvartal,tapteDagsverkKorttid,muligeDagsverk,antallPers)
        ));
        SykefraværMedVarighetshistorikk langtid = new SykefraværMedVarighetshistorikk();
        langtid.setVarighet("langtid");
        langtid.setKvartalsvisSykefravær(Arrays.asList(
                new KvartalsvisSykefravær
                        (årstallOgKvartal,tapteDagsverkLangtid,muligeDagsverk,antallPers)
        ));
        LangtidOgKorttidsSykefraværshistorikk langtidOgKorttidsSykefraværshistorikk =
                new LangtidOgKorttidsSykefraværshistorikk();
        langtidOgKorttidsSykefraværshistorikk.setKorttidssykefravær(korttid);
        langtidOgKorttidsSykefraværshistorikk.setLangtidssykefravær(langtid);
        return langtidOgKorttidsSykefraværshistorikk;
    }
}

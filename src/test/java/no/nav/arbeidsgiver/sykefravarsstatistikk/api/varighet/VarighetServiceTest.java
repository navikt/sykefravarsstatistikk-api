package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.common.Sykefraværsvarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class VarighetServiceTest {

    @Mock
    private KvartalsvisSykefraværVarighetRepository kvartalsvisSykefraværVarighetRepository;

    @InjectMocks
    VarighetService varighetService;

    private Underenhet barnehage;

    @BeforeEach
    public void setUp() {
        barnehage = Underenhet.builder().orgnr(new Orgnr("999999999"))
                .navn("test Barnehage")
                .næringskode(new Næringskode5Siffer("88911", "Barnehage"))
                .antallAnsatte(10)
                .overordnetEnhetOrgnr(new Orgnr("1111111111")).build();


    }


    @Test
    public void hentLangtidOgKorttidsSykefraværshistorikk__skal_returnere_langtid_og_korttid_historikk() {
        when(kvartalsvisSykefraværVarighetRepository.hentKvartalsvisSykefraværMedVarighet(any()))
                .thenReturn(
                        lagKvartalsvisSykefravarMedVarighet1kvaartal()
                );

        LangtidOgKorttidsSykefraværshistorikk langtidOgKorttidsSykefraværshistorikk =
                varighetService.hentLangtidOgKorttidsSykefraværshistorikk(barnehage);

        SykefraværMedVarighetshistorikk korttidssykefravær =
                langtidOgKorttidsSykefraværshistorikk.getKorttidssykefravær();
        SykefraværMedVarighetshistorikk langtidssykefravær =
                langtidOgKorttidsSykefraværshistorikk.getLangtidssykefravær();

        assertThat(korttidssykefravær.getKvartalsvisSykefravær().size()).isEqualTo(1);
        assertThat(korttidssykefravær.getKvartalsvisSykefravær().get(0).getTapteDagsverk()).isEqualTo(BigDecimal.valueOf(5.0));
        assertThat(korttidssykefravær.getVarighet()).isEqualTo("korttid");
        assertThat(langtidssykefravær.getKvartalsvisSykefravær().size()).isEqualTo(1);
        assertThat(langtidssykefravær.getKvartalsvisSykefravær().get(0).getTapteDagsverk()).isEqualTo(BigDecimal.valueOf(2.0));
        assertThat(langtidssykefravær.getVarighet()).isEqualTo("langtid");
    }

    @Test
    public void hentLangtidOgKorttidsSykefraværshistorikk__skal_returnere_langtid_og_korttid_historikk_for_flere_kvartaler() {
        when(kvartalsvisSykefraværVarighetRepository.hentKvartalsvisSykefraværMedVarighet(any()))
                .thenReturn(
                        lagKvartalsvisSykefravarMedVarighet2Kvartaler()
                );

        LangtidOgKorttidsSykefraværshistorikk langtidOgKorttidsSykefraværshistorikk =
                varighetService.hentLangtidOgKorttidsSykefraværshistorikk(barnehage);

        SykefraværMedVarighetshistorikk korttidssykefravær =
                langtidOgKorttidsSykefraværshistorikk.getKorttidssykefravær();
        SykefraværMedVarighetshistorikk langtidssykefravær =
                langtidOgKorttidsSykefraværshistorikk.getLangtidssykefravær();

        assertThat(korttidssykefravær.getKvartalsvisSykefravær().size()).isEqualTo(2);
        assertThat(korttidssykefravær.getKvartalsvisSykefravær().get(0).getTapteDagsverk()).isEqualTo(BigDecimal.valueOf(9.0));
        assertThat(korttidssykefravær.getKvartalsvisSykefravær().get(1).getTapteDagsverk()).isEqualTo(BigDecimal.valueOf(5.0));
        assertThat(korttidssykefravær.getVarighet()).isEqualTo("korttid");
        assertThat(langtidssykefravær.getKvartalsvisSykefravær().size()).isEqualTo(2);
        assertThat(langtidssykefravær.getKvartalsvisSykefravær().get(0).getTapteDagsverk()).isEqualTo(BigDecimal.valueOf(6.0));
        assertThat(langtidssykefravær.getKvartalsvisSykefravær().get(1).getTapteDagsverk()).isEqualTo(BigDecimal.valueOf(2.0));
        assertThat(langtidssykefravær.getVarighet()).isEqualTo("langtid");
    }

    private List<UmaskertKvartalsvisSykefraværMedVarighet> lagKvartalsvisSykefravarMedVarighet1kvaartal() {
        return Arrays.asList(
                new UmaskertKvartalsvisSykefraværMedVarighet(
                        new ÅrstallOgKvartal(2020, 1),
                        BigDecimal.valueOf(5),
                        BigDecimal.valueOf(0),
                        0,
                        Sykefraværsvarighet._1_DAG_TIL_7_DAGER
                ),
                new UmaskertKvartalsvisSykefraværMedVarighet(
                        new ÅrstallOgKvartal(2020, 1),
                        BigDecimal.valueOf(2),
                        BigDecimal.valueOf(0),
                        0,
                        Sykefraværsvarighet.MER_ENN_39_UKER
                ),
                new UmaskertKvartalsvisSykefraværMedVarighet(
                        new ÅrstallOgKvartal(2020, 1),
                        BigDecimal.valueOf(0),
                        BigDecimal.valueOf(100),
                        10,
                        Sykefraværsvarighet.TOTAL
                )

        )
                ;
    }

    private List<UmaskertKvartalsvisSykefraværMedVarighet> lagKvartalsvisSykefravarMedVarighet2Kvartaler() {
        return Arrays.asList(
                new UmaskertKvartalsvisSykefraværMedVarighet(
                        new ÅrstallOgKvartal(2019, 4),
                        BigDecimal.valueOf(9),
                        BigDecimal.valueOf(0),
                        0,
                        Sykefraværsvarighet._8_DAGER_TIL_16_DAGER
                ),
                new UmaskertKvartalsvisSykefraværMedVarighet(
                        new ÅrstallOgKvartal(2019, 4),
                        BigDecimal.valueOf(6),
                        BigDecimal.valueOf(0),
                        0,
                        Sykefraværsvarighet._17_DAGER_TIL_8_UKER
                ),
                new UmaskertKvartalsvisSykefraværMedVarighet(
                        new ÅrstallOgKvartal(2019, 4),
                        BigDecimal.valueOf(0),
                        BigDecimal.valueOf(100),
                        10,
                        Sykefraværsvarighet.TOTAL
                ),
                new UmaskertKvartalsvisSykefraværMedVarighet(
                        new ÅrstallOgKvartal(2020, 1),
                        BigDecimal.valueOf(5),
                        BigDecimal.valueOf(0),
                        0,
                        Sykefraværsvarighet._1_DAG_TIL_7_DAGER
                ),
                new UmaskertKvartalsvisSykefraværMedVarighet(
                        new ÅrstallOgKvartal(2020, 1),
                        BigDecimal.valueOf(2),
                        BigDecimal.valueOf(0),
                        0,
                        Sykefraværsvarighet.MER_ENN_39_UKER
                ),
                new UmaskertKvartalsvisSykefraværMedVarighet(
                        new ÅrstallOgKvartal(2020, 1),
                        BigDecimal.valueOf(0),
                        BigDecimal.valueOf(100),
                        10,
                        Sykefraværsvarighet.TOTAL
                )

        )
                ;
    }

}
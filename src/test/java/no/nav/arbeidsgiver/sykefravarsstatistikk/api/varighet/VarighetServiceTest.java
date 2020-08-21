package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.common.Sykefraværsvarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VarighetServiceTest {

    @Mock
    private KvartalsvisSykefraværVarighetRepository kvartalsvisSykefraværVarighetRepository;

    @InjectMocks
    VarighetService varighetService;

    private Underenhet barnehage;
    private final ÅrstallOgKvartal sistePublisertÅrstallOgKvartal = new ÅrstallOgKvartal(2020, 1);

    @BeforeEach
    public void setUp() {
        barnehage = Underenhet.builder().orgnr(new Orgnr("999999999"))
                .navn("test Barnehage")
                .næringskode(new Næringskode5Siffer("88911", "Barnehage"))
                .antallAnsatte(10)
                .overordnetEnhetOrgnr(new Orgnr("1111111111")).build();
    }

    @Test
    public void hentKorttidsOgLangtidsfraværSiste4Kvartaler__skal_returnere_langtid_og_korttid_historikk() {
        List<UmaskertKvartalsvisSykefraværMedVarighet> sykefraværMed1Kvartal = Arrays.asList(
                getSykefraværMedVarighet(sistePublisertÅrstallOgKvartal, 5, 0, 0, Sykefraværsvarighet._1_DAG_TIL_7_DAGER),
                getSykefraværMedVarighet(sistePublisertÅrstallOgKvartal, 2, 0, 0, Sykefraværsvarighet.MER_ENN_39_UKER),
                getSykefraværMedVarighet(sistePublisertÅrstallOgKvartal, 0, 100, 10, Sykefraværsvarighet.TOTAL)
        );
        when(kvartalsvisSykefraværVarighetRepository.hentKvartalsvisSykefraværMedVarighet(any())).thenReturn(
                sykefraværMed1Kvartal
        );

        KorttidsOgLangtidsfraværSiste4Kvartaler korttidsOgLangtidsfraværSiste4Kvartaler =
                varighetService.hentKorttidsOgLangtidsfraværSiste4Kvartaler(barnehage, new ÅrstallOgKvartal(2020, 1));

        assertIsEqual(
                korttidsOgLangtidsfraværSiste4Kvartaler.getKorttidsfraværSiste4Kvartaler(),
                100,
                5,
                5,
                false
        );
        assertIsEqual(
                korttidsOgLangtidsfraværSiste4Kvartaler.getLangtidsfraværSiste4Kvartaler(),
                100,
                2,
                2,
                false
        );
    }

    @Test
    public void hentKorttidsOgLangtidsfraværSiste4Kvartaler__skal_returnere_langtid_og_korttid_historikk_for_flere_kvartaler() {
        ÅrstallOgKvartal kvartal1 = new ÅrstallOgKvartal(2019, 4);
        ÅrstallOgKvartal kvartal2 = new ÅrstallOgKvartal(2020, 1);

        List<UmaskertKvartalsvisSykefraværMedVarighet> sykefraværMed2Kvartaler = Arrays.asList(
                getSykefraværMedVarighet(kvartal1,9, 0, 0, Sykefraværsvarighet._8_DAGER_TIL_16_DAGER),
                getSykefraværMedVarighet(kvartal1, 6, 0, 0, Sykefraværsvarighet._17_DAGER_TIL_8_UKER),
                getSykefraværMedVarighet(kvartal1, 0, 100, 10, Sykefraværsvarighet.TOTAL),

                getSykefraværMedVarighet(kvartal2, 5, 0, 0, Sykefraværsvarighet._1_DAG_TIL_7_DAGER),
                getSykefraværMedVarighet(kvartal2, 2, 0, 0, Sykefraværsvarighet.MER_ENN_39_UKER),
                getSykefraværMedVarighet(kvartal2, 0, 100, 10, Sykefraværsvarighet.TOTAL)
        );

        when(kvartalsvisSykefraværVarighetRepository.hentKvartalsvisSykefraværMedVarighet(any())).thenReturn(
                sykefraværMed2Kvartaler
        );

        KorttidsOgLangtidsfraværSiste4Kvartaler korttidsOgLangtidsfraværSiste4Kvartaler =
                varighetService.hentKorttidsOgLangtidsfraværSiste4Kvartaler(barnehage, new ÅrstallOgKvartal(2020, 1));

        assertIsEqual(
                korttidsOgLangtidsfraværSiste4Kvartaler.getKorttidsfraværSiste4Kvartaler(),
                200,
                14,
                7,
                false
        );
        assertIsEqual(
                korttidsOgLangtidsfraværSiste4Kvartaler.getLangtidsfraværSiste4Kvartaler(),
                200,
                8,
                4,
                false
        );
    }

    @Test
    public void hentKorttidsOgLangtidsfraværSiste4Kvartaler__skal_summere_opp_riktig_hvis_det_er_flere_innslag_med_samme_varighet_i_hvert_kvartal() {
        ÅrstallOgKvartal kvartal = new ÅrstallOgKvartal(2020, 1);
        List<UmaskertKvartalsvisSykefraværMedVarighet> sykefraværMed1Kvartal = Arrays.asList(
                getSykefraværMedVarighet(kvartal, 3000, 0, 0, Sykefraværsvarighet._1_DAG_TIL_7_DAGER),
                getSykefraværMedVarighet(kvartal, 2000, 0, 0, Sykefraværsvarighet._8_DAGER_TIL_16_DAGER),

                getSykefraværMedVarighet(kvartal, 1000, 0, 0, Sykefraværsvarighet._17_DAGER_TIL_8_UKER),
                getSykefraværMedVarighet(kvartal, 100, 0, 0, Sykefraværsvarighet._8_UKER_TIL_20_UKER),
                getSykefraværMedVarighet(kvartal, 10, 0, 0, Sykefraværsvarighet._20_UKER_TIL_39_UKER),
                getSykefraværMedVarighet(kvartal, 1, 0, 0, Sykefraværsvarighet.MER_ENN_39_UKER),

                getSykefraværMedVarighet(kvartal, 0, 100000, 2000, Sykefraværsvarighet.TOTAL)
        );

        when(kvartalsvisSykefraværVarighetRepository.hentKvartalsvisSykefraværMedVarighet(any())).thenReturn(
                sykefraværMed1Kvartal
        );

        KorttidsOgLangtidsfraværSiste4Kvartaler korttidsOgLangtidsfraværSiste4Kvartaler =
                varighetService.hentKorttidsOgLangtidsfraværSiste4Kvartaler(barnehage, new ÅrstallOgKvartal(2020, 1));

        assertIsEqual(
                korttidsOgLangtidsfraværSiste4Kvartaler.getKorttidsfraværSiste4Kvartaler(),
                100000,
                5000,
                5,
                false
        );
        assertIsEqual(
                korttidsOgLangtidsfraværSiste4Kvartaler.getLangtidsfraværSiste4Kvartaler(),
                100000,
                1111,
                1.1f,
                false
        );
    }

    @Test
    public void hentKorttidsOgLangtidsfraværSiste4Kvartaler__filtrerer_bort_kvartaler_som_er_eldre_enn_de_4_siste() {
        ÅrstallOgKvartal sistePublisertKvartal = new ÅrstallOgKvartal(2020, 1);
        ÅrstallOgKvartal utdatertKvartal = sistePublisertKvartal.minusKvartaler(4);
        ÅrstallOgKvartal ikkeUtdatertKvartal = sistePublisertKvartal.minusKvartaler(2);

        List<UmaskertKvartalsvisSykefraværMedVarighet> umaskertKvartalsvisSykefraværMedVarighet = Arrays.asList(
                getSykefraværMedVarighet(utdatertKvartal, 10, 0, 0, Sykefraværsvarighet._1_DAG_TIL_7_DAGER),
                getSykefraværMedVarighet(utdatertKvartal, 20, 0, 0, Sykefraværsvarighet.MER_ENN_39_UKER),
                getSykefraværMedVarighet(utdatertKvartal, 0, 500, 2000, Sykefraværsvarighet.TOTAL),

                getSykefraværMedVarighet(ikkeUtdatertKvartal, 10, 0, 0, Sykefraværsvarighet._1_DAG_TIL_7_DAGER),
                getSykefraværMedVarighet(ikkeUtdatertKvartal, 20, 0, 0, Sykefraværsvarighet.MER_ENN_39_UKER),
                getSykefraværMedVarighet(ikkeUtdatertKvartal, 0, 100, 200, Sykefraværsvarighet.TOTAL)
        );

        when(kvartalsvisSykefraværVarighetRepository.hentKvartalsvisSykefraværMedVarighet(any())).thenReturn(
                umaskertKvartalsvisSykefraværMedVarighet
        );

        KorttidsOgLangtidsfraværSiste4Kvartaler korttidsOgLangtidsfraværSiste4Kvartaler =
                varighetService.hentKorttidsOgLangtidsfraværSiste4Kvartaler(barnehage, new ÅrstallOgKvartal(2020, 1));

        assertIsEqual(
                korttidsOgLangtidsfraværSiste4Kvartaler.getKorttidsfraværSiste4Kvartaler(),
                100,
                10,
                10,
                false
        );
        assertIsEqual(
                korttidsOgLangtidsfraværSiste4Kvartaler.getLangtidsfraværSiste4Kvartaler(),
                100,
                20,
                20,
                false
        );
    }


    private UmaskertKvartalsvisSykefraværMedVarighet getSykefraværMedVarighet(
            ÅrstallOgKvartal årstallOgKvartal,
            int tapteDagsverk,
            int muligeDagsverk,
            int antallPersoner,
            Sykefraværsvarighet varighet
    ) {
        return new UmaskertKvartalsvisSykefraværMedVarighet(
                årstallOgKvartal,
                BigDecimal.valueOf(tapteDagsverk),
                BigDecimal.valueOf(muligeDagsverk),
                antallPersoner,
                varighet
        );
    }

    private void assertIsEqual(
            SykefraværSiste4Kvartaler korttidsEllerLangtidsfraværSiste4Kvartaler,
            int expectedMuligeDagsverk,
            int expectedTapteDagsverk,
            float expectedProsent,
            boolean expectedErMaskert
    ) {
        assertBigDecimalIsEqual(
                korttidsEllerLangtidsfraværSiste4Kvartaler.getMuligeDagsverk(),
                expectedMuligeDagsverk
        );
        assertBigDecimalIsEqual(
                korttidsEllerLangtidsfraværSiste4Kvartaler.getTapteDagsverk(),
                expectedTapteDagsverk
        );
        assertBigDecimalIsEqual(
                korttidsEllerLangtidsfraværSiste4Kvartaler.getProsent(),
                expectedProsent
        );
        assertThat(
                korttidsEllerLangtidsfraværSiste4Kvartaler.isErMaskert()
        )
                .isEqualTo(
                        expectedErMaskert
                );
    }

    private void assertBigDecimalIsEqual(BigDecimal actual, float expected) {
        assertThat(actual.setScale(6, RoundingMode.HALF_UP))
                .isEqualTo(BigDecimal.valueOf(expected).setScale(6, RoundingMode.HALF_UP));
    }

}

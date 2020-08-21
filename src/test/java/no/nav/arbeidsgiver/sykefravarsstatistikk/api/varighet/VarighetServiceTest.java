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
        ÅrstallOgKvartal kvartal = new ÅrstallOgKvartal(2020, 1);
        List<UmaskertKvartalsvisSykefraværMedVarighet> sykefraværMed1Kvartal = Arrays.asList(
                getSykefraværMedVarighet(kvartal, 5, 0, 0, Sykefraværsvarighet._1_DAG_TIL_7_DAGER),
                getSykefraværMedVarighet(kvartal, 2, 0, 0, Sykefraværsvarighet.MER_ENN_39_UKER),
                getSykefraværMedVarighet(kvartal, 0, 100, 10, Sykefraværsvarighet.TOTAL)
        );
        when(kvartalsvisSykefraværVarighetRepository.hentKvartalsvisSykefraværMedVarighet(any())).thenReturn(
                sykefraværMed1Kvartal
        );

        KorttidsOgLangtidsfraværSiste4Kvartaler korttidsOgLangtidsfraværSiste4Kvartaler =
                varighetService.hentKorttidsOgLangtidsfraværSiste4Kvartaler(barnehage);

        assertIsEqual(
                korttidsOgLangtidsfraværSiste4Kvartaler.getKorttidsfraværSiste4Kvartaler(),
                100,
                5,
                5,
                false,
                "korttid"
        );
        assertIsEqual(
                korttidsOgLangtidsfraværSiste4Kvartaler.getLangtidsfraværSiste4Kvartaler(),
                100,
                2,
                2,
                false,
                "langtid"
        );
    }

    @Test
    public void hentLangtidOgKorttidsSykefraværshistorikk__skal_returnere_langtid_og_korttid_historikk_for_flere_kvartaler() {
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
                varighetService.hentKorttidsOgLangtidsfraværSiste4Kvartaler(barnehage);

        assertIsEqual(
                korttidsOgLangtidsfraværSiste4Kvartaler.getKorttidsfraværSiste4Kvartaler(),
                200,
                14,
                7,
                false,
                "korttid"
        );
        assertIsEqual(
                korttidsOgLangtidsfraværSiste4Kvartaler.getLangtidsfraværSiste4Kvartaler(),
                200,
                8,
                4,
                false,
                "langtid"
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
            KorttidsEllerLangtidsfraværSiste4Kvartaler korttidsEllerLangtidsfraværSiste4Kvartaler,
            int expectedMuligeDagsverk,
            int expectedTapteDagsverk,
            int expectedProsent,
            boolean expectedErMaskert,
            String expectedLangtidEllerKorttid
    ) {
        assertBigDecimalIsEqual(
                korttidsEllerLangtidsfraværSiste4Kvartaler.getSiste4KvartalerSykefravær().getMuligeDagsverk(),
                expectedMuligeDagsverk
        );
        assertBigDecimalIsEqual(
                korttidsEllerLangtidsfraværSiste4Kvartaler.getSiste4KvartalerSykefravær().getTapteDagsverk(),
                expectedTapteDagsverk
        );
        assertBigDecimalIsEqual(
                korttidsEllerLangtidsfraværSiste4Kvartaler.getSiste4KvartalerSykefravær().getProsent(),
                expectedProsent
        );
        assertThat(
                korttidsEllerLangtidsfraværSiste4Kvartaler.getSiste4KvartalerSykefravær().isErMaskert()
        )
                .isEqualTo(
                        expectedErMaskert
                );
        assertThat(
                korttidsEllerLangtidsfraværSiste4Kvartaler.getLangtidEllerKorttid()
        )
                .isEqualTo(
                        expectedLangtidEllerKorttid
                );
    }

    private void assertBigDecimalIsEqual(BigDecimal actual, int expected) {
        assertThat(actual.setScale(6)).isEqualTo(BigDecimal.valueOf(expected).setScale(6));
    }

}

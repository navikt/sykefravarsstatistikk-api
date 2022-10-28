package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SummertSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartalMedVarighet;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.AssertUtils.assertBigDecimalIsEqual;
import static org.assertj.core.api.Assertions.assertThat;

class SummertKorttidsOgLangtidsfraværTest {

    private final ÅrstallOgKvartal sistePublisertÅrstallOgKvartal = new ÅrstallOgKvartal(2020, 1);


    @Test
    public void getSummertKorttidsOgLangtidsfravær__skal_returnere_et_tomt_KorttidsOgLangtidsfraværSiste4Kvartaler_dersom_ingen_data_er_tilgjengelig() {
        SummertKorttidsOgLangtidsfravær summertKorttidsOgLangtidsfravær =
                SummertKorttidsOgLangtidsfravær.getSummertKorttidsOgLangtidsfravær(new ÅrstallOgKvartal(2020, 1), 4, Arrays.asList());

        assertIsEmptyObject(summertKorttidsOgLangtidsfravær.getSummertKorttidsfravær());
        assertIsEmptyObject(summertKorttidsOgLangtidsfravær.getSummertLangtidsfravær());
    }


    @Test
    public void getSummertKorttidsOgLangtidsfravær____skal_returnere_langtid_og_korttid_historikk() {
        List<UmaskertSykefraværForEttKvartalMedVarighet> sykefraværMed1Kvartal = Arrays.asList(
                getSykefraværMedVarighet(sistePublisertÅrstallOgKvartal, 5, 0, 0, Varighetskategori._1_DAG_TIL_7_DAGER),
                getSykefraværMedVarighet(sistePublisertÅrstallOgKvartal, 2, 0, 0, Varighetskategori.MER_ENN_39_UKER),
                getSykefraværMedVarighet(sistePublisertÅrstallOgKvartal, 0, 100, 10, Varighetskategori.TOTAL)
        );

        SummertKorttidsOgLangtidsfravær summertKorttidsOgLangtidsfravær =
                SummertKorttidsOgLangtidsfravær.getSummertKorttidsOgLangtidsfravær(new ÅrstallOgKvartal(2020, 1), 4, sykefraværMed1Kvartal);

        assertIsEqual(
                summertKorttidsOgLangtidsfravær.getSummertKorttidsfravær(),
                100,
                5,
                5,
                false,
                sistePublisertÅrstallOgKvartal
        );
        assertIsEqual(
                summertKorttidsOgLangtidsfravær.getSummertLangtidsfravær(),
                100,
                2,
                2,
                false,
                sistePublisertÅrstallOgKvartal
        );
    }

    @Test
    public void hentKorttidsOgLangtidsfraværSiste4Kvartaler__skal_returnere_langtid_og_korttid_historikk_for_flere_kvartaler() {
        ÅrstallOgKvartal kvartal1 = new ÅrstallOgKvartal(2019, 4);
        ÅrstallOgKvartal kvartal2 = new ÅrstallOgKvartal(2020, 1);

        List<UmaskertSykefraværForEttKvartalMedVarighet> sykefraværMed2Kvartaler = Arrays.asList(
                getSykefraværMedVarighet(kvartal1,9, 0, 0, Varighetskategori._8_DAGER_TIL_16_DAGER),
                getSykefraværMedVarighet(kvartal1, 6, 0, 0, Varighetskategori._17_DAGER_TIL_8_UKER),
                getSykefraværMedVarighet(kvartal1, 0, 100, 10, Varighetskategori.TOTAL),

                getSykefraværMedVarighet(kvartal2, 5, 0, 0, Varighetskategori._1_DAG_TIL_7_DAGER),
                getSykefraværMedVarighet(kvartal2, 2, 0, 0, Varighetskategori.MER_ENN_39_UKER),
                getSykefraværMedVarighet(kvartal2, 0, 100, 10, Varighetskategori.TOTAL)
        );

        SummertKorttidsOgLangtidsfravær summertKorttidsOgLangtidsfravær =
                SummertKorttidsOgLangtidsfravær.getSummertKorttidsOgLangtidsfravær(new ÅrstallOgKvartal(2020, 1), 4, sykefraværMed2Kvartaler);

        assertIsEqual(
                summertKorttidsOgLangtidsfravær.getSummertKorttidsfravær(),
                200,
                14,
                7,
                false,
                kvartal1,
                kvartal2
        );
        assertIsEqual(
                summertKorttidsOgLangtidsfravær.getSummertLangtidsfravær(),
                200,
                8,
                4,
                false,
                kvartal1,
                kvartal2
        );
    }

    @Test
    public void hentKorttidsOgLangtidsfraværSiste4Kvartaler__skal_summere_opp_riktig_hvis_det_er_flere_innslag_med_samme_varighet_i_hvert_kvartal() {
        ÅrstallOgKvartal kvartal = new ÅrstallOgKvartal(2020, 1);
        List<UmaskertSykefraværForEttKvartalMedVarighet> sykefraværMed1Kvartal = Arrays.asList(
                getSykefraværMedVarighet(kvartal, 3000, 0, 0, Varighetskategori._1_DAG_TIL_7_DAGER),
                getSykefraværMedVarighet(kvartal, 2000, 0, 0, Varighetskategori._8_DAGER_TIL_16_DAGER),

                getSykefraværMedVarighet(kvartal, 1000, 0, 0, Varighetskategori._17_DAGER_TIL_8_UKER),
                getSykefraværMedVarighet(kvartal, 100, 0, 0, Varighetskategori._8_UKER_TIL_20_UKER),
                getSykefraværMedVarighet(kvartal, 10, 0, 0, Varighetskategori._20_UKER_TIL_39_UKER),
                getSykefraværMedVarighet(kvartal, 1, 0, 0, Varighetskategori.MER_ENN_39_UKER),

                getSykefraværMedVarighet(kvartal, 0, 100000, 2000, Varighetskategori.TOTAL)
        );


        SummertKorttidsOgLangtidsfravær summertKorttidsOgLangtidsfravær =
                SummertKorttidsOgLangtidsfravær.getSummertKorttidsOgLangtidsfravær(new ÅrstallOgKvartal(2020, 1), 4, sykefraværMed1Kvartal);

        assertIsEqual(
                summertKorttidsOgLangtidsfravær.getSummertKorttidsfravær(),
                100000,
                5000,
                5,
                false,
                kvartal
        );
        assertIsEqual(
                summertKorttidsOgLangtidsfravær.getSummertLangtidsfravær(),
                100000,
                1111,
                1.1f,
                false,
                kvartal
        );
    }

    @Test
    public void hentKorttidsOgLangtidsfraværSiste4Kvartaler__filtrerer_bort_kvartaler_som_er_eldre_enn_de_4_siste() {
        ÅrstallOgKvartal sistePublisertKvartal = new ÅrstallOgKvartal(2020, 1);
        ÅrstallOgKvartal utdatertKvartal = sistePublisertKvartal.minusKvartaler(4);
        ÅrstallOgKvartal ikkeUtdatertKvartal = sistePublisertKvartal.minusKvartaler(2);

        List<UmaskertSykefraværForEttKvartalMedVarighet> umaskertKvartalsvisSykefraværMedVarighet = Arrays.asList(
                getSykefraværMedVarighet(utdatertKvartal, 10, 0, 0, Varighetskategori._1_DAG_TIL_7_DAGER),
                getSykefraværMedVarighet(utdatertKvartal, 20, 0, 0, Varighetskategori.MER_ENN_39_UKER),
                getSykefraværMedVarighet(utdatertKvartal, 0, 500, 2000, Varighetskategori.TOTAL),

                getSykefraværMedVarighet(ikkeUtdatertKvartal, 10, 0, 0, Varighetskategori._1_DAG_TIL_7_DAGER),
                getSykefraværMedVarighet(ikkeUtdatertKvartal, 20, 0, 0, Varighetskategori.MER_ENN_39_UKER),
                getSykefraværMedVarighet(ikkeUtdatertKvartal, 0, 100, 200, Varighetskategori.TOTAL)
        );


        SummertKorttidsOgLangtidsfravær summertKorttidsOgLangtidsfravær =
                SummertKorttidsOgLangtidsfravær.getSummertKorttidsOgLangtidsfravær(new ÅrstallOgKvartal(2020, 1), 4, umaskertKvartalsvisSykefraværMedVarighet);

        assertIsEqual(
                summertKorttidsOgLangtidsfravær.getSummertKorttidsfravær(),
                100,
                10,
                10,
                false,
                ikkeUtdatertKvartal
        );
        assertIsEqual(
                summertKorttidsOgLangtidsfravær.getSummertLangtidsfravær(),
                100,
                20,
                20,
                false,
                ikkeUtdatertKvartal
        );
    }

    @Test
    public void hentKorttidsOgLangtidsfraværSiste4Kvartaler__beregner_sykefravær_selv_om_det_bare_er_data_om_TOTAL() {
        ÅrstallOgKvartal sistePublisertKvartal = new ÅrstallOgKvartal(2020, 1);
        ÅrstallOgKvartal kvartalMedBareTotal = sistePublisertKvartal.minusKvartaler(1);
        ÅrstallOgKvartal kvartalMedKorttidOgLangtidsfravær = sistePublisertKvartal.minusKvartaler(2);

        List<UmaskertSykefraværForEttKvartalMedVarighet> umaskertKvartalsvisSykefraværMedVarighet = Arrays.asList(
                getSykefraværMedVarighet(kvartalMedBareTotal, 0, 100, 100, Varighetskategori.TOTAL),

                getSykefraværMedVarighet(kvartalMedKorttidOgLangtidsfravær, 10, 0, 0, Varighetskategori._1_DAG_TIL_7_DAGER),
                getSykefraværMedVarighet(kvartalMedKorttidOgLangtidsfravær, 30, 0, 0, Varighetskategori.MER_ENN_39_UKER),
                getSykefraværMedVarighet(kvartalMedKorttidOgLangtidsfravær, 0, 100, 200, Varighetskategori.TOTAL)
        );


        SummertKorttidsOgLangtidsfravær summertKorttidsOgLangtidsfravær =
                SummertKorttidsOgLangtidsfravær.getSummertKorttidsOgLangtidsfravær(new ÅrstallOgKvartal(2020, 1), 4, umaskertKvartalsvisSykefraværMedVarighet);

        assertIsEqual(
                summertKorttidsOgLangtidsfravær.getSummertKorttidsfravær(),
                200,
                10,
                5,
                false,
                kvartalMedBareTotal, kvartalMedKorttidOgLangtidsfravær
        );

        assertIsEqual(
                summertKorttidsOgLangtidsfravær.getSummertLangtidsfravær(),
                200,
                30,
                15,
                false,
                kvartalMedBareTotal, kvartalMedKorttidOgLangtidsfravær
        );
    }

    @Test
    public void hentKorttidsOgLangtidsfraværSiste4Kvartaler__håndterer_kvartaler_som_ikke_inneholder_TOTAL() {
        ÅrstallOgKvartal sistePublisertKvartal = new ÅrstallOgKvartal(2020, 1);
        ÅrstallOgKvartal kvartalSomIkkeInneholderTotal = sistePublisertKvartal.minusKvartaler(1);
        ÅrstallOgKvartal kvartalMedKorttidOgLangtidsfravær = sistePublisertKvartal.minusKvartaler(2);

        List<UmaskertSykefraværForEttKvartalMedVarighet> umaskertKvartalsvisSykefraværMedVarighet = Arrays.asList(
                getSykefraværMedVarighet(kvartalSomIkkeInneholderTotal, 10, 0, 0, Varighetskategori._1_DAG_TIL_7_DAGER),

                getSykefraværMedVarighet(kvartalMedKorttidOgLangtidsfravær, 10, 0, 0, Varighetskategori._1_DAG_TIL_7_DAGER),
                getSykefraværMedVarighet(kvartalMedKorttidOgLangtidsfravær, 30, 0, 0, Varighetskategori.MER_ENN_39_UKER),
                getSykefraværMedVarighet(kvartalMedKorttidOgLangtidsfravær, 0, 100, 200, Varighetskategori.TOTAL)
        );


        SummertKorttidsOgLangtidsfravær summertKorttidsOgLangtidsfravær =
                SummertKorttidsOgLangtidsfravær.getSummertKorttidsOgLangtidsfravær(new ÅrstallOgKvartal(2020, 1), 4, umaskertKvartalsvisSykefraværMedVarighet);

        assertIsEqual(
                summertKorttidsOgLangtidsfravær.getSummertKorttidsfravær(),
                100,
                20,
                20,
                false,
                kvartalMedKorttidOgLangtidsfravær, kvartalSomIkkeInneholderTotal
        );

        assertIsEqual(
                summertKorttidsOgLangtidsfravær.getSummertLangtidsfravær(),
                100,
                30,
                30,
                false,
                kvartalMedKorttidOgLangtidsfravær, kvartalSomIkkeInneholderTotal
        );
    }

    @Test
    public void hentKorttidsOgLangtidsfraværSiste4Kvartaler__maskerer_sykefravær_dersom_antall_personer_er_lavere_enn_5() {
        ÅrstallOgKvartal sistePublisertKvartal = new ÅrstallOgKvartal(2020, 1);
        ÅrstallOgKvartal kvartal1 = sistePublisertKvartal.minusKvartaler(1);
        ÅrstallOgKvartal kvartal2 = sistePublisertKvartal.minusKvartaler(2);

        List<UmaskertSykefraværForEttKvartalMedVarighet> umaskertKvartalsvisSykefraværMedVarighet = Arrays.asList(
                getSykefraværMedVarighet(kvartal1, 5, 0, 0, Varighetskategori._1_DAG_TIL_7_DAGER),
                getSykefraværMedVarighet(kvartal1, 0, 100, 1, Varighetskategori.TOTAL),

                getSykefraværMedVarighet(kvartal2, 10, 0, 0, Varighetskategori._1_DAG_TIL_7_DAGER),
                getSykefraværMedVarighet(kvartal2, 30, 0, 0, Varighetskategori.MER_ENN_39_UKER),
                getSykefraværMedVarighet(kvartal2, 0, 100, 4, Varighetskategori.TOTAL)
        );


        SummertKorttidsOgLangtidsfravær summertKorttidsOgLangtidsfravær =
                SummertKorttidsOgLangtidsfravær.getSummertKorttidsOgLangtidsfravær(new ÅrstallOgKvartal(2020, 1), 4, umaskertKvartalsvisSykefraværMedVarighet);

        assertIsMaskert(
                summertKorttidsOgLangtidsfravær.getSummertKorttidsfravær(),
                kvartal1, kvartal2
        );

        assertIsMaskert(
                summertKorttidsOgLangtidsfravær.getSummertLangtidsfravær(),
                kvartal1, kvartal2
        );
    }

    @Test
    public void hentKorttidsOgLangtidsfraværSiste4Kvartaler__skal_IKKE_maskere_dersom_maks_antall_personer_er_større_enn_4() {
        ÅrstallOgKvartal kvartal1 = new ÅrstallOgKvartal(2019, 4);
        ÅrstallOgKvartal kvartal2 = new ÅrstallOgKvartal(2020, 1);

        List<UmaskertSykefraværForEttKvartalMedVarighet> sykefraværMed2Kvartaler = Arrays.asList(
                getSykefraværMedVarighet(kvartal1,9, 0, 0, Varighetskategori._8_DAGER_TIL_16_DAGER),
                getSykefraværMedVarighet(kvartal1, 6, 0, 0, Varighetskategori._17_DAGER_TIL_8_UKER),
                getSykefraværMedVarighet(kvartal1, 0, 100, 4, Varighetskategori.TOTAL),

                getSykefraværMedVarighet(kvartal2, 5, 0, 0, Varighetskategori._1_DAG_TIL_7_DAGER),
                getSykefraværMedVarighet(kvartal2, 2, 0, 0, Varighetskategori.MER_ENN_39_UKER),
                getSykefraværMedVarighet(kvartal2, 0, 100, 5, Varighetskategori.TOTAL)
        );

        SummertKorttidsOgLangtidsfravær summertKorttidsOgLangtidsfravær =
                SummertKorttidsOgLangtidsfravær.getSummertKorttidsOgLangtidsfravær(new ÅrstallOgKvartal(2020, 1), 4, sykefraværMed2Kvartaler);

        assertIsEqual(
                summertKorttidsOgLangtidsfravær.getSummertKorttidsfravær(),
                200,
                14,
                7,
                false,
                kvartal1,
                kvartal2
        );
        assertIsEqual(
                summertKorttidsOgLangtidsfravær.getSummertLangtidsfravær(),
                200,
                8,
                4,
                false,
                kvartal1,
                kvartal2
        );
    }

    private void assertIsEmptyObject(
            SummertSykefravær korttidsEllerLangtidsfraværSiste4Kvartaler,
            ÅrstallOgKvartal... expectedKvartaler
    ) {
        assertThat(korttidsEllerLangtidsfraværSiste4Kvartaler.getMuligeDagsverk()).isNull();
        assertThat(korttidsEllerLangtidsfraværSiste4Kvartaler.getTapteDagsverk()).isNull();
        assertThat(korttidsEllerLangtidsfraværSiste4Kvartaler.getProsent()).isNull();
        assertThat(korttidsEllerLangtidsfraværSiste4Kvartaler.isErMaskert()).isFalse();
        assertThat(expectedKvartaler.length).isEqualTo(0);
    }

    private UmaskertSykefraværForEttKvartalMedVarighet getSykefraværMedVarighet(
            ÅrstallOgKvartal årstallOgKvartal,
            int tapteDagsverk,
            int muligeDagsverk,
            int antallPersoner,
            Varighetskategori varighet
    ) {
        return new UmaskertSykefraværForEttKvartalMedVarighet(
                årstallOgKvartal,
                BigDecimal.valueOf(tapteDagsverk),
                BigDecimal.valueOf(muligeDagsverk),
                antallPersoner,
                varighet
        );
    }

    private void assertIsEqual(
            SummertSykefravær korttidsEllerLangtidsfraværSiste4Kvartaler,
            int expectedMuligeDagsverk,
            int expectedTapteDagsverk,
            float expectedProsent,
            boolean expectedErMaskert,
            ÅrstallOgKvartal... expectedKvartaler
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

        kvartalerIsEqual(korttidsEllerLangtidsfraværSiste4Kvartaler, expectedKvartaler);
    }

    private void kvartalerIsEqual(SummertSykefravær korttidsEllerLangtidsfraværSiste4Kvartaler, ÅrstallOgKvartal[] expectedKvartaler) {
        List<ÅrstallOgKvartal> expectedKvartalerAsList = Arrays.asList(expectedKvartaler);
        expectedKvartalerAsList.sort(ÅrstallOgKvartal::compareTo);

        assertThat(korttidsEllerLangtidsfraværSiste4Kvartaler.getKvartaler())
                .isEqualTo(expectedKvartalerAsList);
    }

    private void assertIsMaskert(
            SummertSykefravær korttidsEllerLangtidsfraværSiste4Kvartaler,
            ÅrstallOgKvartal... expectedKvartaler
    ) {
        assertThat(korttidsEllerLangtidsfraværSiste4Kvartaler.getMuligeDagsverk()).isNull();
        assertThat(korttidsEllerLangtidsfraværSiste4Kvartaler.getTapteDagsverk()).isNull();
        assertThat(korttidsEllerLangtidsfraværSiste4Kvartaler.getProsent()).isNull();
        assertThat(korttidsEllerLangtidsfraværSiste4Kvartaler.isErMaskert()).isTrue();

        kvartalerIsEqual(korttidsEllerLangtidsfraværSiste4Kvartaler, expectedKvartaler);
    }

}

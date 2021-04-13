package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetEksportPerKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkSektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetUtenVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.AssertUtils.assertBigDecimalIsEqual;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EksporteringServiceTest {

    @Test
    public void getAntallSomKanEksporteres__returnerer_antall_virksomheter_som_ikke_har_blitt_eksportert_enda__uavhengig_av_kvartal() {
        long antallSomKanEksporteres = EksporteringService.getAntallSomKanEksporteres(Arrays.asList(
                new VirksomhetEksportPerKvartal(ORGNR_VIRKSOMHET_1, __2020_4, true),
                new VirksomhetEksportPerKvartal(ORGNR_VIRKSOMHET_1, __2021_1, false),
                new VirksomhetEksportPerKvartal(ORGNR_VIRKSOMHET_1, __2021_2, false),
                new VirksomhetEksportPerKvartal(ORGNR_VIRKSOMHET_2, __2021_2, false)
        ));

        assertEquals(3, antallSomKanEksporteres);
    }

    @Test
    public void getVirksomhetMetada__returnerer_VirksomhetMetada_som_matcher_VirksomhetEksportPerKvartal() {
        VirksomhetMetadata resultat = EksporteringService.getVirksomhetMetada(
                ORGNR_VIRKSOMHET_1,
                __2020_4,
                Arrays.asList(virksomhet1Metadata_2020_4, virksomhet1Metadata_2021_1)
        );

        assertEquals(virksomhet1Metadata_2020_4, resultat);
    }

    @Test
    public void getVirksomhetMetada__returnerer_NULL__dersom_ingen_entry_matcher_VirksomhetEksportPerKvartal() {
        VirksomhetMetadata result = EksporteringService.getVirksomhetMetada(
                ORGNR_VIRKSOMHET_1,
                __2021_2,
                Arrays.asList(virksomhet1Metadata_2020_4, virksomhet1Metadata_2021_1)
        );

        assertNull(result);
    }

    @Test
    public void getVirksomhetSykefravær__returnerer_TOM_VirksomhetSykefravær__dersom_liste_sykefraværsstatistikk_for_virksomheten_er_tom() {
        VirksomhetSykefravær resultat = EksporteringService.getVirksomhetSykefravær(
                virksomhet1Metadata_2020_4,
                Collections.emptyList()
        );

        assertEqualsVirksomhetSykefravær(resultat, tomVirksomhetSykefravær(virksomhet1Metadata_2020_4));
    }

    @Test
    public void getVirksomhetSykefravær__returnerer_TOM_VirksomhetSykefravær__dersom_sykefraværsstatistikk_IKKE_er_funnet_for_virksomheten() {
        VirksomhetSykefravær resultat = EksporteringService.getVirksomhetSykefravær(
                virksomhet1Metadata_2021_2,
                Arrays.asList(
                        byggSykefraværsstatistikkVirksomhet(virksomhet1Metadata_2020_4),
                        byggSykefraværsstatistikkVirksomhet(virksomhet1Metadata_2021_1)
                )
        );

        assertEqualsVirksomhetSykefravær(resultat, tomVirksomhetSykefravær(virksomhet1Metadata_2021_2));
    }

    @Test
    public void getVirksomhetSykefravær__returnerer_VirksomhetSykefravær__med_sykefraværsstatistikk_for_virksomheten() {
        VirksomhetSykefravær resultat = EksporteringService.getVirksomhetSykefravær(
                virksomhet1Metadata_2020_4,
                Arrays.asList(
                        byggSykefraværsstatistikkVirksomhet(virksomhet1Metadata_2020_4, 10, 156, 22233),
                        byggSykefraværsstatistikkVirksomhet(virksomhet2Metadata_2020_4)
                )
        );

        assertEqualsVirksomhetSykefravær(resultat, byggVirksomhetSykefravær(virksomhet1Metadata_2020_4, 10, 156, 22233));
    }

    @Test
    public void getVirksomhetSykefravær__returnerer_VirksomhetSykefravær__med_sykefraværsstatistikk_for_virksomheten__på_riktig_kvartal() {
        VirksomhetSykefravær resultat = EksporteringService.getVirksomhetSykefravær(
                virksomhet1Metadata_2020_4,
                Arrays.asList(
                        byggSykefraværsstatistikkVirksomhet(virksomhet1Metadata_2020_4),
                        byggSykefraværsstatistikkVirksomhet(virksomhet1Metadata_2021_1)
                )
        );

        assertEqualsVirksomhetSykefravær(resultat, byggVirksomhetSykefravær(virksomhet1Metadata_2020_4));
    }

    @Test
    public void getSykefraværMedKategoriForSektor__returnerer_SykefraværMedKategori__med_sykefraværsstatistikk_for_sektor() {
        SykefraværMedKategori resultat = EksporteringService.getSykefraværMedKategoriForSektor(
                virksomhet1Metadata_2020_4,
                Arrays.asList(
                        byggSykefraværStatistikkSektor(virksomhet1Metadata_2020_4, 10, 156, 22233),
                        byggSykefraværStatistikkSektor(virksomhet2Metadata_2020_4)
                )
        );

        assertEqualsSykefraværMedKategori(
                resultat,
                byggSykefraværStatistikkSektor(virksomhet1Metadata_2020_4, 10, 156, 22233),
                Statistikkategori.SEKTOR,
                virksomhet1Metadata_2020_4.getSektor()
        );
    }


    // Assertions
    private static void assertEqualsVirksomhetSykefravær(VirksomhetSykefravær actual, VirksomhetSykefravær expected) {
        assertThat(actual.getÅrstall()).isEqualTo(expected.getÅrstall());
        assertThat(actual.getKvartal()).isEqualTo(expected.getKvartal());
        assertThat(actual.getOrgnr()).isEqualTo(expected.getOrgnr());
        assertBigDecimalIsEqual(actual.getMuligeDagsverk(), expected.getMuligeDagsverk());
        assertBigDecimalIsEqual(actual.getTapteDagsverk(), expected.getTapteDagsverk());
    }

    private void assertEqualsSykefraværMedKategori(
            SykefraværMedKategori actual,
            SykefraværsstatistikkSektor expected,
            Statistikkategori expectedKategori,
            String expectedKode
    ) {
        assertThat(actual.getKategori()).isEqualTo(expectedKategori);
        assertThat(actual.getKode()).isEqualTo(expectedKode);
        assertThat(actual.getÅrstall()).isEqualTo(expected.getÅrstall());
        assertThat(actual.getÅrstall()).isEqualTo(expected.getÅrstall());
        assertThat(actual.getKvartal()).isEqualTo(expected.getKvartal());
        assertBigDecimalIsEqual(actual.getMuligeDagsverk(), expected.getMuligeDagsverk());
        assertBigDecimalIsEqual(actual.getTapteDagsverk(), expected.getTapteDagsverk());
    }



    // Data for testing & Utilities
    private static ÅrstallOgKvartal __2020_4 = new ÅrstallOgKvartal(2020, 4);
    private static ÅrstallOgKvartal __2021_1 = new ÅrstallOgKvartal(2021, 1);
    private static ÅrstallOgKvartal __2021_2 = new ÅrstallOgKvartal(2021, 2);
    private static Orgnr ORGNR_VIRKSOMHET_1 = new Orgnr("987654321");
    private static Orgnr ORGNR_VIRKSOMHET_2 = new Orgnr("912345678");

    private static VirksomhetMetadata virksomhet1Metadata_2020_4 = new VirksomhetMetadata(
            ORGNR_VIRKSOMHET_1,
            "Virksomhet 1",
            RECTYPE_FOR_VIRKSOMHET,
            "1",
            "11",
            __2020_4
    );

    private static VirksomhetMetadata virksomhet2Metadata_2020_4 = new VirksomhetMetadata(
            ORGNR_VIRKSOMHET_2,
            "Virksomhet 2",
            RECTYPE_FOR_VIRKSOMHET,
            "2",
            "22",
            __2020_4
    );

    private static VirksomhetMetadata virksomhet1Metadata_2021_1 = new VirksomhetMetadata(
            ORGNR_VIRKSOMHET_1,
            "Virksomhet 1",
            RECTYPE_FOR_VIRKSOMHET,
            "1",
            "11",
            __2021_1
    );

    private static VirksomhetMetadata virksomhet1Metadata_2021_2 = new VirksomhetMetadata(
            ORGNR_VIRKSOMHET_1,
            "Virksomhet 1",
            RECTYPE_FOR_VIRKSOMHET,
            "1",
            "11",
            __2021_2
    );

    private SykefraværsstatistikkVirksomhetUtenVarighet byggSykefraværsstatistikkVirksomhet(
            VirksomhetMetadata virksomhetMetadata
    ) {
        return byggSykefraværsstatistikkVirksomhet(
                virksomhetMetadata,
                156,
                3678,
                188000
        );
    }

    private static SykefraværsstatistikkVirksomhetUtenVarighet byggSykefraværsstatistikkVirksomhet(
            VirksomhetMetadata virksomhetMetadata,
            int antallPersoner,
            int tapteDagsverk,
            int muligeDagsverk
    ) {
        return new SykefraværsstatistikkVirksomhetUtenVarighet(
                virksomhetMetadata.getÅrstall(),
                virksomhetMetadata.getKvartal(),
                virksomhetMetadata.getOrgnr(),
                antallPersoner,
                new BigDecimal(tapteDagsverk),
                new BigDecimal(muligeDagsverk)
        );
    }

    private static VirksomhetSykefravær tomVirksomhetSykefravær(VirksomhetMetadata virksomhetMetadata) {
        return new VirksomhetSykefravær(
                virksomhetMetadata.getOrgnr(),
                virksomhetMetadata.getNavn(),
                new ÅrstallOgKvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
                null,
                null,
                0
        );
    }


    private static SykefraværsstatistikkSektor byggSykefraværStatistikkSektor(
            VirksomhetMetadata virksomhetMetadata,
            int antallPersoner,
            int tapteDagsverk,
            int muligeDagsverk
    ) {
        return new SykefraværsstatistikkSektor(
                virksomhetMetadata.getÅrstall(),
                virksomhetMetadata.getKvartal(),
                virksomhetMetadata.getSektor(),
                antallPersoner,
                new BigDecimal(tapteDagsverk),
                new BigDecimal(muligeDagsverk)
        );
    }

    private static SykefraværsstatistikkSektor byggSykefraværStatistikkSektor(VirksomhetMetadata virksomhetMetadata) {
        return new SykefraværsstatistikkSektor(
                virksomhetMetadata.getÅrstall(),
                virksomhetMetadata.getKvartal(),
                virksomhetMetadata.getSektor(),
                156,
                new BigDecimal(3678),
                new BigDecimal(188000)
        );
    }

    private static VirksomhetSykefravær byggVirksomhetSykefravær(
            VirksomhetMetadata virksomhetMetadata,
            int antallPersoner,
            int tapteDagsverk,
            int muligeDagsverk
    ) {
        return new VirksomhetSykefravær(
                virksomhetMetadata.getOrgnr(),
                virksomhetMetadata.getNavn(),
                new ÅrstallOgKvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
                new BigDecimal(tapteDagsverk),
                new BigDecimal(muligeDagsverk),
                antallPersoner
                );
    }

    private static VirksomhetSykefravær byggVirksomhetSykefravær(VirksomhetMetadata virksomhetMetadata) {
        return new VirksomhetSykefravær(
                virksomhetMetadata.getOrgnr(),
                virksomhetMetadata.getNavn(),
                new ÅrstallOgKvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
                new BigDecimal(3678),
                new BigDecimal(188000),
                156
        );
    }
}
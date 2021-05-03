package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.NæringOgNæringskode5siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetEksportPerKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EksporteringServiceTest {

    @Test
    public void listeAvVirksomheterSomString__formater_en_liste_til_logging() {
        String resultat = EksporteringService.listeAvVirksomheterSomString(
                Arrays.asList(
                        new VirksomhetEksportPerKvartal(ORGNR_VIRKSOMHET_1, __2020_4, true),
                        new VirksomhetEksportPerKvartal(ORGNR_VIRKSOMHET_1, __2021_1, false),
                        new VirksomhetEksportPerKvartal(ORGNR_VIRKSOMHET_1, __2021_2, true),
                        new VirksomhetEksportPerKvartal(ORGNR_VIRKSOMHET_2, __2021_2, false)
                )
        );

        assertNotNull(resultat);
    }

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
    public void getVirksomhetMetada__returnerer_VirksomhetMetada_som_matcher_Virksomhet() {
        VirksomhetMetadata resultat = EksporteringService.getVirksomhetMetada(
                ORGNR_VIRKSOMHET_1,
                Arrays.asList(virksomhet1Metadata_2020_4, virksomhet2Metadata_2020_4)
        );

        assertEquals(virksomhet1Metadata_2020_4, resultat);
    }

    @Test
    public void getVirksomhetMetada__returnerer_NULL__dersom_ingen_entry_matcher_Virksomhet() {
        VirksomhetMetadata result = EksporteringService.getVirksomhetMetada(
                ORGNR_VIRKSOMHET_2,
                Arrays.asList(virksomhet1Metadata_2020_4)
        );

        assertNull(result);
    }

    @Test
    public void getVirksomhetSykefravær__returnerer_TOM_VirksomhetSykefravær__dersom_liste_sykefraværsstatistikk_for_virksomheten_er_tom() {
        VirksomhetSykefravær resultat = EksporteringService.getVirksomhetSykefravær(
                virksomhet1Metadata_2020_4,
                Collections.emptyList()
        );

        assertEqualsVirksomhetSykefravær(tomVirksomhetSykefravær(virksomhet1Metadata_2020_4), resultat);
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

        assertEqualsVirksomhetSykefravær(tomVirksomhetSykefravær(virksomhet1Metadata_2021_2), resultat);
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

        assertEqualsVirksomhetSykefravær(byggVirksomhetSykefravær(virksomhet1Metadata_2020_4, 10, 156, 22233), resultat);
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

        assertEqualsVirksomhetSykefravær(byggVirksomhetSykefravær(virksomhet1Metadata_2020_4), resultat);
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
                byggSykefraværStatistikkSektor(virksomhet1Metadata_2020_4, 10, 156, 22233), resultat,
                Statistikkategori.SEKTOR,
                virksomhet1Metadata_2020_4.getSektor()
        );
    }

    @Test
    public void getSykefraværMedKategoriForNæring__returnerer_SykefraværMedKategori__med_sykefraværsstatistikk_for_næring() {
        SykefraværMedKategori resultat = EksporteringService.getSykefraværMedKategoriForNæring(
                virksomhet1Metadata_2020_4,
                Arrays.asList(
                        byggSykefraværStatistikkNæring(virksomhet1Metadata_2020_4, 10, 156, 22233),
                        byggSykefraværStatistikkNæring(virksomhet2Metadata_2020_4)
                )
        );

        assertEqualsSykefraværMedKategori(
                byggSykefraværStatistikkNæring(virksomhet1Metadata_2020_4, 10, 156, 22233), resultat,
                Statistikkategori.NÆRING2SIFFER,
                virksomhet1Metadata_2020_4.getNæring()
        );
    }
@Test
    public void getSykefraværMedKategoriForNæring5Siffer__returnerer_SykefraværMedKategori__med_sykefraværsstatistikk_for_næring_5_siffer() {
        virksomhet1Metadata_2020_4.leggTilNæringOgNæringskode5siffer(Arrays.asList(
                new NæringOgNæringskode5siffer("85","85000"),
                new NæringOgNæringskode5siffer("11","11000")
        ));
        List<SykefraværMedKategori> resultat = EksporteringService.getSykefraværMedKategoriForNæring5Siffer(
                virksomhet1Metadata_2020_4,
                Arrays.asList(
                        byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "11000"),
                        byggSykefraværStatistikkNæring5Siffer(virksomhet2Metadata_2020_4,"85000")
                )
        );

        assertThat(resultat.size()).isEqualTo(2);
        SykefraværMedKategori sykefraværMedKategori85000 = resultat.stream().
                filter(r -> "85000".equals(r.getKode()))
                . findFirst().get();

        assertEqualsSykefraværMedKategori(
                byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4,"85000"), sykefraværMedKategori85000,
                Statistikkategori.NÆRING5SIFFER,
                virksomhet1Metadata_2020_4.getNæringOgNæringskode5siffer().get(0).getNæringskode5Siffer()
        );
        SykefraværMedKategori sykefraværMedKategori11000 = resultat.stream().
                filter(r -> "11000".equals(r.getKode()))
                . findFirst().get();

        assertEqualsSykefraværMedKategori(
                byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4,"11000"), sykefraværMedKategori11000,
                Statistikkategori.NÆRING5SIFFER,
                virksomhet1Metadata_2020_4.getNæringOgNæringskode5siffer().get(1).getNæringskode5Siffer()
        );
    }

    @Test
    public void getSykefraværsstatistikkNæring5Siffers__skal_returnere_riktig_liste() {
        VirksomhetMetadata virksomhetMetadata_2020_4_med_næring5siffer = virksomhet1Metadata_2020_4;
        virksomhetMetadata_2020_4_med_næring5siffer.leggTilNæringOgNæringskode5siffer(
                Arrays.asList(
                        new NæringOgNæringskode5siffer("11", "11000"),
                        new NæringOgNæringskode5siffer("85", "85000")
                )
        );
        List<SykefraværsstatistikkNæring5Siffer> sykefraværsstatistikkNæring5SifferList = Arrays.asList(
                byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "11000"),
                byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "45210"),
                byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "85000")
        );
        List<SykefraværsstatistikkNæring5Siffer> resultat = EksporteringService.getSykefraværsstatistikkNæring5Siffers(
                virksomhetMetadata_2020_4_med_næring5siffer,
                sykefraværsstatistikkNæring5SifferList
        );

        assertThat(resultat.size()).isEqualTo(2);
        assertThat(resultat.contains(
                byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "11000")
        ));
        assertThat(resultat.contains(
                byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "85000")
        ));
        assertThat(!resultat.contains(
                byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "45210")
        ));
    }

}

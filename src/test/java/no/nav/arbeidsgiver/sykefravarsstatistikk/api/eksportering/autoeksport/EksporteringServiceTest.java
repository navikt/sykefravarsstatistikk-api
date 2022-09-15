package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.NæringOgNæringskode5siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetEksportPerKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.ArbeidsmiljøportalenBransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetUtenVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.StatistikkDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.*;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal.SISTE_PUBLISERTE_KVARTAL;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class EksporteringServiceTest {

    @Test
    public void getVirksomhetMetadataHashMap__returnerer_en_map_med_orgnr_som_key() {
        Map<String, VirksomhetMetadata> virksomhetMetadataHashMap =
                EksporteringService.getVirksomhetMetadataHashMap(
                        Arrays.asList(
                                virksomhet1Metadata_2020_4,
                                virksomhet2Metadata_2020_4
                        )
                );

        assertEquals(2, virksomhetMetadataHashMap.size());
        assertEquals(virksomhet1Metadata_2020_4, virksomhetMetadataHashMap.get(virksomhet1Metadata_2020_4.getOrgnr()));
        assertEquals(virksomhet2Metadata_2020_4, virksomhetMetadataHashMap.get(virksomhet2Metadata_2020_4.getOrgnr()));
    }

    @Test
    public void getVirksomheterMetadataFraSubset__returnerer_intersection() {
        Map<String, VirksomhetMetadata> virksomhetMetadataHashMap = new HashMap<>();
        virksomhetMetadataHashMap.put(virksomhet1Metadata_2020_4.getOrgnr(), virksomhet1Metadata_2020_4);
        virksomhetMetadataHashMap.put(virksomhet2Metadata_2020_4.getOrgnr(), virksomhet2Metadata_2020_4);
        virksomhetMetadataHashMap.put(virksomhet3Metadata_2020_4.getOrgnr(), virksomhet3Metadata_2020_4);

        List<VirksomhetMetadata> virksomhetMetadataList = EksporteringService.getVirksomheterMetadataFraSubset(
                virksomhetMetadataHashMap,
                Arrays.asList(
                        new VirksomhetEksportPerKvartal(ORGNR_VIRKSOMHET_1, __2020_4, false),
                        new VirksomhetEksportPerKvartal(ORGNR_VIRKSOMHET_2, __2020_4, false)
                )
        );

        assertEquals(2, virksomhetMetadataList.size());
        assertTrue(virksomhetMetadataList.contains(virksomhet1Metadata_2020_4));
        assertTrue(virksomhetMetadataList.contains(virksomhet2Metadata_2020_4));
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
    public void getVirksomhetSykefravær__returnerer_VirksomhetSykefravær_uten_statistikk__dersom_ingen_entry_matcher_Virksomhet() {
        VirksomhetSykefravær actualVirksomhetSykefravær = EksporteringService.getVirksomhetSykefravær(
                virksomhet1Metadata_2020_4,
                buildMapAvSykefraværsstatistikkPerVirksomhet(10)
        );

        VirksomhetSykefravær expectedVirksomhetSykefravær = new VirksomhetSykefravær(
                virksomhet1Metadata_2020_4.getOrgnr(),
                virksomhet1Metadata_2020_4.getNavn(),
                2020,
                4,
                null,
                null,
                0
        );
        assertEqualsVirksomhetSykefravær(expectedVirksomhetSykefravær, actualVirksomhetSykefravær);
    }

        @Test
    public void getVirksomhetSykefravær__returnerer_VirksomhetSykefravær_som_matcher_Virksomhet() {
        VirksomhetMetadata virksomhetToBeFound = new VirksomhetMetadata(
                new Orgnr("399000"),
                "Virksomhet 1",
                RECTYPE_FOR_VIRKSOMHET,
                "1",
                "11",
                __2020_4
        );
        Map<String, SykefraværsstatistikkVirksomhetUtenVarighet> bigMap =
                buildMapAvSykefraværsstatistikkPerVirksomhet(500000);

        long startWithMap = System.nanoTime();
        VirksomhetSykefravær actualVirksomhetSykefravær = EksporteringService.getVirksomhetSykefravær(
                virksomhetToBeFound,
                bigMap
        );
        long stopWithMap = System.nanoTime();

        VirksomhetSykefravær expectedVirksomhetSykefravær = new VirksomhetSykefravær(
                virksomhetToBeFound.getOrgnr(),
                virksomhetToBeFound.getNavn(),
                2020,
                4,
                new BigDecimal(100),
                new BigDecimal(1000),
                10
        );
        assertEqualsVirksomhetSykefravær(expectedVirksomhetSykefravær, actualVirksomhetSykefravær);
        System.out.println("Elapsed time in nanoseconds (With Map) = " + (stopWithMap - startWithMap));
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
                new NæringOgNæringskode5siffer("85", "85000"),
                new NæringOgNæringskode5siffer("11", "11000")
        ));
        List<SykefraværMedKategori> resultat = EksporteringService.getSykefraværMedKategoriForNæring5Siffer(
                virksomhet1Metadata_2020_4,
                Arrays.asList(
                        byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "11000"),
                        byggSykefraværStatistikkNæring5Siffer(virksomhet2Metadata_2020_4, "85000")
                )
        );

        assertThat(resultat.size()).isEqualTo(2);
        SykefraværMedKategori sykefraværMedKategori85000 = resultat.stream().
                filter(r -> "85000".equals(r.getKode()))
                .findFirst().get();

        assertEqualsSykefraværMedKategori(
                byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "85000"), sykefraværMedKategori85000,
                Statistikkategori.NÆRING5SIFFER,
                virksomhet1Metadata_2020_4.getNæringOgNæringskode5siffer().get(0).getNæringskode5Siffer()
        );
        SykefraværMedKategori sykefraværMedKategori11000 = resultat.stream().
                filter(r -> "11000".equals(r.getKode()))
                .findFirst().get();

        assertEqualsSykefraværMedKategori(
                byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "11000"), sykefraværMedKategori11000,
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

    @Test
    public void getSykefraværMedKategoriForBransjeEllerNæringSiste4Kvartaler__skalIkkeFeileVedManglendeData(){
        assertThat( EksporteringService.getSykefraværMedKategoriForBransjeEllerNæringSiste4Kvartaler(
              virksomhet1Metadata_2020_4,Collections.emptyList(),Collections.emptyList(),
              SISTE_PUBLISERTE_KVARTAL,new BransjeEllerNæring(new Næring("11","Industri"))
        )).isEmpty();
    }

    @Test
    public void getSykefraværMedKategoriForBransjeEllerNæringSiste4Kvartaler__skalHenteTomtListe(){
        assertThat(EksporteringService.getSykefraværMedKategoriForBransjeEllerNæringSiste4Kvartaler(
              virksomhet1Metadata_2020_4, List.of(byggSykefraværStatistikkNæring(
                    virksomhet1Metadata_2020_4
              )), List.of(
                    byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "11000")
              ),
              SISTE_PUBLISERTE_KVARTAL, new BransjeEllerNæring(new Næring("11", "Industri"))
        ).size()).isEqualTo(0);
    }

    @Test
    public void getSykefraværMedKategoriForBransjeEllerNæringSiste4Kvartaler__skalHenteNæringForSistePubliserteKvartal(){
        assertThat(EksporteringService.getSykefraværMedKategoriForBransjeEllerNæringSiste4Kvartaler(
              virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL, List.of(byggSykefraværStatistikkNæring(
                    virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL
              )), List.of(
                    byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL, "11000")
              ),
              SISTE_PUBLISERTE_KVARTAL, new BransjeEllerNæring(new Næring("11", "Industri"))
        ).get(0)).isEqualTo(StatistikkDto.builder()
              .statistikkategori(Statistikkategori.NÆRING)
              .label("Industri")
              .verdi("2.0")
              .antallPersonerIBeregningen(156)
              .kvartalerIBeregningen(List.of(SISTE_PUBLISERTE_KVARTAL)
              ).build());
    }

    @Test
    public void getSykefraværMedKategoriForBransjeEllerNæringSiste4Kvartaler__skalHenteNæringFor4SisteKvartaler(){
        assertThat(EksporteringService.getSykefraværMedKategoriForBransjeEllerNæringSiste4Kvartaler(
              virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL,
              List.of(
                    byggSykefraværStatistikkNæring(virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL),
                    byggSykefraværStatistikkNæring(virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL,SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1)),
                    byggSykefraværStatistikkNæring(virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL,SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2)),
                    byggSykefraværStatistikkNæring(virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL,SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3)),
                    byggSykefraværStatistikkNæring(virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL,SISTE_PUBLISERTE_KVARTAL.minusEttÅr())
              ),
              List.of(
                    byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL, "11000")
              ),
              SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3), new BransjeEllerNæring(new Næring("11", "Industri"))
        ).get(0)).isEqualTo(StatistikkDto.builder()
              .statistikkategori(Statistikkategori.NÆRING)
              .label("Industri")
              .verdi("2.0")
              .antallPersonerIBeregningen(624)
              .kvartalerIBeregningen(
                    List.of(
                          SISTE_PUBLISERTE_KVARTAL,
                          SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1),
                          SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2),
                          SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3)
                    )
              ).build());
    }

    @Test
    public void getSykefraværMedKategoriForBransjeEllerNæringSiste4Kvartaler__skalHenteNæringForKunRiktigeKvartaler(){
        assertThat(EksporteringService.getSykefraværMedKategoriForBransjeEllerNæringSiste4Kvartaler(
              virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL,
              List.of(
                    byggSykefraværStatistikkNæring(virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL),
                    byggSykefraværStatistikkNæring(virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL,SISTE_PUBLISERTE_KVARTAL.plussKvartaler(1)),
                    byggSykefraværStatistikkNæring(virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL,SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3)),
                    byggSykefraværStatistikkNæring(virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL,SISTE_PUBLISERTE_KVARTAL.minusEttÅr())
              ),
              List.of(
                    byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL, "11000")
              ),
              SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3), new BransjeEllerNæring(new Næring("11", "Industri"))
        ).get(0)).isEqualTo(StatistikkDto.builder()
              .statistikkategori(Statistikkategori.NÆRING)
              .label("Industri")
              .verdi("2.0")
              .antallPersonerIBeregningen(312)
              .kvartalerIBeregningen(
                    List.of(
                          SISTE_PUBLISERTE_KVARTAL,
                          SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3)
                    )
              ).build());
    }
    @Test
    public void getSykefraværMedKategoriForBransjeSiste4Kvartaler__skalHenteTomtListe(){
        assertThat(EksporteringService.getSykefraværMedKategoriForBransjeEllerNæringSiste4Kvartaler(
              virksomhet1Metadata_2020_4, List.of(byggSykefraværStatistikkNæring(
                    virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL
              )), List.of(
                    byggSykefraværStatistikkNæring5Siffer(virksomhet1_TilHørerBransjeMetadata__2021_2, "86101")
              ),
              SISTE_PUBLISERTE_KVARTAL, new BransjeEllerNæring(new Bransje(ArbeidsmiljøportalenBransje.SYKEHUS,"86101", "86101","86102","86104"))
        ).size()).isEqualTo(0);
    }

    @Test
    public void getSykefraværMedKategoriForBransjeSiste4Kvartaler__skalHenteBransjeForSistePubliserteKvartal(){
        assertThat(EksporteringService.getSykefraværMedKategoriForBransjeEllerNæringSiste4Kvartaler(
              virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL,
              List.of(byggSykefraværStatistikkNæring(
                    virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL
              )),
              List.of(
                    byggSykefraværStatistikkNæring5Siffer(virksomhet1_TilHørerBransjeMetadata__SISTE_PUBLISERTE_KVARTAL, "86102")
              ),
              SISTE_PUBLISERTE_KVARTAL,
              new BransjeEllerNæring(new Bransje(ArbeidsmiljøportalenBransje.SYKEHUS,"Sykehus", "86101","86102","86104"))
        ).get(0)).isEqualTo(StatistikkDto.builder()
              .statistikkategori(Statistikkategori.BRANSJE)
              .label("Sykehus")
              .verdi("1.0")
              .antallPersonerIBeregningen(100)
              .kvartalerIBeregningen(List.of(SISTE_PUBLISERTE_KVARTAL)
              ).build());
    }

    // TODO fikse de tester
    @Test
    public void getSykefraværMedKategoriForBransjeSiste4Kvartaler__skalHenteBransjeFor4SisteKvartaler(){
        assertThat(EksporteringService.getSykefraværMedKategoriForBransjeEllerNæringSiste4Kvartaler(
              virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL,
              List.of(
                    byggSykefraværStatistikkNæring(virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL),
                    byggSykefraværStatistikkNæring(virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL,SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1)),
                    byggSykefraværStatistikkNæring(virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL,SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2)),
                    byggSykefraværStatistikkNæring(virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL,SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3)),
                    byggSykefraværStatistikkNæring(virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL,SISTE_PUBLISERTE_KVARTAL.minusEttÅr())
              ),
              List.of(
                    byggSykefraværStatistikkNæring5Siffer(virksomhet1_TilHørerBransjeMetadata__SISTE_PUBLISERTE_KVARTAL, "86104"),
                    byggSykefraværStatistikkNæring5Siffer(SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1), "86104"),
                    byggSykefraværStatistikkNæring5Siffer(SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1), "86106"),
                    byggSykefraværStatistikkNæring5Siffer(SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2), "86104"),
                    byggSykefraværStatistikkNæring5Siffer(SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2), "86106"),
                    byggSykefraværStatistikkNæring5Siffer(SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3), "86106"),
                    byggSykefraværStatistikkNæring5Siffer(SISTE_PUBLISERTE_KVARTAL.minusEttÅr(), "86104"),
                    byggSykefraværStatistikkNæring5Siffer(SISTE_PUBLISERTE_KVARTAL.minusEttÅr(), "86106")
              ),
              SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3),
              new BransjeEllerNæring(new Bransje(ArbeidsmiljøportalenBransje.SYKEHUS,"Sykehus", "86101","86102","86104","86106"))
        ).get(0)).isEqualTo(StatistikkDto.builder()
              .statistikkategori(Statistikkategori.BRANSJE)
              .label("Sykehus")
              .verdi("2.3")
              .antallPersonerIBeregningen(1100)
              .kvartalerIBeregningen(
                    List.of(
                          SISTE_PUBLISERTE_KVARTAL,
                          SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1),
                          SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2),
                          SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3)
                    )
              ).build());
    }

    @Test
    public void getSykefraværMedKategoriForBransjeSiste4Kvartaler__skalHenteBransjeForKunRiktigeKvartaler(){
        assertThat(EksporteringService.getSykefraværMedKategoriForBransjeEllerNæringSiste4Kvartaler(
              virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL,
              List.of(
                    byggSykefraværStatistikkNæring(virksomhet1Metadata_SISTE_PUBLISERTE_KVARTAL)
              ),
              List.of(
                    byggSykefraværStatistikkNæring5Siffer(SISTE_PUBLISERTE_KVARTAL.minusEttÅr(), "86101"),
                    byggSykefraværStatistikkNæring5Siffer(SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3), "86101"),
                    byggSykefraværStatistikkNæring5Siffer(virksomhet1_TilHørerBransjeMetadata__SISTE_PUBLISERTE_KVARTAL, "86101")
              ),
              SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3),
              new BransjeEllerNæring(new Bransje(ArbeidsmiljøportalenBransje.SYKEHUS,"Sykehus", "86101","86102","86104","86106"))
        ).get(0)).isEqualTo(StatistikkDto.builder()
              .statistikkategori(Statistikkategori.BRANSJE)
              .label("Sykehus")
              .verdi("1.6")
              .antallPersonerIBeregningen(300)
              .kvartalerIBeregningen(
                    List.of(
                          SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3),
                          SISTE_PUBLISERTE_KVARTAL
                    )
              ).build());
    }


    private static Map<String, SykefraværsstatistikkVirksomhetUtenVarighet>
    buildMapAvSykefraværsstatistikkPerVirksomhet(
            int size
    ) {
        Map<String, SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighetMap =
                new HashMap<>();

        int count = 1;
        do {
            sykefraværsstatistikkVirksomhetUtenVarighetMap.put(
                    Integer.toString(count),
                    new SykefraværsstatistikkVirksomhetUtenVarighet(
                            2020,
                            4,
                            Integer.toString(count),
                            10,
                            new BigDecimal(100),
                            new BigDecimal(1000)
                    )
            );
            count++;
        } while (count < size);
        return sykefraværsstatistikkVirksomhetUtenVarighetMap;
    }
}

package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.SISTE_PUBLISERTE_KVARTAL
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringServiceUtils
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.ORGNR_VIRKSOMHET_1
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.ORGNR_VIRKSOMHET_2
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.__2020_4
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.__2021_2
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.assertEqualsSykefraværMedKategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.assertEqualsVirksomhetSykefravær
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.byggSykefraværStatistikkNæring
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.byggSykefraværStatistikkNæring5Siffer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.byggSykefraværStatistikkSektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.byggSykefraværsstatistikkVirksomhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.virksomhet1Metadata_2020_4
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.virksomhet1Metadata_2021_1
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.virksomhet1Metadata_2021_2
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.virksomhet2Metadata_2020_4
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.virksomhet3Metadata_2020_4
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.collections.set

class EksporteringServiceUtilsTest {
    @Test
    fun virksomhetMetadataHashMap__returnerer_en_map_med_orgnr_som_key() {
        val virksomhetMetadataHashMap = EksporteringServiceUtils.getVirksomhetMetadataHashMap(
            listOf(virksomhet1Metadata_2020_4, virksomhet2Metadata_2020_4)
        )
        Assertions.assertEquals(2, virksomhetMetadataHashMap.size)
        Assertions.assertEquals(
            virksomhet1Metadata_2020_4,
            virksomhetMetadataHashMap[virksomhet1Metadata_2020_4.orgnr]
        )
        Assertions.assertEquals(
            virksomhet2Metadata_2020_4,
            virksomhetMetadataHashMap[virksomhet2Metadata_2020_4.orgnr]
        )
    }

    @Test
    fun virksomheterMetadataFraSubset__returnerer_intersection() {
        val virksomhetMetadataHashMap: MutableMap<String, VirksomhetMetadata> = HashMap()
        virksomhetMetadataHashMap[virksomhet1Metadata_2020_4.orgnr] = virksomhet1Metadata_2020_4
        virksomhetMetadataHashMap[virksomhet2Metadata_2020_4.orgnr] = virksomhet2Metadata_2020_4
        virksomhetMetadataHashMap[virksomhet3Metadata_2020_4.orgnr] = virksomhet3Metadata_2020_4
        val virksomhetMetadataList = EksporteringServiceUtils.getVirksomheterMetadataFraSubset(
            virksomhetMetadataHashMap,
            listOf(
                VirksomhetEksportPerKvartal(ORGNR_VIRKSOMHET_1, __2020_4, false),
                VirksomhetEksportPerKvartal(ORGNR_VIRKSOMHET_2, __2020_4, false)
            )
        )
        Assertions.assertEquals(2, virksomhetMetadataList.size)
        Assertions.assertTrue(virksomhetMetadataList.contains(virksomhet1Metadata_2020_4))
        Assertions.assertTrue(virksomhetMetadataList.contains(virksomhet2Metadata_2020_4))
    }

    @Test
    fun virksomhetSykefravær__returnerer_VirksomhetSykefravær_uten_statistikk__dersom_ingen_entry_matcher_Virksomhet() {
        val actualVirksomhetSykefravær = EksporteringServiceUtils.getVirksomhetSykefravær(
            virksomhet1Metadata_2020_4, buildMapAvSykefraværsstatistikkPerVirksomhet(10)
        )
        val expectedVirksomhetSykefravær = VirksomhetSykefravær(
            virksomhet1Metadata_2020_4.orgnr,
            virksomhet1Metadata_2020_4.navn,
            2020,
            4,
            null,
            null,
            0
        )
        assertEqualsVirksomhetSykefravær(expectedVirksomhetSykefravær, actualVirksomhetSykefravær)
    }

    @Test
    fun virksomhetSykefravær__returnerer_VirksomhetSykefravær_som_matcher_Virksomhet() {
        val virksomhetToBeFound = VirksomhetMetadata(
            Orgnr("399000"),
            "Virksomhet 1",
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            Sektor.STATLIG,
            "11",
            "11111",
            __2020_4
        )
        val bigMap = buildMapAvSykefraværsstatistikkPerVirksomhet(500000)
        val startWithMap = System.nanoTime()
        val actualVirksomhetSykefravær =
            EksporteringServiceUtils.getVirksomhetSykefravær(virksomhetToBeFound, bigMap)
        val stopWithMap = System.nanoTime()
        val expectedVirksomhetSykefravær = VirksomhetSykefravær(
            virksomhetToBeFound.orgnr,
            virksomhetToBeFound.navn,
            2020,
            4,
            BigDecimal(100),
            BigDecimal(1000),
            10
        )
        assertEqualsVirksomhetSykefravær(expectedVirksomhetSykefravær, actualVirksomhetSykefravær)
        println("Elapsed time in nanoseconds (With Map) = " + (stopWithMap - startWithMap))
    }

    @Test
    fun sykefraværMedKategoriForSektor__returnerer_SykefraværMedKategori__med_sykefraværsstatistikk_for_sektor() {
        val resultat = EksporteringServiceUtils.getSykefraværMedKategoriForSektor(
            virksomhet1Metadata_2020_4,
            listOf(
                byggSykefraværStatistikkSektor(virksomhet1Metadata_2020_4, 10, 156, 22233),
                byggSykefraværStatistikkSektor(virksomhet2Metadata_2020_4)
            )
        )
        assertEqualsSykefraværMedKategori(
            byggSykefraværStatistikkSektor(virksomhet1Metadata_2020_4, 10, 156, 22233),
            resultat,
            Statistikkategori.SEKTOR,
            virksomhet1Metadata_2020_4.sektor.sektorkode
        )
    }

    @Test
    fun sykefraværMedKategoriForNæring__returnerer_SykefraværMedKategori__med_sykefraværsstatistikk_for_næring() {
        val resultat = EksporteringServiceUtils.getSykefraværMedKategoriNæringForVirksomhet(
            virksomhet1Metadata_2020_4,
            listOf(
                byggSykefraværStatistikkNæring(virksomhet1Metadata_2020_4, 10, 156, 22233),
                byggSykefraværStatistikkNæring(virksomhet2Metadata_2020_4)
            )
        )
        assertEqualsSykefraværMedKategori(
            byggSykefraværStatistikkNæring(virksomhet1Metadata_2020_4, 10, 156, 22233),
            resultat,
            Statistikkategori.NÆRING,
            virksomhet1Metadata_2020_4.primærnæring
        )
    }

    @Test
    fun sykefraværMedKategoriForNæring5Siffer__returnerer_SykefraværMedKategori__med_sykefraværsstatistikk_for_næring_5_siffer() {
        virksomhet1Metadata_2020_4.leggTilNæringOgNæringskode5siffer(
            listOf(
                Næringskode("85000"),
                Næringskode("11000")
            )
        )
        val resultat = EksporteringServiceUtils.getSykefraværMedKategoriForNæring5Siffer(
            virksomhet1Metadata_2020_4,
            listOf(
                byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "11000"),
                byggSykefraværStatistikkNæring5Siffer(virksomhet2Metadata_2020_4, "85000")
            )
        )
        assertThat(resultat.size).isEqualTo(2)
        val sykefraværMedKategori85000 = resultat.first { r: SykefraværMedKategori -> "85000" == r.kode }
        assertEqualsSykefraværMedKategori(
            byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "85000"),
            sykefraværMedKategori85000,
            Statistikkategori.NÆRINGSKODE,
            virksomhet1Metadata_2020_4.næringOgNæringskode5siffer[1].femsifferIdentifikator
        )
        val sykefraværMedKategori11000 = resultat.first { r: SykefraværMedKategori -> "11000" == r.kode }
        assertEqualsSykefraværMedKategori(
            byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "11000"),
            sykefraværMedKategori11000,
            Statistikkategori.NÆRINGSKODE,
            virksomhet1Metadata_2020_4.næringOgNæringskode5siffer[0].femsifferIdentifikator
        )
    }

    @Test
    fun sykefraværsstatistikkNæring5Siffers__skal_returnere_riktig_liste() {
        val virksomhetMetadata_2020_4_med_næring5siffer: VirksomhetMetadata = virksomhet1Metadata_2020_4
        virksomhetMetadata_2020_4_med_næring5siffer.leggTilNæringOgNæringskode5siffer(
            listOf(
                Næringskode("11000"),
                Næringskode("85000")
            )
        )
        val sykefraværsstatistikkForNæringskodeList = listOf(
            byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "11000"),
            byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "45210"),
            byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "85000")
        )
        val resultat = EksporteringServiceUtils.getSykefraværsstatistikkNæring5Siffers(
            virksomhetMetadata_2020_4_med_næring5siffer, sykefraværsstatistikkForNæringskodeList
        )
        assertThat(resultat.size).isEqualTo(2)
        assertThat(
            resultat.contains(
                byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "11000")
            )
        )
        assertThat(
            resultat.contains(
                byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "85000")
            )
        )
        assertThat(
            !resultat.contains(
                byggSykefraværStatistikkNæring5Siffer(virksomhet1Metadata_2020_4, "45210")
            )
        )
    }

    @Test
    fun filterByKvartal_skalIkkeFeile() {
        assertThat(
            EksporteringServiceUtils.filterByKvartal(
                SISTE_PUBLISERTE_KVARTAL,
                listOf(
                    byggSykefraværsstatistikkVirksomhet(
                        virksomhet1Metadata_2020_4
                    )
                )
            )
        )
            .isEmpty()
    }

    @Test
    fun filterByKvartal_skalReturnereRiktigKvartal() {
        val ønskedeResultat__2021_2 = listOf(
            byggSykefraværsstatistikkVirksomhet(virksomhet1Metadata_2021_2)
        )
        val actual__2021_2 = EksporteringServiceUtils.filterByKvartal(
            __2021_2,
            listOf(
                byggSykefraværsstatistikkVirksomhet(virksomhet1Metadata_2020_4),
                byggSykefraværsstatistikkVirksomhet(virksomhet1Metadata_2021_1),
                byggSykefraværsstatistikkVirksomhet(virksomhet1Metadata_2021_2)
            )
        )
        assertThat(actual__2021_2).size().isEqualTo(1)
        assertThat(actual__2021_2)
            .containsExactlyInAnyOrderElementsOf(ønskedeResultat__2021_2)
    }

    @Test
    fun filterByKvartal_skalReturnereAlleVirksomheterForØnskedeKvartal() {
        val ønskedeResultat__2020_4 = listOf(
            byggSykefraværsstatistikkVirksomhet(virksomhet1Metadata_2020_4),
            byggSykefraværsstatistikkVirksomhet(virksomhet2Metadata_2020_4)
        )
        val actual__2020_4 = EksporteringServiceUtils.filterByKvartal(
            __2020_4,
            listOf(
                byggSykefraværsstatistikkVirksomhet(virksomhet1Metadata_2020_4),
                byggSykefraværsstatistikkVirksomhet(virksomhet2Metadata_2020_4),
                byggSykefraværsstatistikkVirksomhet(virksomhet1Metadata_2021_1),
                byggSykefraværsstatistikkVirksomhet(virksomhet1Metadata_2021_2)
            )
        )
        assertThat(actual__2020_4).size().isEqualTo(2)
        assertThat(actual__2020_4)
            .containsExactlyInAnyOrderElementsOf(ønskedeResultat__2020_4)
    }

    companion object {
        private fun buildMapAvSykefraværsstatistikkPerVirksomhet(size: Int): Map<String, SykefraværsstatistikkVirksomhetUtenVarighet> {
            val sykefraværsstatistikkVirksomhetUtenVarighetMap: MutableMap<String, SykefraværsstatistikkVirksomhetUtenVarighet> =
                HashMap()
            var count = 1
            do {
                sykefraværsstatistikkVirksomhetUtenVarighetMap[count.toString()] =
                    SykefraværsstatistikkVirksomhetUtenVarighet(
                        2020, 4, count.toString(), 10, BigDecimal(100), BigDecimal(1000)
                    )
                count++
            } while (count < size)
            return sykefraværsstatistikkVirksomhetUtenVarighetMap
        }
    }
}

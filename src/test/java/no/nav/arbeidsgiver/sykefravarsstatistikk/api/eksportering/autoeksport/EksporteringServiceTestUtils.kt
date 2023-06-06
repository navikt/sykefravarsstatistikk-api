package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AssertUtils
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetEksportPerKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.DatavarehusRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal
import org.assertj.core.api.Assertions
import java.math.BigDecimal

object EksporteringServiceTestUtils {
    // Data for testing & Utilities
    @JvmField
    val __2021_2 = ÅrstallOgKvartal(2021, 2)
    @JvmField
    val __2021_1 = ÅrstallOgKvartal(2021, 1)
    @JvmField
    val __2020_4 = ÅrstallOgKvartal(2020, 4)
    @JvmField
    val __2020_2 = ÅrstallOgKvartal(2020, 2)
    @JvmField
    val __2020_1 = ÅrstallOgKvartal(2020, 1)
    @JvmField
    val __2019_4 = ÅrstallOgKvartal(2019, 4)
    @JvmField
    val __2019_3 = ÅrstallOgKvartal(2019, 3)

    @JvmField
    var ORGNR_VIRKSOMHET_1 = Orgnr("987654321")

    @JvmField
    var ORGNR_VIRKSOMHET_2 = Orgnr("912345678")
    var ORGNR_VIRKSOMHET_3 = Orgnr("999966633")

    @JvmField
    var virksomhetSykefravær = VirksomhetSykefravær(
        "987654321", "Virksomhet 1", __2020_2, BigDecimal(10), BigDecimal(500), 6
    )

    @JvmField
    var næringSykefravær = SykefraværMedKategori(
        Statistikkategori.NÆRING, "11", __2020_2, BigDecimal(100), BigDecimal(5000), 150
    )

    @JvmField
    var næring5SifferSykefravær = SykefraværMedKategori(
        Statistikkategori.NÆRING5SIFFER,
        "11000",
        __2020_2,
        BigDecimal(40),
        BigDecimal(4000),
        1250
    )

    @JvmField
    var næring5SifferSykefraværTilhørerBransje = SykefraværMedKategori(
        Statistikkategori.NÆRING5SIFFER,
        "86101",
        __2020_2,
        BigDecimal(80),
        BigDecimal(6000),
        1000
    )

    @JvmField
    var sektorSykefravær = SykefraværMedKategori(
        Statistikkategori.SEKTOR,
        "1",
        __2020_2,
        BigDecimal(1340),
        BigDecimal(88000),
        33000
    )

    @JvmField
    var landSykefravær = SykefraværMedKategori(
        Statistikkategori.LAND,
        "NO",
        __2020_2,
        BigDecimal(10000000),
        BigDecimal(500000000),
        2500000
    )
    var virksomhetSykefraværMedKategori = SykefraværMedKategori(
        Statistikkategori.VIRKSOMHET,
        "987654321",
        __2020_2,
        BigDecimal(10),
        BigDecimal(500),
        2500000
    )


    @JvmField
    var virksomhet1Metadata_2020_4 = VirksomhetMetadata(
        ORGNR_VIRKSOMHET_1, "Virksomhet 1", DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET, "1", "11", "11111", __2020_4
    )

    @JvmField
    val virksomhet2Metadata_2020_4 = VirksomhetMetadata(
        ORGNR_VIRKSOMHET_2, "Virksomhet 2", DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET, "2", "22", "22222", __2020_4
    )

    @JvmField
    val virksomhet3Metadata_2020_4 = VirksomhetMetadata(
        ORGNR_VIRKSOMHET_3, "Virksomhet 3", DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET, "3", "33", "33333", __2020_4
    )

    @JvmField
    val virksomhet1Metadata_2021_1 = VirksomhetMetadata(
        ORGNR_VIRKSOMHET_1, "Virksomhet 1", DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET, "1", "11", "11111", __2021_1
    )

    @JvmField
    val virksomhet1Metadata_2021_2 = VirksomhetMetadata(
        ORGNR_VIRKSOMHET_1, "Virksomhet 1", DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET, "1", "11", "11111", __2021_2
    )

    @JvmStatic
    fun virksomhet1_TilHørerBransjeMetadata(
        årstallOgKvartal: ÅrstallOgKvartal?
    ): VirksomhetMetadata {
        return VirksomhetMetadata(
            ORGNR_VIRKSOMHET_1,
            "Virksomhet 1",
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            "1",
            "86",
            "86000",
            årstallOgKvartal!!
        )
    }

    @JvmStatic
    @JvmOverloads
    fun byggSykefraværsstatistikkVirksomhet(
        virksomhetMetadata: VirksomhetMetadata,
        antallPersoner: Int = 156,
        tapteDagsverk: Int = 3678,
        muligeDagsverk: Int = 188000
    ): SykefraværsstatistikkVirksomhetUtenVarighet {
        return SykefraværsstatistikkVirksomhetUtenVarighet(
            virksomhetMetadata.årstall,
            virksomhetMetadata.kvartal,
            virksomhetMetadata.orgnr,
            antallPersoner,
            BigDecimal(tapteDagsverk),
            BigDecimal(muligeDagsverk)
        )
    }

    @JvmStatic
    fun tomVirksomhetSykefravær(
        virksomhetMetadata: VirksomhetMetadata
    ): VirksomhetSykefravær {
        return VirksomhetSykefravær(
            virksomhetMetadata.orgnr,
            virksomhetMetadata.navn,
            ÅrstallOgKvartal(virksomhetMetadata.årstall, virksomhetMetadata.kvartal),
            null,
            null,
            0
        )
    }

    @JvmStatic
    fun byggSykefraværStatistikkNæring(
        virksomhetMetadata: VirksomhetMetadata,
        antallPersoner: Int,
        tapteDagsverk: Int,
        muligeDagsverk: Int
    ): SykefraværsstatistikkNæring {
        return SykefraværsstatistikkNæring(
            virksomhetMetadata.årstall,
            virksomhetMetadata.kvartal,
            virksomhetMetadata.primærnæring,
            antallPersoner,
            BigDecimal(tapteDagsverk),
            BigDecimal(muligeDagsverk)
        )
    }

    @JvmStatic
    fun byggSykefraværStatistikkNæring(
        virksomhetMetadata: VirksomhetMetadata
    ): SykefraværsstatistikkNæring {
        return SykefraværsstatistikkNæring(
            virksomhetMetadata.årstall,
            virksomhetMetadata.kvartal,
            virksomhetMetadata.primærnæring,
            156,
            BigDecimal(3678),
            BigDecimal(188000)
        )
    }

    @JvmStatic
    fun byggSykefraværStatistikkNæring(
        virksomhetMetadata: VirksomhetMetadata, statistikkÅrstallOgKvartal: ÅrstallOgKvartal
    ): SykefraværsstatistikkNæring {
        return SykefraværsstatistikkNæring(
            statistikkÅrstallOgKvartal.årstall,
            statistikkÅrstallOgKvartal.kvartal,
            virksomhetMetadata.primærnæring,
            156,
            BigDecimal(3678),
            BigDecimal(188000)
        )
    }

    @JvmStatic
    fun byggSykefraværStatistikkNæring5Siffer(
        virksomhetMetadata: VirksomhetMetadata, næringskode5Siffer: String?
    ): SykefraværsstatistikkNæring5Siffer {
        return SykefraværsstatistikkNæring5Siffer(
            virksomhetMetadata.årstall,
            virksomhetMetadata.kvartal,
            næringskode5Siffer,
            100,
            BigDecimal(250),
            BigDecimal(25000)
        )
    }

    @JvmStatic
    fun byggSykefraværStatistikkNæring5Siffer(
        statistikkÅrstallOgKvartal: ÅrstallOgKvartal, næringskode5Siffer: String?
    ): SykefraværsstatistikkNæring5Siffer {
        return SykefraværsstatistikkNæring5Siffer(
            statistikkÅrstallOgKvartal.årstall,
            statistikkÅrstallOgKvartal.kvartal,
            næringskode5Siffer,
            200,
            BigDecimal(300),
            BigDecimal(10000)
        )
    }

    @JvmStatic
    fun byggSykefraværStatistikkSektor(
        virksomhetMetadata: VirksomhetMetadata,
        antallPersoner: Int,
        tapteDagsverk: Int,
        muligeDagsverk: Int
    ): SykefraværsstatistikkSektor {
        return SykefraværsstatistikkSektor(
            virksomhetMetadata.årstall,
            virksomhetMetadata.kvartal,
            virksomhetMetadata.sektor,
            antallPersoner,
            BigDecimal(tapteDagsverk),
            BigDecimal(muligeDagsverk)
        )
    }

    @JvmStatic
    fun byggSykefraværStatistikkSektor(
        virksomhetMetadata: VirksomhetMetadata
    ): SykefraværsstatistikkSektor {
        return SykefraværsstatistikkSektor(
            virksomhetMetadata.årstall,
            virksomhetMetadata.kvartal,
            virksomhetMetadata.sektor,
            156,
            BigDecimal(3678),
            BigDecimal(188000)
        )
    }

    @JvmStatic
    fun byggVirksomhetSykefravær(
        virksomhetMetadata: VirksomhetMetadata,
        antallPersoner: Int,
        tapteDagsverk: Int,
        muligeDagsverk: Int
    ): VirksomhetSykefravær {
        return VirksomhetSykefravær(
            virksomhetMetadata.orgnr,
            virksomhetMetadata.navn,
            ÅrstallOgKvartal(virksomhetMetadata.årstall, virksomhetMetadata.kvartal),
            BigDecimal(tapteDagsverk),
            BigDecimal(muligeDagsverk),
            antallPersoner
        )
    }

    @JvmStatic
    fun byggVirksomhetSykefravær(
        virksomhetMetadata: VirksomhetMetadata
    ): VirksomhetSykefravær {
        return VirksomhetSykefravær(
            virksomhetMetadata.orgnr,
            virksomhetMetadata.navn,
            ÅrstallOgKvartal(virksomhetMetadata.årstall, virksomhetMetadata.kvartal),
            BigDecimal(3678),
            BigDecimal(188000),
            156
        )
    }

    @JvmStatic
    fun sykefraværsstatistikkVirksomhet(
        årstallOgKvartal: ÅrstallOgKvartal, orgnr: String?
    ): SykefraværsstatistikkVirksomhetUtenVarighet {
        return SykefraværsstatistikkVirksomhetUtenVarighet(
            årstallOgKvartal.årstall,
            årstallOgKvartal.kvartal,
            orgnr,
            6,
            BigDecimal(10),
            BigDecimal(500)
        )
    }

    @JvmField
    val virksomhetEksportPerKvartal = VirksomhetEksportPerKvartal(Orgnr("987654321"), __2020_2, false)

    @JvmField
    val virksomhetMetadata = VirksomhetMetadata(Orgnr("987654321"), "Virksomhet 1", "2", "1", "11", "11111", __2020_2)

    @JvmStatic
    fun sykefraværsstatistikkLandSiste4Kvartaler(
        årstallOgKvartal: ÅrstallOgKvartal
    ): List<UmaskertSykefraværForEttKvartal> {
        return java.util.List.of(
            UmaskertSykefraværForEttKvartal(
                årstallOgKvartal, BigDecimal(10000000), BigDecimal(500000000), 2500000
            ),
            UmaskertSykefraværForEttKvartal(
                årstallOgKvartal.minusKvartaler(1),
                BigDecimal(9000000),
                BigDecimal(500000000),
                2500000
            ),
            UmaskertSykefraværForEttKvartal(
                årstallOgKvartal.minusKvartaler(2),
                BigDecimal(11000000),
                BigDecimal(500000000),
                2500000
            ),
            UmaskertSykefraværForEttKvartal(
                årstallOgKvartal.minusKvartaler(3),
                BigDecimal(8000000),
                BigDecimal(500000000),
                2500000
            )
        )
    }

    @JvmField
    val sykefraværsstatistikkSektor = SykefraværsstatistikkSektor(
        __2020_2.årstall,
        __2020_2.kvartal,
        "1",
        33000,
        BigDecimal(1340),
        BigDecimal(88000)
    )

    @JvmField
    val sykefraværsstatistikkNæring = SykefraværsstatistikkNæring(
        __2020_2.årstall,
        __2020_2.kvartal,
        "11",
        150,
        BigDecimal(100),
        BigDecimal(5000)
    )

    @JvmStatic
    @JvmOverloads
    fun sykefraværsstatistikkNæring(
        årstallOgKvartal: ÅrstallOgKvartal, næringskode: String? = "11"
    ): SykefraværsstatistikkNæring {
        return SykefraværsstatistikkNæring(
            årstallOgKvartal.årstall,
            årstallOgKvartal.kvartal,
            næringskode!!,
            150,
            BigDecimal(100),
            BigDecimal(5000)
        )
    }

    @JvmField
    val sykefraværsstatistikkNæring5Siffer = SykefraværsstatistikkNæring5Siffer(
        __2020_2.årstall,
        __2020_2.kvartal,
        "11000",
        1250,
        BigDecimal(40),
        BigDecimal(4000)
    )

    @JvmStatic
    fun sykefraværsstatistikkNæring5SifferBransjeprogram(
        næringskode5Siffer: String?, årstallOgKvartal: ÅrstallOgKvartal
    ): SykefraværsstatistikkNæring5Siffer {
        return SykefraværsstatistikkNæring5Siffer(
            årstallOgKvartal.årstall,
            årstallOgKvartal.kvartal,
            næringskode5Siffer,
            1000,
            BigDecimal(80),
            BigDecimal(6000)
        )
    }

    fun assertEqualsSykefraværFlereKvartalerForEksport(
        expected: SykefraværFlereKvartalerForEksport, actual: SykefraværFlereKvartalerForEksport
    ) {
        Assertions.assertThat(actual.prosent).`as`("Sjekk prosent").isEqualByComparingTo(expected.prosent)
        Assertions.assertThat(actual.kvartaler)
            .`as`("Sjekk listen av kvartaler")
            .isEqualTo(expected.kvartaler)
        AssertUtils.assertBigDecimalIsEqual(actual.muligeDagsverk, expected.muligeDagsverk)
        AssertUtils.assertBigDecimalIsEqual(actual.tapteDagsverk, expected.tapteDagsverk)
    }

    @JvmStatic
    fun assertEqualsVirksomhetSykefravær(
        expected: VirksomhetSykefravær, actual: VirksomhetSykefravær
    ) {
        Assertions.assertThat(actual.Årstall).isEqualTo(expected.Årstall)
        Assertions.assertThat(actual.kvartal).isEqualTo(expected.kvartal)
        Assertions.assertThat(actual.orgnr).isEqualTo(expected.orgnr)
        AssertUtils.assertBigDecimalIsEqual(actual.muligeDagsverk, expected.muligeDagsverk)
        AssertUtils.assertBigDecimalIsEqual(actual.tapteDagsverk, expected.tapteDagsverk)
    }

    @JvmStatic
    fun assertEqualsSykefraværMedKategori(
        expected: Sykefraværsstatistikk,
        actual: SykefraværMedKategori,
        expectedKategori: Statistikkategori?,
        expectedKode: String?
    ) {
        Assertions.assertThat(actual.kategori).`as`("Sjekk Statistikkategori").isEqualTo(expectedKategori)
        Assertions.assertThat(actual.kode).`as`("Sjekk kode").isEqualTo(expectedKode)
        Assertions.assertThat(actual.Årstall).`as`("Sjekk årstall").isEqualTo(expected.Årstall)
        Assertions.assertThat(actual.kvartal).`as`("Sjekk kvartal").isEqualTo(expected.kvartal)
        AssertUtils.assertBigDecimalIsEqual(actual.muligeDagsverk, expected.muligeDagsverk)
        AssertUtils.assertBigDecimalIsEqual(actual.tapteDagsverk, expected.tapteDagsverk)
    }

    @JvmStatic
    fun assertEqualsSykefraværMedKategori(
        expected: SykefraværMedKategori, actual: SykefraværMedKategori
    ) {
        Assertions.assertThat(actual.kategori)
            .`as`("Sjekk Statistikkategori")
            .isEqualTo(expected.kategori)
        Assertions.assertThat(actual.kode).`as`("Sjekk kode").isEqualTo(expected.kode)
        Assertions.assertThat(actual.Årstall).`as`("Sjekk årstall").isEqualTo(expected.Årstall)
        Assertions.assertThat(actual.kvartal).`as`("Sjekk kvartal").isEqualTo(expected.kvartal)
        AssertUtils.assertBigDecimalIsEqual(actual.muligeDagsverk, expected.muligeDagsverk)
        AssertUtils.assertBigDecimalIsEqual(actual.tapteDagsverk, expected.tapteDagsverk)
    }
}

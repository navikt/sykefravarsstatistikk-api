package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AssertUtils
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.domene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository
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
    var virksomhetSykefravær =
        VirksomhetSykefravær(
            "987654321", "Virksomhet 1", __2020_2, BigDecimal(10), BigDecimal(500), 6
        )

    @JvmField
    var næringSykefravær =
        SykefraværMedKategori(
            Statistikkategori.NÆRING, "11", __2020_2, BigDecimal(100), BigDecimal(5000), 150
        )

    @JvmField
    var næring5SifferSykefravær =
        SykefraværMedKategori(
            Statistikkategori.NÆRINGSKODE,
            "11000",
            __2020_2,
            BigDecimal(40),
            BigDecimal(4000),
            1250
        )

    @JvmField
    var næring5SifferSykefraværTilhørerBransje =
        SykefraværMedKategori(
            Statistikkategori.NÆRINGSKODE,
            "86101",
            __2020_2,
            BigDecimal(80),
            BigDecimal(6000),
            1000
        )

    @JvmField
    var sektorSykefravær =
        SykefraværMedKategori(
            Statistikkategori.SEKTOR,
            Sektor.STATLIG.sektorkode,
            __2020_2,
            BigDecimal(1340),
            BigDecimal(88000),
            33000
        )

    @JvmField
    var landSykefravær =
        SykefraværMedKategori(
            Statistikkategori.LAND,
            "NO",
            __2020_2,
            BigDecimal(10000000),
            BigDecimal(500000000),
            2500000
        )

    @JvmField
    var virksomhet1Metadata_2020_4 = VirksomhetMetadata(
        ORGNR_VIRKSOMHET_1, "Virksomhet 1", DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET, Sektor.STATLIG, "11", "11111", __2020_4
    )

    @JvmField
    val virksomhet2Metadata_2020_4 = VirksomhetMetadata(
        ORGNR_VIRKSOMHET_2, "Virksomhet 2", DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET, Sektor.KOMMUNAL, "22", "22222", __2020_4
    )

    @JvmField
    val virksomhet3Metadata_2020_4 = VirksomhetMetadata(
        ORGNR_VIRKSOMHET_3, "Virksomhet 3", DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET, Sektor.PRIVAT, "33", "33333", __2020_4
    )

    @JvmField
    val virksomhet1Metadata_2021_1 = VirksomhetMetadata(
        ORGNR_VIRKSOMHET_1, "Virksomhet 1", DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET, Sektor.STATLIG, "11", "11111", __2021_1
    )

    @JvmField
    val virksomhet1Metadata_2021_2 = VirksomhetMetadata(
        ORGNR_VIRKSOMHET_1, "Virksomhet 1", DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET, Sektor.STATLIG, "11", "11111", __2021_2
    )


    fun virksomhet1_TilHørerBransjeMetadata(
        årstallOgKvartal: ÅrstallOgKvartal?
    ): VirksomhetMetadata {
        return VirksomhetMetadata(
            ORGNR_VIRKSOMHET_1,
            "Virksomhet 1",
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            Sektor.STATLIG,
            "86",
            "86000",
            årstallOgKvartal!!
        )
    }


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


    fun byggSykefraværStatistikkNæring(
        virksomhetMetadata: VirksomhetMetadata,
        antallPersoner: Int,
        tapteDagsverk: Int,
        muligeDagsverk: Int
    ): SykefraværsstatistikkForNæring {
        return SykefraværsstatistikkForNæring(
            virksomhetMetadata.årstall,
            virksomhetMetadata.kvartal,
            virksomhetMetadata.primærnæring,
            antallPersoner,
            BigDecimal(tapteDagsverk),
            BigDecimal(muligeDagsverk)
        )
    }


    fun byggSykefraværStatistikkNæring(
        virksomhetMetadata: VirksomhetMetadata
    ): SykefraværsstatistikkForNæring {
        return SykefraværsstatistikkForNæring(
            virksomhetMetadata.årstall,
            virksomhetMetadata.kvartal,
            virksomhetMetadata.primærnæring,
            156,
            BigDecimal(3678),
            BigDecimal(188000)
        )
    }


    fun byggSykefraværStatistikkNæring5Siffer(
        virksomhetMetadata: VirksomhetMetadata, næringskode5Siffer: String
    ): SykefraværsstatistikkForNæringskode {
        return SykefraværsstatistikkForNæringskode(
            virksomhetMetadata.årstall,
            virksomhetMetadata.kvartal,
            næringskode5Siffer,
            100,
            BigDecimal(250),
            BigDecimal(25000)
        )
    }


    fun byggSykefraværStatistikkSektor(
        virksomhetMetadata: VirksomhetMetadata,
        antallPersoner: Int,
        tapteDagsverk: Int,
        muligeDagsverk: Int
    ): SykefraværsstatistikkSektor {
        return SykefraværsstatistikkSektor(
            virksomhetMetadata.årstall,
            virksomhetMetadata.kvartal,
            virksomhetMetadata.sektor.sektorkode,
            antallPersoner,
            BigDecimal(tapteDagsverk),
            BigDecimal(muligeDagsverk)
        )
    }


    fun byggSykefraværStatistikkSektor(
        virksomhetMetadata: VirksomhetMetadata
    ): SykefraværsstatistikkSektor {
        return SykefraværsstatistikkSektor(
            virksomhetMetadata.årstall,
            virksomhetMetadata.kvartal,
            virksomhetMetadata.sektor.sektorkode,
            156,
            BigDecimal(3678),
            BigDecimal(188000)
        )
    }


    fun sykefraværsstatistikkVirksomhet(
        årstallOgKvartal: ÅrstallOgKvartal, orgnr: String
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
    val virksomhetEksportPerKvartal =
        VirksomhetEksportPerKvartal(
            Orgnr("987654321"),
            __2020_2,
            false
        )

    @JvmField
    val virksomhetMetadata = VirksomhetMetadata(Orgnr("987654321"), "Virksomhet 1", "2", Sektor.STATLIG, "11", "11111", __2020_2)


    fun sykefraværsstatistikkLandSiste4Kvartaler(
        årstallOgKvartal: ÅrstallOgKvartal
    ): List<UmaskertSykefraværForEttKvartal> {
        return listOf(
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
        Sektor.STATLIG.sektorkode,
        33000,
        BigDecimal(1340),
        BigDecimal(88000)
    )


    @JvmOverloads
    fun sykefraværsstatistikkNæring(
        årstallOgKvartal: ÅrstallOgKvartal, næringskode: String? = "11"
    ): SykefraværsstatistikkForNæring {
        return SykefraværsstatistikkForNæring(
            årstallOgKvartal.årstall,
            årstallOgKvartal.kvartal,
            næringskode!!,
            150,
            BigDecimal(100),
            BigDecimal(5000)
        )
    }


    @JvmOverloads
    fun sykefraværsstatistikkNæringskode(
        årstallOgKvartal: ÅrstallOgKvartal,
        næringskode: String = "11001",
        antallPersoner: Int = 150,
        tapteDagsverk: BigDecimal = BigDecimal(100),
        muligeDagsverk: BigDecimal = BigDecimal(5000)

    ): SykefraværsstatistikkForNæringskode {
        return SykefraværsstatistikkForNæringskode(
                årstallOgKvartal.årstall,
                årstallOgKvartal.kvartal,
                næringskode,
                antallPersoner,
                tapteDagsverk,
                muligeDagsverk
        )
    }

    @JvmField
    val sykefraværsstatistikkForNæringskode = SykefraværsstatistikkForNæringskode(
        __2020_2.årstall,
        __2020_2.kvartal,
        "11000",
        1250,
        BigDecimal(40),
        BigDecimal(4000)
    )


    fun sykefraværsstatistikkNæring5SifferBransjeprogram(
        næringskode5Siffer: String, årstallOgKvartal: ÅrstallOgKvartal
    ): SykefraværsstatistikkForNæringskode {
        return SykefraværsstatistikkForNæringskode(
            årstallOgKvartal.årstall,
            årstallOgKvartal.kvartal,
            næringskode5Siffer,
            1000,
            BigDecimal(80),
            BigDecimal(6000)
        )
    }


    fun assertEqualsVirksomhetSykefravær(
        expected: VirksomhetSykefravær, actual: VirksomhetSykefravær
    ) {
        Assertions.assertThat(actual.Årstall).isEqualTo(expected.Årstall)
        Assertions.assertThat(actual.kvartal).isEqualTo(expected.kvartal)
        Assertions.assertThat(actual.orgnr).isEqualTo(expected.orgnr)
        AssertUtils.assertBigDecimalIsEqual(actual.muligeDagsverk, expected.muligeDagsverk)
        AssertUtils.assertBigDecimalIsEqual(actual.tapteDagsverk, expected.tapteDagsverk)
    }


    fun assertEqualsSykefraværMedKategori(
        expected: Sykefraværsstatistikk,
        actual: SykefraværMedKategori,
        expectedKategori: Statistikkategori?,
        expectedKode: String?
    ) {
        Assertions.assertThat(actual.kategori).`as`("Sjekk Statistikkategori").isEqualTo(expectedKategori)
        Assertions.assertThat(actual.kode).`as`("Sjekk kode").isEqualTo(expectedKode)
        Assertions.assertThat(actual.Årstall).`as`("Sjekk årstall").isEqualTo(expected.årstall)
        Assertions.assertThat(actual.kvartal).`as`("Sjekk kvartal").isEqualTo(expected.kvartal)
        AssertUtils.assertBigDecimalIsEqual(actual.muligeDagsverk, expected.muligeDagsverk)
        AssertUtils.assertBigDecimalIsEqual(actual.tapteDagsverk, expected.tapteDagsverk)
    }


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

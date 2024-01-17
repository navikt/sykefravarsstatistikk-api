package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkForNæringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class EksporteringPerStatistikkKategoriServiceKtTest {

    @Test
    fun vi_grupperer_statistikk_lister_per_næringskode() {

        val dataset: List<SykefraværsstatistikkForNæringskode> = listOf(
            SykefraværsstatistikkForNæringskode(
                2023, 1, "12345", 5, BigDecimal(15.5), BigDecimal(15.5)
            )
        )

        val results = dataset.groupBy({ it.næringskode }, { it })
        assertThat(results).isEqualTo(
            mapOf(
                "12345" to listOf(
                    SykefraværsstatistikkForNæringskode(
                        2023, 1, "12345", 5, BigDecimal(15.5), BigDecimal(15.5)
                    )
                )
            )
        )
    }

    @Test
    fun vi_grupperer_statistikk_lister_per_næringskode_over_flere_kvartaler() {

        val statistikFor12345: List<SykefraværsstatistikkForNæringskode> = listOf(

            SykefraværsstatistikkForNæringskode(
                2023, 1, "12345", 5, BigDecimal(15.5), BigDecimal(15.5)
            ),
            SykefraværsstatistikkForNæringskode(
                2022, 4, "12345", 5, BigDecimal(15.5), BigDecimal(15.5)
            ),
            SykefraværsstatistikkForNæringskode(
                2022, 3, "12345", 5, BigDecimal(15.5), BigDecimal(15.5)
            ),
            SykefraværsstatistikkForNæringskode(
                2022, 2, "12345", 5, BigDecimal(15.5), BigDecimal(15.5)
            ),

            )
        val statistikFor67890 = listOf(
            SykefraværsstatistikkForNæringskode(
                2023, 1, "67890", 5, BigDecimal(15.5), BigDecimal(15.5)
            )
        )

        val dataset: List<SykefraværsstatistikkForNæringskode> = statistikFor12345 + statistikFor67890

        val results = dataset.groupBy({ it.næringskode }, { it })
        assertThat(results).isEqualTo(
            mapOf(
                "67890" to statistikFor67890,
                "12345" to statistikFor12345
            )
        )
    }

    @Test
    fun `vi beregner siste kvartal`() {

        val dataset: List<SykefraværsstatistikkForNæringskode> = listOf(
            SykefraværsstatistikkForNæringskode(
                2023, 1, "12345", 5, BigDecimal(15.5), BigDecimal(15.5)
            ),
            SykefraværsstatistikkForNæringskode(
                2022, 4, "12345", 5, BigDecimal(15.5), BigDecimal(15.5)
            ),
            SykefraværsstatistikkForNæringskode(
                2022, 3, "12345", 5, BigDecimal(15.5), BigDecimal(15.5)
            ),
            SykefraværsstatistikkForNæringskode(
                2022, 2, "12345", 5, BigDecimal(15.5), BigDecimal(15.5)
            )
        ).shuffled()


        val result = dataset.map { UmaskertSykefraværForEttKvartal(it) }
            .max()
            .tilSykefraværMedKategori(Statistikkategori.NÆRINGSKODE, "12345")

        assertThat(result.årstallOgKvartal).isEqualTo(ÅrstallOgKvartal(2023, 1))
    }
}

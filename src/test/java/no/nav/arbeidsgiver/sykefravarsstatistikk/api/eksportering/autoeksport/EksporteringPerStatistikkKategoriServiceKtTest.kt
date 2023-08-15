package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.SykefraværsstatistikkNæring5Siffer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.groupByNæringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.tilSykefraværMedKategoriSisteKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.tilUmaskertSykefraværForEttKvartal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class EksporteringPerStatistikkKategoriServiceKtTest {

    @Test
    fun vi_grupperer_statistikk_lister_per_næringskode() {

        val dataset: List<SykefraværsstatistikkNæring5Siffer> = listOf(
                SykefraværsstatistikkNæring5Siffer(
                        2023, 1, "12345", 5, BigDecimal(15.5), BigDecimal(15.5)
                )
        )

        val results = dataset.groupByNæringskode()
        assertThat(results).isEqualTo(
                mapOf("12345" to listOf(
                        SykefraværsstatistikkNæring5Siffer(
                                2023, 1, "12345", 5, BigDecimal(15.5), BigDecimal(15.5)
                        )
                ))
        )
    }

    @Test
    fun vi_grupperer_statistikk_lister_per_næringskode_over_flere_kvartaler() {

        val statistikFor12345: List<SykefraværsstatistikkNæring5Siffer> = listOf(

                SykefraværsstatistikkNæring5Siffer(
                        2023, 1, "12345", 5, BigDecimal(15.5), BigDecimal(15.5)
                ),
                SykefraværsstatistikkNæring5Siffer(
                        2022, 4, "12345", 5, BigDecimal(15.5), BigDecimal(15.5)
                ),
                SykefraværsstatistikkNæring5Siffer(
                        2022, 3, "12345", 5, BigDecimal(15.5), BigDecimal(15.5)
                ),
                SykefraværsstatistikkNæring5Siffer(
                        2022, 2, "12345", 5, BigDecimal(15.5), BigDecimal(15.5)
                ),

                )
        val statistikFor67890 = listOf(
                SykefraværsstatistikkNæring5Siffer(
                        2023, 1, "67890", 5, BigDecimal(15.5), BigDecimal(15.5)
                )
        )

        val dataset: List<SykefraværsstatistikkNæring5Siffer> = statistikFor12345 + statistikFor67890

        val results = dataset.groupByNæringskode()
        assertThat(results).isEqualTo(
                mapOf("67890" to statistikFor67890,
                        "12345" to statistikFor12345
                ))
    }

    @Test
    fun `vi beregner siste kvartal`() {

        val dataset: List<SykefraværsstatistikkNæring5Siffer> = listOf(
                SykefraværsstatistikkNæring5Siffer(
                        2023, 1, "12345", 5, BigDecimal(15.5), BigDecimal(15.5)
                ),
                SykefraværsstatistikkNæring5Siffer(
                        2022, 4, "12345", 5, BigDecimal(15.5), BigDecimal(15.5)
                ),
                SykefraværsstatistikkNæring5Siffer(
                        2022, 3, "12345", 5, BigDecimal(15.5), BigDecimal(15.5)
                ),
                SykefraværsstatistikkNæring5Siffer(
                        2022, 2, "12345", 5, BigDecimal(15.5), BigDecimal(15.5)
                )
        ).shuffled()


        val result = dataset
                .tilUmaskertSykefraværForEttKvartal()
                .tilSykefraværMedKategoriSisteKvartal(Statistikkategori.NÆRINGSKODE, "12345")

        assertThat(result.årstallOgKvartal).isEqualTo(ÅrstallOgKvartal(2023, 1))
    }
}

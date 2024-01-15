package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal.Companion.range
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class ÅrstallOgKvartalTest {
    @Test
    fun minusEtÅr_returnererSammeKvartalEttÅrSiden() {
        Assertions.assertThat(ÅrstallOgKvartal(2022, 1).minusEttÅr()).isEqualTo(ÅrstallOgKvartal(2021, 1))
        Assertions.assertThat(ÅrstallOgKvartal(2020, 2).minusEttÅr()).isEqualTo(ÅrstallOgKvartal(2019, 2))
    }

    @Test
    fun minusKvartaler__skal_retunere_rikitg() {
        Assertions.assertThat(ÅrstallOgKvartal(2019, 3).minusKvartaler(1))
            .isEqualTo(ÅrstallOgKvartal(2019, 2))
        Assertions.assertThat(ÅrstallOgKvartal(2019, 3).minusKvartaler(0))
            .isEqualTo(ÅrstallOgKvartal(2019, 3))
        Assertions.assertThat(ÅrstallOgKvartal(2019, 4).minusKvartaler(1))
            .isEqualTo(ÅrstallOgKvartal(2019, 3))
        Assertions.assertThat(ÅrstallOgKvartal(2019, 1).minusKvartaler(1))
            .isEqualTo(ÅrstallOgKvartal(2018, 4))
        Assertions.assertThat(ÅrstallOgKvartal(2019, 3).minusKvartaler(3))
            .isEqualTo(ÅrstallOgKvartal(2018, 4))
        Assertions.assertThat(ÅrstallOgKvartal(2019, 3).minusKvartaler(11))
            .isEqualTo(ÅrstallOgKvartal(2016, 4))
        Assertions.assertThat(ÅrstallOgKvartal(2019, 3).minusKvartaler(-1))
            .isEqualTo(ÅrstallOgKvartal(2019, 4))
        Assertions.assertThat(ÅrstallOgKvartal(2023, 2).minusKvartaler(20))
            .isEqualTo(ÅrstallOgKvartal(2018, 2))
    }

    @Test
    fun plussKvartaler__skal_retunere_rikitg() {
        Assertions.assertThat(ÅrstallOgKvartal(2019, 3).plussKvartaler(1))
            .isEqualTo(ÅrstallOgKvartal(2019, 4))
        Assertions.assertThat(ÅrstallOgKvartal(2019, 3).plussKvartaler(0))
            .isEqualTo(ÅrstallOgKvartal(2019, 3))
        Assertions.assertThat(ÅrstallOgKvartal(2019, 4).plussKvartaler(1))
            .isEqualTo(ÅrstallOgKvartal(2020, 1))
        Assertions.assertThat(ÅrstallOgKvartal(2019, 1).plussKvartaler(1))
            .isEqualTo(ÅrstallOgKvartal(2019, 2))
        Assertions.assertThat(ÅrstallOgKvartal(2019, 3).plussKvartaler(3))
            .isEqualTo(ÅrstallOgKvartal(2020, 2))
        Assertions.assertThat(ÅrstallOgKvartal(2019, 3).plussKvartaler(11))
            .isEqualTo(ÅrstallOgKvartal(2022, 2))
        Assertions.assertThat(ÅrstallOgKvartal(2019, 3).plussKvartaler(-1))
            .isEqualTo(ÅrstallOgKvartal(2019, 2))
    }

    @Test
    fun compareTo_sorter_ÅrstallOgKvartal_først_på_årstall_og_på_kvartal_etterpå() {
        Assertions.assertThat(ÅrstallOgKvartal(2019, 3).compareTo(ÅrstallOgKvartal(2019, 3))).isEqualTo(0)
        Assertions.assertThat(ÅrstallOgKvartal(2019, 3).compareTo(ÅrstallOgKvartal(2019, 2))).isEqualTo(1)
        Assertions.assertThat(ÅrstallOgKvartal(2019, 3).compareTo(ÅrstallOgKvartal(2017, 4))).isEqualTo(1)
        Assertions.assertThat(ÅrstallOgKvartal(2019, 3).compareTo(ÅrstallOgKvartal(2019, 4)))
            .isEqualTo(-1)
        Assertions.assertThat(ÅrstallOgKvartal(2019, 3).compareTo(ÅrstallOgKvartal(2020, 1)))
            .isEqualTo(-1)
    }

    @Test
    fun range__skal_returnere_alle_årstall_og_kvartal_mellom_angitt_input() {
        val liste = range(ÅrstallOgKvartal(2000, 1), ÅrstallOgKvartal(2002, 3))
        Assertions.assertThat(liste)
            .isEqualTo(
                listOf(
                    ÅrstallOgKvartal(2000, 1),
                    ÅrstallOgKvartal(2000, 2),
                    ÅrstallOgKvartal(2000, 3),
                    ÅrstallOgKvartal(2000, 4),
                    ÅrstallOgKvartal(2001, 1),
                    ÅrstallOgKvartal(2001, 2),
                    ÅrstallOgKvartal(2001, 3),
                    ÅrstallOgKvartal(2001, 4),
                    ÅrstallOgKvartal(2002, 1),
                    ÅrstallOgKvartal(2002, 2),
                    ÅrstallOgKvartal(2002, 3)
                )
            )
    }

    @Test
    fun `inkludertTidligere skal regne seg bakover n kvartaler`() {
        val kvartal = ÅrstallOgKvartal(2023, 1)
        kvartal inkludertTidligere -1 shouldBe emptyList()
        kvartal inkludertTidligere 0 shouldBe listOf(kvartal)
        kvartal inkludertTidligere 1 shouldBe listOf(kvartal, ÅrstallOgKvartal(2022, 4))

        ÅrstallOgKvartal(2040, 3) inkludertTidligere 2 shouldBe listOf(
            ÅrstallOgKvartal(2040, 3),
            ÅrstallOgKvartal(2040, 2),
            ÅrstallOgKvartal(2040, 1),
        )
    }

    @Test
    fun range__skal_returnere_tom_liste_hvis_til_er_før_fra() {
        val liste = range(ÅrstallOgKvartal(2004, 1), ÅrstallOgKvartal(2002, 1))
        Assertions.assertThat(liste).isEmpty()
    }

    @Test
    fun tilOgMed_skal_være_inklusiv() {
        val liste = ÅrstallOgKvartal(2019, 3) tilOgMed ÅrstallOgKvartal(2019, 4)
        Assertions.assertThat(liste).hasSize(2)
        Assertions.assertThat(liste.last()).isEqualTo(ÅrstallOgKvartal(2019, 4))
        Assertions.assertThat(liste.first()).isEqualTo(ÅrstallOgKvartal(2019, 3))
    }

    @Test
    fun tilOgMed_skal_inkludere_alle_kvartaler_mellom_fra_og_til() {
        val liste = ÅrstallOgKvartal(2019, 3) tilOgMed ÅrstallOgKvartal(2020, 2)
        Assertions.assertThat(liste).hasSize(4)
        Assertions.assertThat(liste).isEqualTo(
            listOf(
                ÅrstallOgKvartal(2019, 3),
                ÅrstallOgKvartal(2019, 4),
                ÅrstallOgKvartal(2020, 1),
                ÅrstallOgKvartal(2020, 2),
            )
        )
    }
}

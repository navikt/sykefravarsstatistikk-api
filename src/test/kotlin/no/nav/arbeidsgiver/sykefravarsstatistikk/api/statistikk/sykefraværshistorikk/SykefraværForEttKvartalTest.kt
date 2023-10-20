package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.etÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.SykefraværFlereKvartalerForEksport
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.SykefraværMedKategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.SykefraværMedKategori.Companion.utenStatistikk
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.UmaskertSykefraværForEttKvartal
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class SykefraværForEttKvartalTest {
    @Test
    fun sykefraværprosent__uten_data_er_ikke_maskert() {
        Assertions.assertThat(SykefraværForEttKvartal(etÅrstallOgKvartal(), null, null, 0).erMaskert)
            .isFalse()
    }

    @Test
    fun sykefraværprosent__equals_test() {
        val sykefravær = SykefraværForEttKvartal(
            etÅrstallOgKvartal(), BigDecimal(5), BigDecimal(10), 20
        )
        Assertions.assertThat(
            sykefravær == SykefraværForEttKvartal(
                etÅrstallOgKvartal(), BigDecimal(5), BigDecimal(10), 20
            )
        )
            .isEqualTo(true)
        Assertions.assertThat(
            SykefraværForEttKvartal(etÅrstallOgKvartal(), null, null, 4) == sykefravær
        )
            .isEqualTo(false)
        Assertions.assertThat(sykefravær == SykefraværForEttKvartal(etÅrstallOgKvartal(), null, null, 4))
            .isFalse()
    }

    @Test
    fun sykefraværFlereKvartalerForEksport_equals_test() {
        Assertions.assertThat(
            SykefraværFlereKvartalerForEksport(
                listOf(
                    UmaskertSykefraværForEttKvartal(
                        etÅrstallOgKvartal(), BigDecimal(10), BigDecimal(100), 6
                    ),
                    UmaskertSykefraværForEttKvartal(
                        etÅrstallOgKvartal().minusKvartaler(1),
                        BigDecimal(12),
                        BigDecimal(100),
                        6
                    )
                )
            ) == SykefraværFlereKvartalerForEksport(listOf())
        )
            .isFalse()
    }

    @Test
    fun sykefraværMedKategori_equals_test() {
        Assertions.assertThat(
            SykefraværMedKategori(
                Statistikkategori.VIRKSOMHET,
                "987654321",
                etÅrstallOgKvartal(),
                BigDecimal(10),
                BigDecimal(100),
                6
            ) == utenStatistikk(
                Statistikkategori.VIRKSOMHET, "987654321", etÅrstallOgKvartal()
            )
        )
            .isFalse()
        Assertions.assertThat(
            SykefraværMedKategori(
                Statistikkategori.VIRKSOMHET,
                "987654321",
                etÅrstallOgKvartal(),
                BigDecimal(2),
                null,
                0
            ) == utenStatistikk(
                Statistikkategori.VIRKSOMHET, "987654321", etÅrstallOgKvartal()
            )
        )
            .isTrue()
        Assertions.assertThat(
            utenStatistikk(
                Statistikkategori.VIRKSOMHET, "987654321", etÅrstallOgKvartal()
            ) == utenStatistikk(
                Statistikkategori.VIRKSOMHET, "987654321", etÅrstallOgKvartal()
            )
        )
            .isTrue()
    }

    @Test
    fun sykefraværprosent__skal_regne_ut_riktig_prosent_ut_i_fra_tapte_og_mulige_dagsverk() {
        val sykefravær = SykefraværForEttKvartal(
            etÅrstallOgKvartal(), BigDecimal(5), BigDecimal(10), 20
        )
        Assertions.assertThat(sykefravær.prosent).isEqualTo(BigDecimal("50.0"))
    }

    @Test
    fun sykefraværprosent__skal_runde_prosenten_opp_ved_tvil() {
        val sykefravær = SykefraværForEttKvartal(
            etÅrstallOgKvartal(), BigDecimal(455), BigDecimal(10000), 100
        )
        Assertions.assertThat(sykefravær.prosent).isEqualTo(BigDecimal("4.6"))
    }

    @Test
    fun sykefraværprosent__skal_være_maskert_hvis_antallPersoner_er_4_eller_under() {
        val sykefravær = SykefraværForEttKvartal(etÅrstallOgKvartal(), BigDecimal(1), BigDecimal(10), 4)
        Assertions.assertThat(sykefravær.erMaskert).isTrue()
        Assertions.assertThat(sykefravær.prosent).isNull()
        Assertions.assertThat(sykefravær.tapteDagsverk).isNull()
        Assertions.assertThat(sykefravær.muligeDagsverk).isNull()
    }

    @Test
    fun sykefraværprosent__skal_være_maskert_hvis_antallPersoner_over_4() {
        val sykefravær = SykefraværForEttKvartal(etÅrstallOgKvartal(), BigDecimal(1), BigDecimal(10), 5)
        Assertions.assertThat(sykefravær.erMaskert).isFalse()
        Assertions.assertThat(sykefravær.prosent).isNotNull()
        Assertions.assertThat(sykefravær.tapteDagsverk).isNotNull()
        Assertions.assertThat(sykefravær.muligeDagsverk).isNotNull()
    }

    @Test
    fun sykefraværprosent__skal_bare_inkludere_relevante_felt_i_json_konvertering() {
        val mapper = ObjectMapper()
        val sykefravær = SykefraværForEttKvartal(
            etÅrstallOgKvartal(), BigDecimal(5), BigDecimal(10), 20
        )
        val json = mapper.readTree(mapper.writeValueAsString(sykefravær))
        val ønsketJson = mapper.readTree(
            "{"
                    + "    \"årstall\": 2019,"
                    + "    \"kvartal\": 4,"
                    + "    \"prosent\": 50.0,"
                    + "    \"tapteDagsverk\": 5.0,"
                    + "    \"muligeDagsverk\": 10.0,"
                    + "    \"erMaskert\": false"
                    + "}"
        )
        Assertions.assertThat(json).isEqualTo(ønsketJson)
    }
}

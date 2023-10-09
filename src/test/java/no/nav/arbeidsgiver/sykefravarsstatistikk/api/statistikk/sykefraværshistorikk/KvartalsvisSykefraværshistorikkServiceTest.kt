package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.enNæringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.etOrgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregering.KvartalsvisSykefraværshistorikkService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.KvartalsvisSykefraværRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class KvartalsvisSykefraværshistorikkServiceTest {
    @Mock
    private val kvartalsvisSykefraværprosentRepository: KvartalsvisSykefraværRepository? = null
    var kvartalsvisSykefraværshistorikkService: KvartalsvisSykefraværshistorikkService? = null

    @BeforeEach
    fun setUp() {
        kvartalsvisSykefraværshistorikkService = KvartalsvisSykefraværshistorikkService(
            kvartalsvisSykefraværprosentRepository!!
        )
        whenever(kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentLand())
            .thenReturn(listOf(sykefraværprosent()))
        whenever(kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentSektor(ArgumentMatchers.any()))
            .thenReturn(listOf(sykefraværprosent()))
        whenever(
            kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentVirksomhet(
                ArgumentMatchers.any()
            )
        )
            .thenReturn(listOf(sykefraværprosent()))
    }

    @Test
    fun hentSykefraværshistorikk__skal_returnere_en_næring_dersom_virksomhet_er_i_bransjeprogram_på_2_siffer_nivå() {
        val underenhet = UnderenhetLegacy(etOrgnr(), etOrgnr(), "Underenhet AS", enNæringskode("10300"), 40)
        val kvartalsvisSykefraværshistorikk: List<KvartalsvisSykefraværshistorikk> =
            kvartalsvisSykefraværshistorikkService!!.hentSykefraværshistorikk(
                underenhet, Sektor.PRIVAT
            )
        assertThatHistorikkHarKategori(kvartalsvisSykefraværshistorikk, Statistikkategori.NÆRING, true)
        assertThatHistorikkHarKategori(kvartalsvisSykefraværshistorikk, Statistikkategori.LAND, true)
        assertThatHistorikkHarKategori(kvartalsvisSykefraværshistorikk, Statistikkategori.SEKTOR, true)
        assertThatHistorikkHarKategori(
            kvartalsvisSykefraværshistorikk, Statistikkategori.VIRKSOMHET, true
        )
        assertThatHistorikkHarKategori(
            kvartalsvisSykefraværshistorikk, Statistikkategori.BRANSJE, false
        )
    }

    companion object {
        private fun assertThatHistorikkHarKategori(
            kvartalsvisSykefraværshistorikk: List<KvartalsvisSykefraværshistorikk>,
            statistikkategori: Statistikkategori,
            expected: Boolean
        ) {
            Assertions.assertThat(
                kvartalsvisSykefraværshistorikk.stream()
                    .anyMatch { (type): KvartalsvisSykefraværshistorikk -> type == statistikkategori })
                .isEqualTo(expected)
        }

        private fun sykefraværprosent(): SykefraværForEttKvartal {
            return SykefraværForEttKvartal(
                ÅrstallOgKvartal(2019, 1), BigDecimal(50), BigDecimal(100), 10
            )
        }
    }
}

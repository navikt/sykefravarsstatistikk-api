package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk

import io.mockk.every
import io.mockk.mockk
import testUtils.TestData.enNæringskode
import testUtils.TestData.etOrgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.UnderenhetLegacy
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.KvartalsvisSykefraværRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværStatistikkLandRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class KvartalsvisSykefraværshistorikkServiceTest {
    private val kvartalsvisSykefraværprosentRepository: KvartalsvisSykefraværRepository = mockk()
    private val sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository = mockk()
    private val kvartalsvisSykefraværshistorikkService: KvartalsvisSykefraværshistorikkService =
        KvartalsvisSykefraværshistorikkService(
            kvartalsvisSykefraværprosentRepository,
            sykefraværStatistikkLandRepository
        )


    @BeforeEach
    fun setUp() {
        every { sykefraværStatistikkLandRepository.hentAlt() } returns listOf(sykefraværprosent())
        every { kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentSektor(any()) } returns listOf(
            sykefraværprosent()
        )
        every { kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentVirksomhet(any()) } returns listOf(
            sykefraværprosent()
        )
    }

    @Test
    fun hentSykefraværshistorikk__skal_returnere_en_næring_dersom_virksomhet_er_i_bransjeprogram_på_2_siffer_nivå() {
        val underenhet = UnderenhetLegacy(etOrgnr(), etOrgnr(), "Underenhet AS", enNæringskode("10300"), 40)
        val kvartalsvisSykefraværshistorikk: List<KvartalsvisSykefraværshistorikkJson> =
            kvartalsvisSykefraværshistorikkService.hentSykefraværshistorikk(
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
            kvartalsvisSykefraværshistorikkJson: List<KvartalsvisSykefraværshistorikkJson>,
            statistikkategori: Statistikkategori,
            expected: Boolean
        ) {
            Assertions.assertThat(
                kvartalsvisSykefraværshistorikkJson.stream()
                    .anyMatch { (type): KvartalsvisSykefraværshistorikkJson -> type == statistikkategori })
                .isEqualTo(expected)
        }

        private fun sykefraværprosent(): SykefraværForEttKvartal {
            return SykefraværForEttKvartal(
                ÅrstallOgKvartal(2019, 1), BigDecimal(50), BigDecimal(100), 10
            )
        }
    }
}

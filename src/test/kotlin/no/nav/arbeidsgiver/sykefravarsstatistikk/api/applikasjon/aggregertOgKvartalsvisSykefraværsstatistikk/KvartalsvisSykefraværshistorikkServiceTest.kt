package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk

import io.mockk.every
import io.mockk.mockk
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.*
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class KvartalsvisSykefraværshistorikkServiceTest {
    private val sykefravarStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository = mockk()
    private val sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository = mockk()
    private val sykefraværStatistikkSektorRepository = mockk<SykefraværStatistikkSektorRepository>()
    private val sykefraværStatistikkNæringRepository = mockk<SykefraværStatistikkNæringRepository>()
    private val sykefraværStatistikkNæringskodeRepository = mockk<SykefraværStatistikkNæringskodeRepository>()

    private val kvartalsvisSykefraværshistorikkService: KvartalsvisSykefraværshistorikkService =
        KvartalsvisSykefraværshistorikkService(
            sykefraværStatistikkVirksomhetRepository = sykefravarStatistikkVirksomhetRepository,
            sykefraværStatistikkLandRepository = sykefraværStatistikkLandRepository,
            sykefraværStatistikkSektorRepository = sykefraværStatistikkSektorRepository,
            sykefraværStatistikkNæringRepository = sykefraværStatistikkNæringRepository,
            sykefraværStatistikkNæringskodeRepository = sykefraværStatistikkNæringskodeRepository
        )

    val ET_ORGNR = Orgnr("971800534")


    @BeforeEach
    fun setUp() {
        every { sykefraværStatistikkLandRepository.hentAlt() } returns listOf(sykefraværprosent())
        every { sykefraværStatistikkSektorRepository.hentKvartalsvisSykefraværprosent(any<Sektor>()) } returns listOf(
            sykefraværprosent()
        )
        every { sykefravarStatistikkVirksomhetRepository.hentAlt(any()) } returns listOf(sykefraværprosent())
    }

    @Test
    fun hentSykefraværshistorikk__skal_returnere_en_næring_dersom_virksomhet_er_i_bransjeprogram_på_2_siffer_nivå() {
        val underenhet = Underenhet.Næringsdrivende(
            orgnr = ET_ORGNR,
            overordnetEnhetOrgnr = ET_ORGNR,
            navn = "Underenhet AS",
            næringskode = Næringskode("10300"),
            antallAnsatte = 40
        )
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

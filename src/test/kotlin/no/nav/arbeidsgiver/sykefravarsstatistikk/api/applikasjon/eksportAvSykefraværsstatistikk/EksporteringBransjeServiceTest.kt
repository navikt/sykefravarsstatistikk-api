package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk

import ia.felles.definisjoner.bransjer.Bransjer
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.SykefraværFlereKvartalerForEksport
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.SykefraværMedKategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.KafkaTopic
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværStatistikkLandRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværStatistikkNæringRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværStatistikkNæringskodeRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.KafkaClient
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto.StatistikkategoriKafkamelding
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class EksporteringBransjeServiceTest {

    private val kafkaClientMock = mockk<KafkaClient>(relaxed = true)
    private val sykefraværStatistikkLandRepositoryMock = mockk<SykefraværStatistikkLandRepository>()

    private val sykefraværStatistikkNæringRepository = mockk<SykefraværStatistikkNæringRepository>()
    private val sykefraværStatistikkNæringskodeRepository = mockk<SykefraværStatistikkNæringskodeRepository>()

    private val service =
        EksporteringPerStatistikkKategoriService(
            sykefraværStatistikkLandRepository = sykefraværStatistikkLandRepositoryMock,
            sykefraværStatistikkSektorRepository = mockk(),
            sykefravarStatistikkVirksomhetRepository = mockk(),
            sykefravarStatistikkVirksomhetGraderingRepository = mockk(),
            sykefraværStatistikkNæringRepository = sykefraværStatistikkNæringRepository,
            sykefraværStatistikkNæringskodeRepository = sykefraværStatistikkNæringskodeRepository,
            kafkaClient = kafkaClientMock,
        )

    @Test
    fun `eksporterPerStatistikkKategori skal ikke putte noe på kafkastrømmen dersom datagrunnalget er tomt`() {
        every { sykefraværStatistikkNæringRepository.hentForAlleNæringer(any()) } returns emptyList()
        every { sykefraværStatistikkNæringskodeRepository.hentAltForKvartaler(any()) } returns emptyList()

        service.eksporterPerStatistikkKategori(
            ÅrstallOgKvartal(2023, 1),
            Statistikkategori.BRANSJE,
        )

        verify(exactly = 0) { kafkaClientMock.sendMelding(any(), any()) }
    }

    @Test
    fun `eksporterPerStatistikkKategori skal putte riktige kvartaler på topic`() {
        every { sykefraværStatistikkNæringskodeRepository.hentAltForKvartaler(any()) } returns emptyList()
        every { sykefraværStatistikkNæringRepository.hentForAlleNæringer(any()) } returns listOf(
            SykefraværsstatistikkForNæring(
                årstall = 1990,
                kvartal = 1,
                næringkode = Bransjer.ANLEGG.næringskoder.first(),
                antallPersoner = 1,
                tapteDagsverk = BigDecimal.ONE,
                muligeDagsverk = BigDecimal.ONE
            ),
            SykefraværsstatistikkForNæring(
                årstall = 2023,
                kvartal = 1,
                næringkode = Bransjer.ANLEGG.næringskoder.first(),
                antallPersoner = 1,
                tapteDagsverk = BigDecimal.ONE,
                muligeDagsverk = BigDecimal.ONE
            )
        )

        service.eksporterPerStatistikkKategori(
            ÅrstallOgKvartal(2023, 1),
            Statistikkategori.BRANSJE,
        )

        verify(exactly = 1) { kafkaClientMock.sendMelding(any(), any()) }
    }

    @Test
    fun `eksporterPerStatistikkKategori skal ikke sende melding når forespurt kvartal ikke finnes i databasen`() {
        every { sykefraværStatistikkNæringskodeRepository.hentAltForKvartaler(any()) } returns emptyList()
        every { sykefraværStatistikkNæringRepository.hentForAlleNæringer(any()) } returns listOf(
            SykefraværsstatistikkForNæring(
                årstall = 1990,
                kvartal = 1,
                næringkode = Bransjer.ANLEGG.næringskoder.first(),
                antallPersoner = 1,
                tapteDagsverk = BigDecimal.ONE,
                muligeDagsverk = BigDecimal.ONE
            )
        )

        verify(exactly = 0) { kafkaClientMock.sendMelding(any(), any()) }
    }


    @Test
    fun `eksporterPerStatistikkKategori skal putte bransjetall på topic`() {
        every { sykefraværStatistikkNæringskodeRepository.hentAltForKvartaler(any()) } returns emptyList()
        every { sykefraværStatistikkNæringRepository.hentForAlleNæringer(any()) } returns listOf(
            SykefraværsstatistikkForNæring(
                årstall = 2023,
                kvartal = 2,
                næringkode = Bransjer.ANLEGG.næringskoder.first(),
                antallPersoner = 5,
                tapteDagsverk = BigDecimal.ONE,
                muligeDagsverk = BigDecimal.ONE
            )
        )

        service.eksporterPerStatistikkKategori(
            ÅrstallOgKvartal(2023, 2),
            Statistikkategori.BRANSJE,
        )

        val melding = StatistikkategoriKafkamelding(
            SykefraværMedKategori(
                Statistikkategori.BRANSJE,
                Bransjer.ANLEGG.name,
                2023,
                2,
                BigDecimal.ONE,
                BigDecimal.ONE,
                5
            ),
            SykefraværFlereKvartalerForEksport(
                listOf(
                    UmaskertSykefraværForEttKvartal(
                        SykefraværsstatistikkBransje(
                            årstall = 2023,
                            kvartal = 2,
                            bransje = Bransjer.ANLEGG,
                            antallPersoner = 5,
                            tapteDagsverk = BigDecimal.ONE,
                            muligeDagsverk = BigDecimal.ONE
                        )
                    )
                )
            )
        )

        verify { kafkaClientMock.sendMelding(melding, KafkaTopic.SYKEFRAVARSSTATISTIKK_BRANSJE_V1) }
        confirmVerified(kafkaClientMock)
    }
}

package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhetUtenVarighet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefravarStatistikkVirksomhetRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.KafkaClient
import org.junit.jupiter.api.Test

class EksporteringAvVirksomhetServiceTest {

    val sykefravarStatistikkVirksomhetRepository = mockk<SykefravarStatistikkVirksomhetRepository>()
    val kafkaClient = mockk<KafkaClient>(relaxed = true)
    private val service = EksporteringPerStatistikkKategoriService(
        sykefraværStatistikkLandRepository = mockk(),
        sykefraværStatistikkSektorRepository = mockk(),
        sykefravarStatistikkVirksomhetRepository = sykefravarStatistikkVirksomhetRepository,
        sykefravarStatistikkVirksomhetGraderingRepository = mockk(),
        sykefraværStatistikkNæringRepository = mockk(),
        sykefraværStatistikkNæringskodeRepository = mockk(),
        kafkaClient = kafkaClient,
    )

    @Test
    fun `eksport av virksomhetsdata burde ikke returnere tidlig hvis vi får en bedrift som ikke har statistikk for forespurte kvartal`() {
        every { sykefravarStatistikkVirksomhetRepository.hentSykefraværAlleVirksomheter(any()) } returns listOf(
            SykefraværsstatistikkVirksomhetUtenVarighet(
                årstall = 2022,
                kvartal = 4,
                orgnr = "1234",
                antallPersoner = 5,
                tapteDagsverk = 50.toBigDecimal(),
                muligeDagsverk = 100.toBigDecimal()
            ), SykefraværsstatistikkVirksomhetUtenVarighet(
                årstall = 2023,
                kvartal = 1,
                orgnr = "666",
                antallPersoner = 5,
                tapteDagsverk = 50.toBigDecimal(),
                muligeDagsverk = 100.toBigDecimal()
            )
        )
        val årstallOgKvartal = ÅrstallOgKvartal(2023, 1)
        service.eksporterPerStatistikkKategori(årstallOgKvartal, Statistikkategori.VIRKSOMHET)

        verify(exactly = 1) { kafkaClient.sendMelding(any(), any()) }
    }
}
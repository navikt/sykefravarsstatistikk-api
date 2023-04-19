package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.SykefraværsstatistikkTilEksporteringRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.ArbeidsmiljøportalenBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram.ANLEGG
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.math.BigDecimal

internal class EksporteringBransjeServiceTest {

    private val repositoryMock = mock<SykefraværsstatistikkTilEksporteringRepository>()
    private val kafkaServiceMock = mock<KafkaService>()

    private val service =
        EksporteringPerStatistikkKategoriService(mock(), repositoryMock, mock(), kafkaServiceMock, true)

    @Test
    fun `eksporterPerStatistikkKategori skal ikke putte noe på kafkastrømmen dersom datagrunnalget er tomt`() {
        val antallEksportert = service.eksporterPerStatistikkKategori(
            ÅrstallOgKvartal(2023, 1),
            Statistikkategori.BRANSJE,
            EksporteringBegrensning.build().utenBegrensning()
        )

        assertThat(antallEksportert).isEqualTo(0)
        verify(kafkaServiceMock, never()).sendTilStatistikkKategoriTopic(any(), any(), any(), any(), any())
    }

    @Test
    fun `eksporterPerStatistikkKategori skal putte riktige kvartaler på topic`() {
        whenever(repositoryMock.hentSykefraværAlleBransjerFraOgMed(any()))
            .thenReturn(
                listOf(
                    SykefraværsstatistikkBransje(
                        årstall = 1990,
                        kvartal = 1,
                        bransje = ArbeidsmiljøportalenBransje.ANLEGG,
                        antallPersoner = 1,
                        tapteDagsverk = BigDecimal.ONE,
                        muligeDagsverk = BigDecimal.ONE
                    ), SykefraværsstatistikkBransje(
                        årstall = 2023,
                        kvartal = 1,
                        bransje = ArbeidsmiljøportalenBransje.ANLEGG,
                        antallPersoner = 1,
                        tapteDagsverk = BigDecimal.ONE,
                        muligeDagsverk = BigDecimal.ONE
                    )
                )
            )

        whenever(kafkaServiceMock.sendTilStatistikkKategoriTopic(any(), any(), any(), any(), any()))
            .thenReturn(true)

        val result = service.eksporterPerStatistikkKategori(
            ÅrstallOgKvartal(2023, 1),
            Statistikkategori.BRANSJE,
            EksporteringBegrensning.build().utenBegrensning()
        )

        assertThat(result).isEqualTo(1)
    }

    @Test
    fun `eksporterPerStatistikkKategori skal kaste feil når forespurt kvartal ikke finnes i databasen`() {
        whenever(repositoryMock.hentSykefraværAlleBransjerFraOgMed(any())).thenReturn(
            listOf(
                SykefraværsstatistikkBransje(
                    årstall = 1990,
                    kvartal = 1,
                    bransje = ArbeidsmiljøportalenBransje.ANLEGG,
                    antallPersoner = 1,
                    tapteDagsverk = BigDecimal.ONE,
                    muligeDagsverk = BigDecimal.ONE
                )
            )
        )

        val exception = assertThrows<RuntimeException> {
            service.eksporterPerStatistikkKategori(
                ÅrstallOgKvartal(2023, 2),
                Statistikkategori.BRANSJE,
                EksporteringBegrensning.build().utenBegrensning()
            )
        }
        assertThat(exception.message).isEqualTo("Siste kvartal i dataene er ikke lik forespurt kvartal")
    }


    @Test
    fun `eksporterPerStatistikkKategori skal putte bransjetall på topic`() {
        val bransjestatistikk = SykefraværsstatistikkBransje(
            årstall = 2023,
            kvartal = 2,
            bransje = ArbeidsmiljøportalenBransje.ANLEGG,
            antallPersoner = 5,
            tapteDagsverk = BigDecimal.ONE,
            muligeDagsverk = BigDecimal.ONE
        )
        val sykefraværsstatistikk = listOf(bransjestatistikk)

        whenever(repositoryMock.hentSykefraværAlleBransjerFraOgMed(any()))
            .thenReturn(sykefraværsstatistikk)

        whenever(
            kafkaServiceMock.sendTilStatistikkKategoriTopic(any(), any(), any(), any(), any())
        ).thenReturn(true)

        service.eksporterPerStatistikkKategori(
            ÅrstallOgKvartal(2023, 2),
            Statistikkategori.BRANSJE,
            EksporteringBegrensning.build().utenBegrensning()
        )

        verify(kafkaServiceMock).sendTilStatistikkKategoriTopic(
            any(),
            any(),
            any(),
            eq(
                SykefraværMedKategori(
                    Statistikkategori.BRANSJE,
                    ANLEGG.type.name,
                    ÅrstallOgKvartal(2023, 2),
                    BigDecimal.ONE,
                    BigDecimal.ONE,
                    5,
                )
            ),
            eq(SykefraværFlereKvartalerForEksport(listOf(UmaskertSykefraværForEttKvartal(bransjestatistikk))))
        )
    }
}
package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringPerStatistikkKategoriService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.SykefraværFlereKvartalerForEksport
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.SykefraværsstatistikkBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværsstatistikkTilEksporteringRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.bransjeprogram.ArbeidsmiljøportalenBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.KafkaTopic
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto.StatistikkategoriKafkamelding
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
        EksporteringPerStatistikkKategoriService(mock(), repositoryMock, kafkaServiceMock, true)

    @Test
    fun `eksporterPerStatistikkKategori skal ikke putte noe på kafkastrømmen dersom datagrunnalget er tomt`() {
        service.eksporterPerStatistikkKategori(
            ÅrstallOgKvartal(2023, 1),
            Statistikkategori.BRANSJE,
        )

        verify(kafkaServiceMock, never()).sendMelding(any(), any())
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

        service.eksporterPerStatistikkKategori(
            ÅrstallOgKvartal(2023, 1),
            Statistikkategori.BRANSJE,
        )

        verify(kafkaServiceMock, times(1)).sendMelding(any(), any())
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
            )
        }
        assertThat(exception.message).isEqualTo("Stopper eksport av kategori BRANSJE pga: 'Siste kvartal i dataene '1990-1' er ikke lik forespurt kvartal '2023-2'. Kategori er 'BRANSJE' og kode er 'ANLEGG''")
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

        service.eksporterPerStatistikkKategori(
            ÅrstallOgKvartal(2023, 2),
            Statistikkategori.BRANSJE,
        )

        val melding = StatistikkategoriKafkamelding(
            SykefraværMedKategori(
                Statistikkategori.BRANSJE,
                ArbeidsmiljøportalenBransje.ANLEGG.name,
                2023,
                2,
                BigDecimal.ONE,
                BigDecimal.ONE,
                5
            ),
            SykefraværFlereKvartalerForEksport(
                listOf(
                    UmaskertSykefraværForEttKvartal(bransjestatistikk)
                )
            )
        )


        verify(kafkaServiceMock).sendMelding(
            eq(melding),
            eq(KafkaTopic.SYKEFRAVARSSTATISTIKK_BRANSJE_V1),
        )
    }
}

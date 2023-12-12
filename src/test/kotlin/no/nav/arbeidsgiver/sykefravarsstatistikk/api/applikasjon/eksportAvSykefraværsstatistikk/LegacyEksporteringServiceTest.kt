package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.LegacyEksporteringService.LegacyEksportFeil
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.LegacyEksporteringTestUtils.__2020_2
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.LegacyEksporteringTestUtils.assertEqualsSykefraværMedKategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.LegacyEksporteringTestUtils.assertEqualsVirksomhetSykefravær
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.LegacyEksporteringTestUtils.landSykefravær
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.LegacyEksporteringTestUtils.næring5SifferSykefravær
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.LegacyEksporteringTestUtils.næring5SifferSykefraværTilhørerBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.LegacyEksporteringTestUtils.næringSykefravær
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.LegacyEksporteringTestUtils.sektorSykefravær
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.LegacyEksporteringTestUtils.sykefraværsstatistikkLandSiste4Kvartaler
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.LegacyEksporteringTestUtils.sykefraværsstatistikkNæring
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.LegacyEksporteringTestUtils.sykefraværsstatistikkNæring5SifferBransjeprogram
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.LegacyEksporteringTestUtils.sykefraværsstatistikkSektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.LegacyEksporteringTestUtils.sykefraværsstatistikkVirksomhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.LegacyEksporteringTestUtils.virksomhet1_TilHørerBransjeMetadata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.LegacyEksporteringTestUtils.virksomhetEksportPerKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.LegacyEksporteringTestUtils.virksomhetMetadata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.LegacyEksporteringTestUtils.virksomhetSykefravær
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.SykefraværMedKategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.VirksomhetSykefravær
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Næringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkForNæringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.KafkaClient
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class LegacyEksporteringServiceTest {
    private val legacyEksporteringRepository: LegacyEksporteringRepository = mock()

    private val kafkaClient: KafkaClient = mock()
    private val sykefraværStatistikkLandRepository = mock<SykefraværStatistikkLandRepository>()
    private val sykefraværStatistikkSektorRepository = mock<SykefraværStatistikkSektorRepository>()

    private val sykefravarStatistikkVirksomhetRepository = mock<SykefravarStatistikkVirksomhetRepository>()
    private val sykefraværStatistikkNæringRepository = mock<SykefraværStatistikkNæringRepository>()
    private val sykefraværStatistikkNæringskodeRepository = mock<SykefraværStatistikkNæringskodeRepository>()
    private val legacyVirksomhetMetadataRepository = mock<LegacyVirksomhetMetadataRepository>()

    private val service = LegacyEksporteringService(
        legacyEksporteringRepository,
        sykefraværStatistikkLandRepository,
        sykefraværStatistikkSektorRepository,
        kafkaClient,
        sykefravarStatistikkVirksomhetRepository,
        sykefraværStatistikkNæringRepository,
        sykefraværStatistikkNæringskodeRepository,
        legacyVirksomhetMetadataRepository
    )

    val årstallOgKvartalArgumentCaptor: KArgumentCaptor<ÅrstallOgKvartal> = argumentCaptor<ÅrstallOgKvartal>()

    val virksomhetSykefraværArgumentCaptor: KArgumentCaptor<VirksomhetSykefravær> =
        argumentCaptor<VirksomhetSykefravær>()

    val næring5SifferSykefraværArgumentCaptor: KArgumentCaptor<List<SykefraværMedKategori>> =
        argumentCaptor<List<SykefraværMedKategori>>()

    val næringSykefraværArgumentCaptor: KArgumentCaptor<SykefraværMedKategori> = argumentCaptor<SykefraværMedKategori>()

    val sektorSykefraværArgumentCaptor: KArgumentCaptor<SykefraværMedKategori> = argumentCaptor<SykefraværMedKategori>()

    val landSykefraværArgumentCaptor: KArgumentCaptor<SykefraværMedKategori> = argumentCaptor<SykefraværMedKategori>()

    @Test
    fun eksporter_returnerer_feil_når_det_ikke_finnes_statistikk() {
        whenever(
            legacyEksporteringRepository.hentVirksomhetEksportPerKvartal(
                __2020_2
            )
        ).thenReturn(emptyList())
        val antallEksporterte = service.legacyEksporter(__2020_2).swap().getOrNull()
        Assertions.assertThat(antallEksporterte).isEqualTo(LegacyEksportFeil.IngenNyStatistikk)
    }

    @Test
    fun eksporter_sender_riktig_melding_til_kafka_og_returnerer_antall_meldinger_sendt() {
        whenever(
            legacyEksporteringRepository.hentVirksomhetEksportPerKvartal(
                __2020_2
            )
        ).thenReturn(listOf(virksomhetEksportPerKvartal))
        virksomhetMetadata.leggTilNæringOgNæringskode5siffer(
            listOf(
                Næringskode("11000"), Næringskode("85000")
            )
        )
        val fraÅrstallOgKvartal: ÅrstallOgKvartal = __2020_2.minusKvartaler(3)
        whenever(
            legacyVirksomhetMetadataRepository.hentVirksomhetMetadataMedNæringskoder(
                __2020_2
            )
        ).thenReturn(listOf(virksomhetMetadata))
        whenever(
            sykefraværStatistikkSektorRepository.hentForKvartaler(any())
        ).thenReturn(listOf(sykefraværsstatistikkSektor))
        whenever(
            sykefraværStatistikkNæringRepository.hentForAlleNæringer(any())
        ).thenReturn(
            listOf(
                sykefraværsstatistikkNæring(fraÅrstallOgKvartal),
                sykefraværsstatistikkNæring(fraÅrstallOgKvartal.plussKvartaler(1)),
                sykefraværsstatistikkNæring(fraÅrstallOgKvartal.plussKvartaler(2)),
                sykefraværsstatistikkNæring(fraÅrstallOgKvartal.plussKvartaler(3))
            )
        )
        whenever(
            sykefraværStatistikkNæringskodeRepository.hentAltForKvartaler(any())
        ).thenReturn(
            listOf(
                SykefraværsstatistikkForNæringskode(
                    årstall = __2020_2.årstall,
                    kvartal = __2020_2.kvartal,
                    næringskode = "11000",
                    antallPersoner = 1250,
                    tapteDagsverk = BigDecimal(40),
                    muligeDagsverk = BigDecimal(4000)
                )
            ))
        whenever(
            sykefravarStatistikkVirksomhetRepository.hentSykefraværAlleVirksomheter(listOf(__2020_2))
        ).thenReturn(
            listOf(
                sykefraværsstatistikkVirksomhet(fraÅrstallOgKvartal, "987654321"),
                sykefraværsstatistikkVirksomhet(fraÅrstallOgKvartal.plussKvartaler(1), "987654321"),
                sykefraværsstatistikkVirksomhet(fraÅrstallOgKvartal.plussKvartaler(2), "987654321"),
                sykefraværsstatistikkVirksomhet(
                    fraÅrstallOgKvartal.plussKvartaler(3), "987654321"
                )
            )
        )
        whenever(
            sykefraværStatistikkLandRepository.hentForKvartaler(any())
        ).thenReturn(sykefraværsstatistikkLandSiste4Kvartaler(__2020_2))
        val antallEksporterte = service.legacyEksporter(__2020_2).getOrNull()!!
        verify(kafkaClient)?.send(
            årstallOgKvartalArgumentCaptor.capture(),
            virksomhetSykefraværArgumentCaptor.capture(),
            næring5SifferSykefraværArgumentCaptor.capture(),
            næringSykefraværArgumentCaptor.capture(),
            sektorSykefraværArgumentCaptor.capture(),
            landSykefraværArgumentCaptor.capture()
        )
        Assertions.assertThat(årstallOgKvartalArgumentCaptor.firstValue).isEqualTo(__2020_2)
        assertEqualsVirksomhetSykefravær(
            virksomhetSykefravær, virksomhetSykefraværArgumentCaptor.firstValue
        )
        Assertions.assertThat(næring5SifferSykefraværArgumentCaptor.firstValue.size).isEqualTo(1)
        assertEqualsSykefraværMedKategori(
            næring5SifferSykefraværArgumentCaptor.firstValue[0], næring5SifferSykefravær
        )
        assertEqualsSykefraværMedKategori(næringSykefravær, næringSykefraværArgumentCaptor.firstValue)
        assertEqualsSykefraværMedKategori(sektorSykefravær, sektorSykefraværArgumentCaptor.firstValue)
        assertEqualsSykefraværMedKategori(landSykefravær, landSykefraværArgumentCaptor.firstValue)
        Assertions.assertThat(antallEksporterte).isEqualTo(1)
    }

    @Test
    fun eksporter_sender_riktig_melding_til_kafka_inkluderer_bransje_ved_tilhørighet_bransejprogram() {
        val årstallOgKvartal: ÅrstallOgKvartal = __2020_2
        val fraÅrstallOgKvartal: ÅrstallOgKvartal = __2020_2.minusKvartaler(3)
        val virksomhet1_TilHørerBransjeMetadata = virksomhet1_TilHørerBransjeMetadata(årstallOgKvartal)
        virksomhet1_TilHørerBransjeMetadata.leggTilNæringOgNæringskode5siffer(
            listOf(
                Næringskode("86101"), Næringskode("86102")
            )
        )
        whenever(
            legacyEksporteringRepository.hentVirksomhetEksportPerKvartal(
                __2020_2
            )
        ).thenReturn(listOf(virksomhetEksportPerKvartal))
        whenever(
            legacyVirksomhetMetadataRepository.hentVirksomhetMetadataMedNæringskoder(
                årstallOgKvartal
            )
        ).thenReturn(
            listOf(
                virksomhet1_TilHørerBransjeMetadata
            )
        )
        whenever(
            sykefraværStatistikkSektorRepository.hentForKvartaler(any())
        ).thenReturn(listOf(sykefraværsstatistikkSektor))
        whenever(
            sykefraværStatistikkNæringRepository.hentForAlleNæringer(any())
        ).thenReturn(
            listOf(
                sykefraværsstatistikkNæring(fraÅrstallOgKvartal),
                sykefraværsstatistikkNæring(fraÅrstallOgKvartal.plussKvartaler(1)),
                sykefraværsstatistikkNæring(fraÅrstallOgKvartal.plussKvartaler(2)),
                sykefraværsstatistikkNæring(fraÅrstallOgKvartal.plussKvartaler(3))
            )
        )
        whenever(
            sykefraværStatistikkNæringskodeRepository.hentAltForKvartaler(any())
        ).thenReturn(
            listOf(
                sykefraværsstatistikkNæring5SifferBransjeprogram("86101", årstallOgKvartal),
                sykefraværsstatistikkNæring5SifferBransjeprogram(
                    "86101", årstallOgKvartal.minusKvartaler(1)
                ),
                sykefraværsstatistikkNæring5SifferBransjeprogram(
                    "86101", årstallOgKvartal.minusKvartaler(2)
                ),
                sykefraværsstatistikkNæring5SifferBransjeprogram(
                    "86101", årstallOgKvartal.minusKvartaler(3)
                ),
                sykefraværsstatistikkNæring5SifferBransjeprogram("86102", årstallOgKvartal),
                sykefraværsstatistikkNæring5SifferBransjeprogram(
                    "86102", årstallOgKvartal.minusKvartaler(1)
                ),
                sykefraværsstatistikkNæring5SifferBransjeprogram(
                    "86102", årstallOgKvartal.minusKvartaler(2)
                ),
                sykefraværsstatistikkNæring5SifferBransjeprogram(
                    "86102", årstallOgKvartal.minusKvartaler(3)
                )
            )
        )
        whenever(
            sykefravarStatistikkVirksomhetRepository.hentSykefraværAlleVirksomheter(listOf(__2020_2))
        ).thenReturn(
            listOf(
                sykefraværsstatistikkVirksomhet(fraÅrstallOgKvartal, "987654321"),
                sykefraværsstatistikkVirksomhet(fraÅrstallOgKvartal.plussKvartaler(1), "987654321"),
                sykefraværsstatistikkVirksomhet(fraÅrstallOgKvartal.plussKvartaler(2), "987654321"),
                sykefraværsstatistikkVirksomhet(
                    fraÅrstallOgKvartal.plussKvartaler(3), "987654321"
                )
            )
        )
        whenever(sykefraværStatistikkLandRepository.hentForKvartaler(any())).thenReturn(
            sykefraværsstatistikkLandSiste4Kvartaler(årstallOgKvartal)
        )
        val antallEksporterte = service.legacyEksporter(årstallOgKvartal).getOrNull()
        verify(kafkaClient)?.send(
            årstallOgKvartalArgumentCaptor.capture(),
            virksomhetSykefraværArgumentCaptor.capture(),
            næring5SifferSykefraværArgumentCaptor.capture(),
            næringSykefraværArgumentCaptor.capture(),
            sektorSykefraværArgumentCaptor.capture(),
            landSykefraværArgumentCaptor.capture()
        )
        Assertions.assertThat(årstallOgKvartalArgumentCaptor.firstValue).isEqualTo(årstallOgKvartal)
        assertEqualsVirksomhetSykefravær(
            virksomhetSykefravær, virksomhetSykefraværArgumentCaptor.firstValue
        )
        Assertions.assertThat(næring5SifferSykefraværArgumentCaptor.firstValue.size).isEqualTo(2)
        assertEqualsSykefraværMedKategori(
            næring5SifferSykefraværArgumentCaptor.firstValue[0], næring5SifferSykefraværTilhørerBransje
        )
        Assertions.assertThat(antallEksporterte).isEqualTo(1)
    }
}

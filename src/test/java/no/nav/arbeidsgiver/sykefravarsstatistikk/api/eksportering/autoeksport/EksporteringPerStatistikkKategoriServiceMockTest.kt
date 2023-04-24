package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksporteringRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.SykefraværsstatistikkTilEksporteringRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetEksportPerKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class EksporteringPerStatistikkKategoriServiceMockTest {
    private val sykefraværRepository = mock<SykefraværRepository>()
    private val sykefraværsstatistikkTilEksporteringRepository = mock<SykefraværsstatistikkTilEksporteringRepository>()
    private val eksporteringRepository = mock<EksporteringRepository>()
    private val kafkaService = mock<KafkaService>()
    private var service: EksporteringPerStatistikkKategoriService = EksporteringPerStatistikkKategoriService(
        sykefraværRepository,
        sykefraværsstatistikkTilEksporteringRepository,
        eksporteringRepository,
        kafkaService,
        true,
    )

    private var årstallOgKvartalArgumentCaptor = argumentCaptor<ÅrstallOgKvartal>()

    private val statistikkategoriArgumentCaptor = argumentCaptor<Statistikkategori>()

    private val identifikatorArgumentCaptor = argumentCaptor<String>()

    private val sykefraværMedKategoriArgumentCaptor = argumentCaptor<SykefraværMedKategori>()

    private val sykefraværFlereKvartalerForEksportArgumentCaptor = argumentCaptor<SykefraværFlereKvartalerForEksport>()

    @BeforeEach
    fun setUp() {
        service = EksporteringPerStatistikkKategoriService(
            sykefraværRepository,
            sykefraværsstatistikkTilEksporteringRepository,
            eksporteringRepository,
            kafkaService,
            true
        )
    }

    @Test
    fun eksporterSykefraværsstatistikkLand__sender_riktig_melding_til_kafka_og_returnerer_antall_meldinger_sendt() {
        val umaskertSykefraværForEttKvartalListe =
            EksporteringServiceTestUtils.sykefraværsstatistikkLandSiste4Kvartaler(EksporteringServiceTestUtils.__2020_2)
        whenever(sykefraværRepository.hentUmaskertSykefraværForNorge(any()))
            .thenReturn(umaskertSykefraværForEttKvartalListe)
        whenever(
            kafkaService.sendTilStatistikkKategoriTopic(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        )
            .thenReturn(true)
        val antallEksporterte = service.eksporterSykefraværsstatistikkLand(EksporteringServiceTestUtils.__2020_2)
        verify(kafkaService)
            .sendTilStatistikkKategoriTopic(
                årstallOgKvartalArgumentCaptor.capture(),
                statistikkategoriArgumentCaptor.capture(),
                identifikatorArgumentCaptor.capture(),
                sykefraværMedKategoriArgumentCaptor.capture(),
                sykefraværFlereKvartalerForEksportArgumentCaptor.capture()
            )
        Assertions.assertThat(årstallOgKvartalArgumentCaptor.firstValue).isEqualTo(EksporteringServiceTestUtils.__2020_2)
        Assertions.assertThat(statistikkategoriArgumentCaptor.firstValue).isEqualTo(Statistikkategori.LAND)
        Assertions.assertThat(identifikatorArgumentCaptor.firstValue).isEqualTo("NO")
        EksporteringServiceTestUtils.assertEqualsSykefraværMedKategori(
            EksporteringServiceTestUtils.landSykefravær, sykefraværMedKategoriArgumentCaptor.firstValue
        )
        EksporteringServiceTestUtils.assertEqualsSykefraværFlereKvartalerForEksport(
            SykefraværFlereKvartalerForEksport(umaskertSykefraværForEttKvartalListe),
            sykefraværFlereKvartalerForEksportArgumentCaptor.firstValue
        )
        Assertions.assertThat(antallEksporterte).isEqualTo(1)
    }

    @Test
    fun eksporterSykefraværsstatistikkVirksomhet__sender_riktig_melding_til_kafka_og_returnerer_antall_meldinger_sendt() {
        // 1- Mock det database og repositories returnerer. Samme med KafkaService
        val allData = listOf(
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet(
                EksporteringServiceTestUtils.__2020_2,
                "987654321"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet(
                EksporteringServiceTestUtils.__2020_1,
                "987654321"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet(
                EksporteringServiceTestUtils.__2019_4,
                "987654321"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet(
                EksporteringServiceTestUtils.__2019_3,
                "987654321"
            )
        )
        whenever(
            sykefraværsstatistikkTilEksporteringRepository.hentSykefraværAlleVirksomheter(
                EksporteringServiceTestUtils.__2019_3, EksporteringServiceTestUtils.__2020_2
            )
        )
            .thenReturn(allData)
        whenever(
            kafkaService.sendTilStatistikkKategoriTopic(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        )
            .thenReturn(true)
        whenever(eksporteringRepository.hentVirksomhetEksportPerKvartal(EksporteringServiceTestUtils.__2020_2))
            .thenReturn(
                listOf(
                    VirksomhetEksportPerKvartal(
                        Orgnr("987654321"), ÅrstallOgKvartal(2022, 2), false
                    )
                )
            )

        // 2- Kall tjenesten
        val antallEksporterte = service.eksporterSykefraværsstatistikkVirksomhet(
            EksporteringServiceTestUtils.__2020_2, EksporteringBegrensning.build().utenBegrensning()
        )

        // 3- Sjekk hva Kafka har fått
        verify(kafkaService)
            .sendTilStatistikkKategoriTopic(
                årstallOgKvartalArgumentCaptor.capture(),
                statistikkategoriArgumentCaptor.capture(),
                identifikatorArgumentCaptor.capture(),
                sykefraværMedKategoriArgumentCaptor.capture(),
                sykefraværFlereKvartalerForEksportArgumentCaptor.capture()
            )
        Assertions.assertThat(årstallOgKvartalArgumentCaptor.firstValue).isEqualTo(EksporteringServiceTestUtils.__2020_2)
        Assertions.assertThat(statistikkategoriArgumentCaptor.firstValue).isEqualTo(Statistikkategori.VIRKSOMHET)
        Assertions.assertThat(identifikatorArgumentCaptor.firstValue).isEqualTo("987654321")
        EksporteringServiceTestUtils.assertEqualsSykefraværMedKategori(
            EksporteringServiceTestUtils.virksomhetSykefraværMedKategori, sykefraværMedKategoriArgumentCaptor.firstValue
        )
        Assertions.assertThat(antallEksporterte).isEqualTo(1)
    }

    @Test
    fun eksporterSykefraværsstatistikkVirksomhet__returnerer_korrekt_antall_meldinger_sendt() {
        val allData = listOf(
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet(
                EksporteringServiceTestUtils.__2020_2,
                "987654321"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet(
                EksporteringServiceTestUtils.__2020_1,
                "987654321"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet(
                EksporteringServiceTestUtils.__2019_4,
                "987654321"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet(
                EksporteringServiceTestUtils.__2019_3,
                "987654321"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet(
                EksporteringServiceTestUtils.__2020_2,
                "987654322"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet(
                EksporteringServiceTestUtils.__2020_1,
                "987654322"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet(
                EksporteringServiceTestUtils.__2019_4,
                "987654322"
            )
        )
        whenever(
            sykefraværsstatistikkTilEksporteringRepository.hentSykefraværAlleVirksomheter(
                EksporteringServiceTestUtils.__2019_3, EksporteringServiceTestUtils.__2020_2
            )
        )
            .thenReturn(allData)
        whenever(
            kafkaService.sendTilStatistikkKategoriTopic(any(), any(), any(), any(), any())
        )
            .thenReturn(true)
        whenever(eksporteringRepository.hentVirksomhetEksportPerKvartal(EksporteringServiceTestUtils.__2020_2))
            .thenReturn(
                listOf(
                    VirksomhetEksportPerKvartal(
                        Orgnr("987654321"), ÅrstallOgKvartal(2022, 2), false
                    ),
                    VirksomhetEksportPerKvartal(
                        Orgnr("987654322"), ÅrstallOgKvartal(2022, 2), false
                    )
                )
            )
        whenever(kafkaService.antallMeldingerMottattForUtsending).thenReturn(2)
        val antallEksporterte = service.eksporterSykefraværsstatistikkVirksomhet(
            EksporteringServiceTestUtils.__2020_2, EksporteringBegrensning.build().utenBegrensning()
        )
        Assertions.assertThat(antallEksporterte).isEqualTo(2)
    }

    @Test
    fun eksporterSykefraværsstatistikkVirksomhet__eksporterer_til_og_med_bedrifter_uten_statistikk() {
        val allData = listOf(
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet(
                EksporteringServiceTestUtils.__2020_2,
                "987654321"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet(
                EksporteringServiceTestUtils.__2020_1,
                "987654321"
            )
        )
        whenever(
            sykefraværsstatistikkTilEksporteringRepository.hentSykefraværAlleVirksomheter(
                EksporteringServiceTestUtils.__2019_3, EksporteringServiceTestUtils.__2020_2
            )
        )
            .thenReturn(allData)
        whenever(
            kafkaService.sendTilStatistikkKategoriTopic(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        )
            .thenReturn(true)
        whenever(eksporteringRepository.hentVirksomhetEksportPerKvartal(EksporteringServiceTestUtils.__2020_2))
            .thenReturn(
                listOf(
                    VirksomhetEksportPerKvartal(
                        Orgnr("987654321"), ÅrstallOgKvartal(2020, 2), false
                    ),
                    VirksomhetEksportPerKvartal(
                        Orgnr("987654322"), ÅrstallOgKvartal(2020, 2), false
                    )
                )
            )
        val antallEksporterte = service.eksporterSykefraværsstatistikkVirksomhet(
            EksporteringServiceTestUtils.__2020_2, EksporteringBegrensning.build().utenBegrensning()
        )
        Assertions.assertThat(antallEksporterte).isEqualTo(2)
    }

    @Test
    fun eksporterSykefraværsstatistikkNæring__sender_riktig_melding_til_kafka_og_returnerer_antall_meldinger_sendt() {
        // 1- Mock det database og repositories returnerer. Samme med KafkaService
        val allData = listOf(
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2020_2, "11"),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2020_1, "11"),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2019_4, "11"),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2019_3, "11")
        )

        // #todo: tester for repository-funksjon
        whenever(
            sykefraværsstatistikkTilEksporteringRepository.hentSykefraværAlleNæringerFraOgMed(any())
        )
            .thenReturn(allData)
        whenever(
            kafkaService.sendTilStatistikkKategoriTopic(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        )
            .thenReturn(true)

        // 2- Kall tjenesten
        val antallEksporterte = service.eksporterSykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2020_2)

        // 3- Sjekk hva Kafka har fått
        verify(kafkaService)
            .sendTilStatistikkKategoriTopic(
                årstallOgKvartalArgumentCaptor.capture(),
                statistikkategoriArgumentCaptor.capture(),
                identifikatorArgumentCaptor.capture(),
                sykefraværMedKategoriArgumentCaptor.capture(),
                sykefraværFlereKvartalerForEksportArgumentCaptor.capture()
            )
        Assertions.assertThat(årstallOgKvartalArgumentCaptor.firstValue).isEqualTo(EksporteringServiceTestUtils.__2020_2)
        Assertions.assertThat(statistikkategoriArgumentCaptor.firstValue).isEqualTo(Statistikkategori.NÆRING)
        Assertions.assertThat(identifikatorArgumentCaptor.firstValue).isEqualTo("11")
        EksporteringServiceTestUtils.assertEqualsSykefraværMedKategori(
            EksporteringServiceTestUtils.næringSykefravær, sykefraværMedKategoriArgumentCaptor.firstValue
        )
        Assertions.assertThat(antallEksporterte).isEqualTo(1)
    }

    @Test
    fun eksporterSykefraværsstatistikkNæring__returnerer_korrekt_antall_meldinger_sendt() {
        val allData = listOf(
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2020_2, "11"),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2020_1, "11"),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2019_4, "11"),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2019_3, "11"),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2020_2, "12"),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2020_1, "12"),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2019_4, "12"),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2019_3, "12"),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2020_2, "13"),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2020_1, "13"),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2019_4, "13"),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2019_3, "13")
        )
        whenever(
            sykefraværsstatistikkTilEksporteringRepository.hentSykefraværAlleNæringerFraOgMed(any())
        )
            .thenReturn(allData)
        whenever(
            kafkaService.sendTilStatistikkKategoriTopic(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        )
            .thenReturn(true)
        val antallEksporterte = service.eksporterSykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2020_2)
        Assertions.assertThat(antallEksporterte).isEqualTo(3)
    }
}
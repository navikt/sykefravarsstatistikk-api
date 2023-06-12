package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport

import net.javacrumbs.jsonunit.assertj.assertThatJson
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.SykefraværsstatistikkTilEksporteringRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaTopic
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto.StatistikkategoriKafkamelding
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class EksporteringPerStatistikkKategoriServiceMockTest {
    private val sykefraværRepository = mock<SykefraværRepository>()
    private val sykefraværsstatistikkTilEksporteringRepository = mock<SykefraværsstatistikkTilEksporteringRepository>()
    private val kafkaService = mock<KafkaService>()
    private var service: EksporteringPerStatistikkKategoriService = EksporteringPerStatistikkKategoriService(
        sykefraværRepository,
        sykefraværsstatistikkTilEksporteringRepository,
        kafkaService,
        true,
    )

    private val statistikkategoriKafkameldingCaptor = argumentCaptor<StatistikkategoriKafkamelding>()

    @BeforeEach
    fun setUp() {
        service = EksporteringPerStatistikkKategoriService(
            sykefraværRepository,
            sykefraværsstatistikkTilEksporteringRepository,
            kafkaService,
            true
        )
    }

    @Test
    fun eksporterSykefraværsstatistikkLand__sender_riktig_melding_til_kafka() {
        val umaskertSykefraværForEttKvartalListe =
            EksporteringServiceTestUtils.sykefraværsstatistikkLandSiste4Kvartaler(EksporteringServiceTestUtils.__2020_2)
        whenever(sykefraværRepository.hentUmaskertSykefraværForNorge(any()))
            .thenReturn(umaskertSykefraværForEttKvartalListe)

        service.eksporterSykefraværsstatistikkLand(EksporteringServiceTestUtils.__2020_2)

        verify(kafkaService)
            .sendMelding(
                statistikkategoriKafkameldingCaptor.capture(),
                eq(KafkaTopic.SYKEFRAVARSSTATISTIKK_LAND_V1)
            )

        assertThatJson(statistikkategoriKafkameldingCaptor.firstValue.innhold) {
            isObject
            node("kategori").isString.isEqualTo(Statistikkategori.LAND.name)
            node("kode").isString.isEqualTo("NO")
            node("sistePubliserteKvartal").isObject.node("årstall").isString.isEqualTo("2020")
        }
    }

    @Test
    fun eksporterSykefraværsstatistikkVirksomhet__sender_riktig_melding_til_kafka() {
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

        // 2- Kall tjenesten
        service.eksporterSykefraværsstatistikkVirksomhet(
            EksporteringServiceTestUtils.__2020_2
        )

        // 3- Sjekk hva Kafka har fått
        verify(kafkaService)
            .sendMelding(
                statistikkategoriKafkameldingCaptor.capture(),
                eq(KafkaTopic.SYKEFRAVARSSTATISTIKK_VIRKSOMHET_V1)
            )

        assertThatJson(statistikkategoriKafkameldingCaptor.firstValue.innhold) {
            isObject
            node("kategori").isString.isEqualTo(Statistikkategori.VIRKSOMHET.name)
            node("kode").isString.isEqualTo("987654321")
            node("sistePubliserteKvartal").isObject.node("årstall").isString.isEqualTo("2020")
            node("siste4Kvartal").isObject.node("prosent").isString.isEqualTo("10000000000")
        }

    }


    @Test
    fun eksporterSykefraværsstatistikkNæring__sender_riktig_melding_til_kafka() {
        // 1- Mock det database og repositories returnerer. Samme med KafkaService
        val allData = listOf(
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2020_2, "11"),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2020_1, "11"),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2019_4, "11"),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2019_3, "11")
        )

        whenever(
            sykefraværsstatistikkTilEksporteringRepository.hentSykefraværAlleNæringerFraOgMed(any())
        )
            .thenReturn(allData)

        // 2- Kall tjenesten
        service.eksporterSykefraværsstatistikkNæring(EksporteringServiceTestUtils.__2020_2)

        // 3- Sjekk hva Kafka har fått
        verify(kafkaService)
            .sendMelding(
                statistikkategoriKafkameldingCaptor.capture(),
                eq(KafkaTopic.SYKEFRAVARSSTATISTIKK_NARING_V1)
            )

        assertThatJson(statistikkategoriKafkameldingCaptor.firstValue.innhold) {
            isObject
            node("kategori").isString.isEqualTo(Statistikkategori.NÆRING)
            node("kode").isString.isEqualTo("11")
            node("sistePubliserteKvartal").isObject.node("årstall").isEqualTo("2020").node("kvartal").isString.isEqualTo("2")
        }

        assertThatJson(statistikkategoriKafkameldingCaptor.firstValue.nøkkel) {
            isObject
            node("kategori").isString.isEqualTo(Statistikkategori.NÆRING)
            node("kode").isString.isEqualTo("11")
            node("kvartal").isString.isEqualTo("2")
            node("årstall").isString.isEqualTo("2020")
        }
    }
}
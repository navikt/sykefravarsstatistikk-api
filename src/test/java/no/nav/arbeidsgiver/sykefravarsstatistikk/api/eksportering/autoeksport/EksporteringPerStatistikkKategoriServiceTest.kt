package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport

import net.javacrumbs.jsonunit.assertj.assertThatJson
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringPerStatistikkKategoriService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.KafkaTopic
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.__2019_3
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.__2019_4
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.__2020_1
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.__2020_2
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværsstatistikkTilEksporteringRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.KafkaClient
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto.StatistikkategoriKafkamelding
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.math.BigDecimal

class EksporteringPerStatistikkKategoriServiceTest {
    private val sykefraværRepository = mock<SykefraværRepository>()
    private val sykefraværsstatistikkTilEksporteringRepository = mock<SykefraværsstatistikkTilEksporteringRepository>()
    private val kafkaClient = mock<KafkaClient>()
    private var service: EksporteringPerStatistikkKategoriService = EksporteringPerStatistikkKategoriService(
        sykefraværRepository,
        sykefraværsstatistikkTilEksporteringRepository,
        kafkaClient,
    )

    private val statistikkategoriKafkameldingCaptor = argumentCaptor<StatistikkategoriKafkamelding>()

    @BeforeEach
    fun setUp() {
        service = EksporteringPerStatistikkKategoriService(
            sykefraværRepository,
            sykefraværsstatistikkTilEksporteringRepository,
            kafkaClient,
        )
    }

    @Test
    fun eksporterSykefraværsstatistikkLand__sender_riktig_melding_til_kafka() {
        val umaskertSykefraværForEttKvartalListe =
            EksporteringServiceTestUtils.sykefraværsstatistikkLandSiste4Kvartaler(__2020_2)
        whenever(sykefraværRepository.hentUmaskertSykefraværForNorge(any()))
            .thenReturn(umaskertSykefraværForEttKvartalListe)

        service.eksporterPerStatistikkKategori(__2020_2, Statistikkategori.LAND)

        verify(kafkaClient)
            .sendMelding(
                statistikkategoriKafkameldingCaptor.capture(),
                eq(KafkaTopic.SYKEFRAVARSSTATISTIKK_LAND_V1)
            )

        assertThatJson(statistikkategoriKafkameldingCaptor.firstValue.innhold) {
            isObject
            node("kategori").isString.isEqualTo(Statistikkategori.LAND.name)
            node("kode").isString.isEqualTo("NO")
            node("sistePubliserteKvartal").isObject.node("årstall").isNumber.isEqualTo(BigDecimal("2020"))
        }
    }

    @Test
    fun eksporterSykefraværsstatistikkVirksomhet__sender_riktig_melding_til_kafka() {
        // 1- Mock det database og repositories returnerer. Samme med KafkaService
        val allData = listOf(
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet(
                __2020_2,
                "987654321"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet(
                __2020_1,
                "987654321"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet(
                __2019_4,
                "987654321"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet(
                __2019_3,
                "987654321"
            )
        )
        whenever(
            sykefraværsstatistikkTilEksporteringRepository.hentSykefraværAlleVirksomheter(
                __2019_3, __2020_2
            )
        )
            .thenReturn(allData)

        // 2- Kall tjenesten
        service.eksporterPerStatistikkKategori(
                __2020_2,
                Statistikkategori.VIRKSOMHET
        )


        // 3- Sjekk hva Kafka har fått
        verify(kafkaClient)
            .sendMelding(
                statistikkategoriKafkameldingCaptor.capture(),
                eq(KafkaTopic.SYKEFRAVARSSTATISTIKK_VIRKSOMHET_V1)
            )

        assertThatJson(statistikkategoriKafkameldingCaptor.firstValue.innhold) {
            isObject
            node("kategori").isString.isEqualTo(Statistikkategori.VIRKSOMHET.name)
            node("kode").isString.isEqualTo("987654321")
            node("sistePubliserteKvartal").isObject.node("årstall").isNumber.isEqualTo(BigDecimal("2020"))
            node("siste4Kvartal").isObject.node("prosent").isNumber.isEqualTo(BigDecimal("2.0"))
        }
    }


    @Test
    fun eksporterSykefraværsstatistikkNæring__sender_riktig_melding_til_kafka() {
        // 1- Mock det database og repositories returnerer. Samme med KafkaService
        val allData = listOf(
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(__2020_2, "11"),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(__2020_1, "11"),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(__2019_4, "11"),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæring(__2019_3, "11")
        )

        whenever(
            sykefraværsstatistikkTilEksporteringRepository.hentSykefraværAlleNæringerFraOgMed(__2019_3)
        )
            .thenReturn(allData)

        // 2- Kall tjenesten
        service.eksporterPerStatistikkKategori(
                __2020_2,
                Statistikkategori.NÆRING)

        // 3- Sjekk hva Kafka har fått
        verify(kafkaClient)
            .sendMelding(
                statistikkategoriKafkameldingCaptor.capture(),
                eq(KafkaTopic.SYKEFRAVARSSTATISTIKK_NARING_V1)
            )

        assertThatJson(statistikkategoriKafkameldingCaptor.firstValue.innhold) {
            isObject
            node("kategori").isString.isEqualTo(Statistikkategori.NÆRING.name)
            node("kode").isString.isEqualTo("11")
            node("sistePubliserteKvartal").isObject.node("årstall").isNumber.isEqualTo(BigDecimal("2020"))
            node("sistePubliserteKvartal").isObject.node("kvartal").isNumber.isEqualTo(BigDecimal("2"))
        }

        assertThatJson(statistikkategoriKafkameldingCaptor.firstValue.nøkkel) {
            isObject
            node("kategori").isString.isEqualTo(Statistikkategori.NÆRING.name)
            node("kode").isString.isEqualTo("11")
            node("kvartal").isString.isEqualTo("2")
            node("årstall").isString.isEqualTo("2020")
        }
    }

    @Test
    fun eksporterSykefraværsstatistikkNæringskode__sender_riktig_melding_til_kafka() {
        // 1- Mock det database og repositories returnerer. Samme med KafkaService
        val allData = listOf(
                EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(årstallOgKvartal = __2020_2, næringskode = "11001"),
                EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(årstallOgKvartal = __2020_1, næringskode = "11001"),
                EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(årstallOgKvartal = __2019_4, næringskode = "11001"),
                EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(årstallOgKvartal = __2019_3, næringskode = "11001"),
                EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(
                        årstallOgKvartal = __2020_2,
                        næringskode = "22002",
                        tapteDagsverk = BigDecimal(10.0),
                        muligeDagsverk = BigDecimal(100)
                ),
                EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(
                        årstallOgKvartal = __2020_1,
                        næringskode = "22002",
                        tapteDagsverk = BigDecimal(15.0),
                        muligeDagsverk = BigDecimal(100)
                ),
        )

        whenever(
                sykefraværsstatistikkTilEksporteringRepository
                        .hentSykefraværprosentForAlleNæringskoder(__2019_3, __2020_2)
        ).thenReturn(allData)

        // 2- Kall tjenesten
        service.eksporterPerStatistikkKategori(__2020_2, Statistikkategori.NÆRINGSKODE)

        // 3- Sjekk hva Kafka har fått
        verify(kafkaClient, times(2))
                .sendMelding(
                        statistikkategoriKafkameldingCaptor.capture(),
                        eq(KafkaTopic.SYKEFRAVARSSTATISTIKK_NARINGSKODE_V1)
                )

        assertThatJson(statistikkategoriKafkameldingCaptor.firstValue.innhold) {
            isObject
            node("kategori").isString.isEqualTo(Statistikkategori.NÆRINGSKODE.name)
            node("kode").isString.isEqualTo("11001")
            node("sistePubliserteKvartal").isObject.node("årstall").isNumber.isEqualTo(BigDecimal("2020"))
            node("sistePubliserteKvartal").isObject.node("kvartal").isNumber.isEqualTo(BigDecimal("2"))
            node("sistePubliserteKvartal").isObject.node("prosent").isNumber.isEqualTo(BigDecimal("2.0"))
            node("siste4Kvartal").isObject.node("prosent").isNumber.isEqualTo(BigDecimal("2.0"))
            node("siste4Kvartal").isObject.node("tapteDagsverk").isNumber.isEqualTo(BigDecimal("400.0"))
            node("siste4Kvartal").isObject.node("muligeDagsverk").isNumber.isEqualTo(BigDecimal("20000.0"))
            node("siste4Kvartal").isObject.node("kvartaler").isArray.size().isEqualTo(4)
            node("siste4Kvartal").isObject.node("kvartaler").isArray.first().isObject.node("årstall").isNumber.isEqualByComparingTo(BigDecimal(2020))
            node("siste4Kvartal").isObject.node("kvartaler").isArray.last().isObject.node("årstall").isNumber.isEqualByComparingTo(BigDecimal(2019))
        }

        assertThatJson(statistikkategoriKafkameldingCaptor.secondValue.innhold) {
            isObject
            node("kategori").isString.isEqualTo(Statistikkategori.NÆRINGSKODE.name)
            node("kode").isString.isEqualTo("22002")
            node("sistePubliserteKvartal").isObject.node("årstall").isNumber.isEqualTo(BigDecimal("2020"))
            node("sistePubliserteKvartal").isObject.node("kvartal").isNumber.isEqualTo(BigDecimal("2"))
            node("sistePubliserteKvartal").isObject.node("prosent").isNumber.isEqualTo(BigDecimal("10.0"))
            node("siste4Kvartal").isObject.node("prosent").isNumber.isEqualTo(BigDecimal("12.5"))
            node("siste4Kvartal").isObject.node("tapteDagsverk").isNumber.isEqualTo(BigDecimal("25.0"))
            node("siste4Kvartal").isObject.node("muligeDagsverk").isNumber.isEqualTo(BigDecimal("200.0"))
            node("siste4Kvartal").isObject.node("kvartaler").isArray.size().isEqualTo(2)
            node("siste4Kvartal").isObject.node("kvartaler").isArray.first().isObject.node("årstall").isNumber.isEqualByComparingTo(BigDecimal(2020))
        }

        assertThatJson(statistikkategoriKafkameldingCaptor.firstValue.nøkkel) {
            isObject
            node("kategori").isString.isEqualTo(Statistikkategori.NÆRINGSKODE.name)
            node("kode").isString.isEqualTo("11001")
            node("kvartal").isString.isEqualTo("2")
            node("årstall").isString.isEqualTo("2020")
        }
    }

    @Test
    fun `eksport sykefraværsstatistikk NÆRINGSKODE hopper over næringskoder som ikke har statistikk for forespurt kvartal`() {
        val allData = listOf(
                EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(årstallOgKvartal = __2020_2, næringskode = "11001"),
                EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(årstallOgKvartal = __2020_1, næringskode = "11001"),
                EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(årstallOgKvartal = __2019_4, næringskode = "11001"),
                EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(årstallOgKvartal = __2020_2, næringskode = "22002"),
                EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(årstallOgKvartal = __2020_1, næringskode = "22002"),
                EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(årstallOgKvartal = __2020_1, næringskode = "33003"),
                EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(årstallOgKvartal = __2019_4, næringskode = "33003"),
                )

        whenever(
                sykefraværsstatistikkTilEksporteringRepository
                        .hentSykefraværprosentForAlleNæringskoder(__2019_3, __2020_2)
        ).thenReturn(allData)

        service.eksporterPerStatistikkKategori(__2020_2, Statistikkategori.NÆRINGSKODE)

        verify(kafkaClient, times(2))
                .sendMelding(
                        statistikkategoriKafkameldingCaptor.capture(),
                        eq(KafkaTopic.SYKEFRAVARSSTATISTIKK_NARINGSKODE_V1)
                )

        assertThatJson(statistikkategoriKafkameldingCaptor.firstValue.innhold) {
            isObject
            node("kode").isString.isEqualTo("11001")
            node("sistePubliserteKvartal").isObject.node("årstall").isNumber.isEqualTo(BigDecimal("2020"))
            node("sistePubliserteKvartal").isObject.node("kvartal").isNumber.isEqualTo(BigDecimal("2"))
        }

        assertThatJson(statistikkategoriKafkameldingCaptor.secondValue.innhold) {
            isObject
            node("kategori").isString.isEqualTo(Statistikkategori.NÆRINGSKODE.name)
            node("kode").isString.isEqualTo("22002")
            node("sistePubliserteKvartal").isObject.node("årstall").isNumber.isEqualTo(BigDecimal("2020"))
            node("sistePubliserteKvartal").isObject.node("kvartal").isNumber.isEqualTo(BigDecimal("2"))
        }

        assertThatJson(statistikkategoriKafkameldingCaptor.firstValue.nøkkel) {
            isObject
            node("kategori").isString.isEqualTo(Statistikkategori.NÆRINGSKODE.name)
            node("kode").isString.isEqualTo("11001")
            node("kvartal").isString.isEqualTo("2")
            node("årstall").isString.isEqualTo("2020")
        }
    }
}

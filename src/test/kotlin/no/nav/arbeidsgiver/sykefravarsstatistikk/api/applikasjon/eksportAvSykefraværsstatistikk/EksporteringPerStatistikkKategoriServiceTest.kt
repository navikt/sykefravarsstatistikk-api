package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk

import net.javacrumbs.jsonunit.assertj.assertThatJson
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringServiceTestUtils.__2019_3
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringServiceTestUtils.__2019_4
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringServiceTestUtils.__2020_1
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringServiceTestUtils.__2020_2
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringServiceTestUtils.sykefraværsstatistikkSektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkLand
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.KafkaTopic
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.KafkaClient
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto.GradertStatistikkategoriKafkamelding
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto.StatistikkategoriKafkamelding
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.*
import java.math.BigDecimal


class EksporteringPerStatistikkKategoriServiceTest {
    private val sykefraværStatistikkLandRepository = mock<SykefraværStatistikkLandRepository>()
    private val sykefravarStatistikkVirksomhetRepository = mock<SykefravarStatistikkVirksomhetRepository>()
    private val sykefraværStatistikkSektorRepository = mock<SykefraværStatistikkSektorRepository>()
    private val sykefravarStatistikkVirksomhetGraderingRepository =
        mock<SykefravarStatistikkVirksomhetGraderingRepository>()
    private val sykefraværStatistikkNæringRepository = mock<SykefraværStatistikkNæringRepository>()
    private val sykefraværStatistikkNæringskodeRepository = mock<SykefraværStatistikkNæringskodeRepository>()

    private val kafkaClient = mock<KafkaClient>()

    private val service: EksporteringPerStatistikkKategoriService = EksporteringPerStatistikkKategoriService(
        sykefraværStatistikkLandRepository = sykefraværStatistikkLandRepository,
        sykefraværStatistikkSektorRepository = sykefraværStatistikkSektorRepository,
        sykefravarStatistikkVirksomhetRepository = sykefravarStatistikkVirksomhetRepository,
        sykefravarStatistikkVirksomhetGraderingRepository = sykefravarStatistikkVirksomhetGraderingRepository,
        sykefraværStatistikkNæringRepository = sykefraværStatistikkNæringRepository,
        sykefraværStatistikkNæringskodeRepository = sykefraværStatistikkNæringskodeRepository,
        kafkaClient = kafkaClient,
    )

    private val statistikkategoriKafkameldingCaptor = argumentCaptor<StatistikkategoriKafkamelding>()
    private val gradertStatistikkategoriKafkameldingCaptor = argumentCaptor<GradertStatistikkategoriKafkamelding>()

    @Test
    fun eksporterSykefraværsstatistikkLand__sender_riktig_melding_til_kafka() {
        val umaskertSykefraværForEttKvartalListe =
            EksporteringServiceTestUtils.sykefraværsstatistikkLandSiste4Kvartaler(__2020_2)
        whenever(sykefraværStatistikkLandRepository.hentSykefraværstatistikkLand(any()))
            .thenReturn(umaskertSykefraværForEttKvartalListe.map {
                SykefraværsstatistikkLand(
                    it.årstallOgKvartal.årstall,
                    it.årstallOgKvartal.kvartal,
                    it.antallPersoner,
                    it.dagsverkTeller,
                    it.dagsverkNevner
                )
            })

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
    fun eksporterSykefraværsstatistikkSektor__sender_riktig_melding_til_kafka() {
        val testData = sykefraværsstatistikkSektor
        whenever(sykefraværStatistikkSektorRepository.hentForKvartaler(any())).thenReturn(listOf(testData))

        service.eksporterPerStatistikkKategori(__2020_2, Statistikkategori.SEKTOR)

        verify(kafkaClient)
            .sendMelding(
                statistikkategoriKafkameldingCaptor.capture(),
                eq(KafkaTopic.SYKEFRAVARSSTATISTIKK_SEKTOR_V1)
            )

        assertThatJson(statistikkategoriKafkameldingCaptor.firstValue.innhold) {
            isObject
            node("kategori").isString.isEqualTo(Statistikkategori.SEKTOR.name)
            node("kode").isString.isEqualTo("1")
            node("sistePubliserteKvartal").isObject.node("årstall").isNumber.isEqualTo(BigDecimal("2020"))
            node("sistePubliserteKvartal").isObject.node("kvartal").isNumber.isEqualTo(BigDecimal("2"))
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
            sykefravarStatistikkVirksomhetRepository.hentSykefraværAlleVirksomheter(__2020_2 inkludertTidligere 3)
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
    fun eksporterSykefraværsstatistikkVirksomhet__sender_riktig_antall_meldinger_til_kafka() {
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
            ),EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet(
                __2020_2,
                "123412341"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet(
                __2020_1,
                "123412341"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet(
                __2019_4,
                "123412341"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet(
                __2019_3,
                "123412341"
            )
        )
        whenever(
            sykefravarStatistikkVirksomhetRepository.hentSykefraværAlleVirksomheter(__2020_2 inkludertTidligere 3)
        )
            .thenReturn(allData)

        // 2- Kall tjenesten
        service.eksporterPerStatistikkKategori(
            __2020_2,
            Statistikkategori.VIRKSOMHET
        )


        // 3- Sjekk hva Kafka har fått
        verify(kafkaClient, times(2)).sendMelding(any(), any())
    }

    @Test
    fun `eksporter statistikk for virksomhet gradert sender riktig melding til kafka`() {
        val allData = listOf(
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhetGradert(__2020_2, "11"),
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhetGradert(__2020_1, "11"),
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhetGradert(__2019_4, "11"),
            EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhetGradert(__2019_3, "11")
        )

        whenever(
            sykefravarStatistikkVirksomhetGraderingRepository.hentSykefraværAlleVirksomheterGradert(__2020_2 inkludertTidligere 3)
        ).thenReturn(allData)

        service.eksporterPerStatistikkKategori(
            __2020_2,
            Statistikkategori.VIRKSOMHET_GRADERT
        )

        verify(kafkaClient)
            .sendMelding(
                gradertStatistikkategoriKafkameldingCaptor.capture(),
                eq(KafkaTopic.SYKEFRAVARSSTATISTIKK_VIRKSOMHET_GRADERT_V1)
            )

        assertThatJson(gradertStatistikkategoriKafkameldingCaptor.firstValue.innhold) {
            isObject
            node("kategori").isString.isEqualTo(Statistikkategori.VIRKSOMHET_GRADERT.name)
            node("kode").isString.isEqualTo("11")
            node("sistePubliserteKvartal").isObject.node("årstall").isNumber.isEqualTo(BigDecimal("2020"))
            node("sistePubliserteKvartal").isObject.node("kvartal").isNumber.isEqualTo(BigDecimal("2"))
            node("sistePubliserteKvartal").isObject.node("prosent").isNumber.isEqualTo(BigDecimal("50.0"))
            node("sistePubliserteKvartal").isObject.node("tapteDagsverkGradert").isNumber.isEqualTo(BigDecimal("5.0"))
            node("sistePubliserteKvartal").isObject.node("tapteDagsverk").isNumber.isEqualTo(BigDecimal("10.0"))
            node("siste4Kvartal").isObject.node("prosent").isNumber.isEqualTo(BigDecimal("50.0"))
            node("siste4Kvartal").isObject.node("tapteDagsverkGradert").isNumber.isEqualTo(BigDecimal("20.0"))
            node("siste4Kvartal").isObject.node("tapteDagsverk").isNumber.isEqualTo(BigDecimal("40.0"))
        }

        assertThatJson(gradertStatistikkategoriKafkameldingCaptor.firstValue.nøkkel) {
            isObject
            node("kategori").isString.isEqualTo(Statistikkategori.VIRKSOMHET_GRADERT.name)
            node("kode").isString.isEqualTo("11")
            node("kvartal").isString.isEqualTo("2")
            node("årstall").isString.isEqualTo("2020")
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
            sykefraværStatistikkNæringRepository.hentForAlleNæringer(__2020_2 inkludertTidligere 3)
        )
            .thenReturn(allData)

        // 2- Kall tjenesten
        service.eksporterPerStatistikkKategori(
            __2020_2,
            Statistikkategori.NÆRING
        )

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
            EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(
                årstallOgKvartal = __2020_2,
                næringskode = "11001"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(
                årstallOgKvartal = __2020_1,
                næringskode = "11001"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(
                årstallOgKvartal = __2019_4,
                næringskode = "11001"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(
                årstallOgKvartal = __2019_3,
                næringskode = "11001"
            ),
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
            sykefraværStatistikkNæringskodeRepository.hentAltForKvartaler(any())
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
            node("siste4Kvartal").isObject.node("kvartaler").isArray.first().isObject.node("årstall").isNumber.isEqualByComparingTo(
                BigDecimal(2020)
            )
            node("siste4Kvartal").isObject.node("kvartaler").isArray.last().isObject.node("årstall").isNumber.isEqualByComparingTo(
                BigDecimal(2019)
            )
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
            node("siste4Kvartal").isObject.node("kvartaler").isArray.first().isObject.node("årstall").isNumber.isEqualByComparingTo(
                BigDecimal(2020)
            )
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
            EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(
                årstallOgKvartal = __2020_2,
                næringskode = "11001"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(
                årstallOgKvartal = __2020_1,
                næringskode = "11001"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(
                årstallOgKvartal = __2019_4,
                næringskode = "11001"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(
                årstallOgKvartal = __2020_2,
                næringskode = "22002"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(
                årstallOgKvartal = __2020_1,
                næringskode = "22002"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(
                årstallOgKvartal = __2020_1,
                næringskode = "33003"
            ),
            EksporteringServiceTestUtils.sykefraværsstatistikkNæringskode(
                årstallOgKvartal = __2019_4,
                næringskode = "33003"
            ),
        )

        whenever(sykefraværStatistikkNæringskodeRepository.hentAltForKvartaler(any())).thenReturn(allData)

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

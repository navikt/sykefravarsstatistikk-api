package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk

import net.javacrumbs.jsonunit.assertj.assertThatJson
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
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

    private val __2020_2 = ÅrstallOgKvartal(2020, 2)
    private val __2020_1 = ÅrstallOgKvartal(2020, 1)
    private val __2019_4 = ÅrstallOgKvartal(2019, 4)
    private val __2019_3 = ÅrstallOgKvartal(2019, 3)

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
            listOf(
                UmaskertSykefraværForEttKvartal(
                    årstallOgKvartal = __2020_2,
                    dagsverkTeller = BigDecimal(10000000),
                    dagsverkNevner = BigDecimal(500000000),
                    antallPersoner = 2500000
                ),
                UmaskertSykefraværForEttKvartal(
                    årstallOgKvartal = __2020_2.minusKvartaler(1),
                    dagsverkTeller = BigDecimal(9000000),
                    dagsverkNevner = BigDecimal(500000000),
                    antallPersoner = 2500000
                ),
                UmaskertSykefraværForEttKvartal(
                    årstallOgKvartal = __2020_2.minusKvartaler(2),
                    dagsverkTeller = BigDecimal(11000000),
                    dagsverkNevner = BigDecimal(500000000),
                    antallPersoner = 2500000
                ),
                UmaskertSykefraværForEttKvartal(
                    årstallOgKvartal = __2020_2.minusKvartaler(3),
                    dagsverkTeller = BigDecimal(8000000),
                    dagsverkNevner = BigDecimal(500000000),
                    antallPersoner = 2500000
                )
            )
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
    fun `eksporterPerStatistikkKategori sender riktig melding til kafka for Sektor`() {
        val data = SykefraværsstatistikkSektor(
            årstall = __2020_2.årstall,
            kvartal = __2020_2.kvartal,
            sektor = Sektor.STATLIG,
            antallPersoner = 33000,
            tapteDagsverk = BigDecimal(1340),
            muligeDagsverk = BigDecimal(88000)
        )
        whenever(sykefraværStatistikkSektorRepository.hentForKvartaler(any())).thenReturn(listOf(data))

        service.eksporterPerStatistikkKategori(__2020_2, Statistikkategori.SEKTOR)

        verify(kafkaClient)
            .sendMelding(
                statistikkategoriKafkameldingCaptor.capture(),
                eq(KafkaTopic.SYKEFRAVARSSTATISTIKK_SEKTOR_V1)
            )

        assertThatJson(statistikkategoriKafkameldingCaptor.firstValue.innhold) {
            isObject
            node("kategori").isString.isEqualTo("SEKTOR")
            node("kode").isString.isEqualTo("STATLIG")
            node("sistePubliserteKvartal").isObject.node("årstall").isNumber.isEqualTo(BigDecimal("2020"))
            node("sistePubliserteKvartal").isObject.node("kvartal").isNumber.isEqualTo(BigDecimal("2"))
        }
    }

    @Test
    fun eksporterSykefraværsstatistikkVirksomhet__sender_riktig_melding_til_kafka() {
        // 1- Mock det database og repositories returnerer. Samme med KafkaService
        val kvartaler = __2020_2 inkludertTidligere 3

        val data = kvartaler.map {
            SykefraværsstatistikkVirksomhetUtenVarighet(
                årstall = it.årstall,
                kvartal = it.kvartal,
                orgnr = "987654321",
                antallPersoner = 6,
                tapteDagsverk = BigDecimal(10),
                muligeDagsverk = BigDecimal(500)
            )
        }

        whenever(sykefravarStatistikkVirksomhetRepository.hentSykefraværAlleVirksomheter(kvartaler)).thenReturn(data)

        // 2- Kall tjenesten
        service.eksporterPerStatistikkKategori(__2020_2, Statistikkategori.VIRKSOMHET)


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
            node("sistePubliserteKvartal").isObject.node("erMaskert").isBoolean.isFalse()
            node("siste4Kvartal").isObject.node("prosent").isNumber.isEqualTo(BigDecimal("2.0"))
            node("siste4Kvartal").isObject.node("erMaskert").isBoolean.isFalse()
        }
    }


    @Test
    fun `eksporterSykefraværsstatistikkVirksomhet tar hensyn til maskering`() {
        val viskromhetMedMaskertKvartal = listOf(
            SykefraværsstatistikkVirksomhetUtenVarighet(
                årstall = __2020_2.årstall,
                kvartal = __2020_2.kvartal,
                orgnr = "999999999",
                antallPersoner = 4,
                tapteDagsverk = 20.toBigDecimal(),
                muligeDagsverk = 1000.toBigDecimal()
            ),
            SykefraværsstatistikkVirksomhetUtenVarighet(
                årstall = __2020_1.årstall,
                kvartal = __2020_1.kvartal,
                orgnr = "999999999",
                antallPersoner = 5,
                tapteDagsverk = 20.toBigDecimal(),
                muligeDagsverk = 1000.toBigDecimal()
            ),
        )

        whenever(sykefravarStatistikkVirksomhetRepository.hentSykefraværAlleVirksomheter(any()))
            .thenReturn(viskromhetMedMaskertKvartal)


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
            // Siste kvartal har bare fire personer, og skal være maskert
            node("kategori").isString.isEqualTo(Statistikkategori.VIRKSOMHET.name)
            node("sistePubliserteKvartal").isObject.node("årstall").isNumber.isEqualTo(BigDecimal("2020"))
            node("sistePubliserteKvartal").isObject.node("prosent").isNull()
            node("sistePubliserteKvartal").isObject.node("antallPersoner").isIntegralNumber.isEqualTo(4)
            node("sistePubliserteKvartal").isObject.node("erMaskert").isBoolean.isTrue()

            // Gjennomsnittet har kvartaler med fem personer, og skal derfor ikke være maskert
            node("siste4Kvartal").isObject.node("prosent").isNumber.isEqualTo(2.0.toBigDecimal())
            node("siste4Kvartal").isObject.node("erMaskert").isBoolean.isFalse()
        }
    }

    fun sykefraværsstatistikkVirksomhet(
        årstallOgKvartal: ÅrstallOgKvartal, orgnr: String
    ): SykefraværsstatistikkVirksomhetUtenVarighet {
        return SykefraværsstatistikkVirksomhetUtenVarighet(
            årstall = årstallOgKvartal.årstall,
            kvartal = årstallOgKvartal.kvartal,
            orgnr = orgnr,
            antallPersoner = 6,
            tapteDagsverk = BigDecimal(10),
            muligeDagsverk = BigDecimal(500)
        )
    }

    @Test
    fun eksporterSykefraværsstatistikkVirksomhet__sender_riktig_antall_meldinger_til_kafka() {
        // 1- Mock det database og repositories returnerer. Samme med KafkaService
        val allData = listOf(
            sykefraværsstatistikkVirksomhet(
                __2020_2,
                "987654321"
            ),
            sykefraværsstatistikkVirksomhet(
                __2020_1,
                "987654321"
            ),
            sykefraværsstatistikkVirksomhet(
                __2019_4,
                "987654321"
            ),
            sykefraværsstatistikkVirksomhet(
                __2019_3,
                "987654321"
            ), sykefraværsstatistikkVirksomhet(
                __2020_2,
                "123412341"
            ),
            sykefraværsstatistikkVirksomhet(
                __2020_1,
                "123412341"
            ),
            sykefraværsstatistikkVirksomhet(
                __2019_4,
                "123412341"
            ),
            sykefraværsstatistikkVirksomhet(
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
        val kvartaler = __2020_2 inkludertTidligere 3

        whenever(
            sykefravarStatistikkVirksomhetGraderingRepository.hentSykefraværAlleVirksomheterGradert(any())
        ).thenReturn(
            kvartaler.map {
                SykefraværsstatistikkVirksomhetMedGradering(
                    årstall = it.årstall,
                    kvartal = it.kvartal,
                    orgnr = "191919191",
                    næring = "10",
                    næringkode = "10111",
                    rectype = "3",
                    tapteDagsverkGradertSykemelding = BigDecimal(5),
                    antallPersoner = 6,
                    tapteDagsverk = BigDecimal(10),
                    muligeDagsverk = BigDecimal(100),
                )
            })

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
            node("kode").isString.isEqualTo("191919191")
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
            node("kode").isString.isEqualTo("191919191")
            node("kvartal").isString.isEqualTo("2")
            node("årstall").isString.isEqualTo("2020")
        }
    }


    @Test
    fun eksporterSykefraværsstatistikkNæring__sender_riktig_melding_til_kafka() {
        // 1- Mock det database og repositories returnerer. Samme med KafkaService

        val kvartaler = __2020_2 inkludertTidligere 3

        val data = kvartaler.map {
            SykefraværsstatistikkForNæring(
                årstall = it.årstall,
                kvartal = it.kvartal,
                næring = "11",
                antallPersoner = 150,
                tapteDagsverk = BigDecimal(100),
                muligeDagsverk = BigDecimal(5000)
            )
        }

        whenever(sykefraværStatistikkNæringRepository.hentForAlleNæringer(kvartaler)).thenReturn(data)

        // 2- Kall tjenesten
        service.eksporterPerStatistikkKategori(__2020_2, Statistikkategori.NÆRING)

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
            SykefraværsstatistikkForNæringskode(
                årstall = __2020_2.årstall,
                kvartal = __2020_2.kvartal,
                næringskode = "11001",
                antallPersoner = 150,
                tapteDagsverk = BigDecimal(100),
                muligeDagsverk = BigDecimal(5000)
            ),
            SykefraværsstatistikkForNæringskode(
                årstall = __2020_1.årstall,
                kvartal = __2020_1.kvartal,
                næringskode = "11001",
                antallPersoner = 150,
                tapteDagsverk = BigDecimal(100),
                muligeDagsverk = BigDecimal(5000)
            ),
            SykefraværsstatistikkForNæringskode(
                årstall = __2019_4.årstall,
                kvartal = __2019_4.kvartal,
                næringskode = "11001",
                antallPersoner = 150,
                tapteDagsverk = BigDecimal(100),
                muligeDagsverk = BigDecimal(5000)
            ),
            SykefraværsstatistikkForNæringskode(
                årstall = __2019_3.årstall,
                kvartal = __2019_3.kvartal,
                næringskode = "11001",
                antallPersoner = 150,
                tapteDagsverk = BigDecimal(100),
                muligeDagsverk = BigDecimal(5000)
            ),
            SykefraværsstatistikkForNæringskode(
                årstall = __2020_2.årstall,
                kvartal = __2020_2.kvartal,
                næringskode = "22002",
                antallPersoner = 150,
                tapteDagsverk = BigDecimal(10.0),
                muligeDagsverk = BigDecimal(100)
            ),
            SykefraværsstatistikkForNæringskode(
                årstall = __2020_1.årstall,
                kvartal = __2020_1.kvartal,
                næringskode = "22002",
                antallPersoner = 150,
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
            SykefraværsstatistikkForNæringskode(
                årstall = __2020_2.årstall,
                kvartal = __2020_2.kvartal,
                næringskode = "11001",
                antallPersoner = 150,
                tapteDagsverk = BigDecimal(100),
                muligeDagsverk = BigDecimal(5000)
            ),
            SykefraværsstatistikkForNæringskode(
                årstall = __2020_1.årstall,
                kvartal = __2020_1.kvartal,
                næringskode = "11001",
                antallPersoner = 150,
                tapteDagsverk = BigDecimal(100),
                muligeDagsverk = BigDecimal(5000)
            ),
            SykefraværsstatistikkForNæringskode(
                årstall = __2019_4.årstall,
                kvartal = __2019_4.kvartal,
                næringskode = "11001",
                antallPersoner = 150,
                tapteDagsverk = BigDecimal(100),
                muligeDagsverk = BigDecimal(5000)
            ),
            SykefraværsstatistikkForNæringskode(
                årstall = __2020_2.årstall,
                kvartal = __2020_2.kvartal,
                næringskode = "22002",
                antallPersoner = 150,
                tapteDagsverk = BigDecimal(100),
                muligeDagsverk = BigDecimal(5000)
            ),
            SykefraværsstatistikkForNæringskode(
                årstall = __2020_1.årstall,
                kvartal = __2020_1.kvartal,
                næringskode = "22002",
                antallPersoner = 150,
                tapteDagsverk = BigDecimal(100),
                muligeDagsverk = BigDecimal(5000)
            ),
            SykefraværsstatistikkForNæringskode(
                årstall = __2020_1.årstall,
                kvartal = __2020_1.kvartal,
                næringskode = "33003",
                antallPersoner = 150,
                tapteDagsverk = BigDecimal(100),
                muligeDagsverk = BigDecimal(5000)
            ),
            SykefraværsstatistikkForNæringskode(
                årstall = __2019_4.årstall,
                kvartal = __2019_4.kvartal,
                næringskode = "33003",
                antallPersoner = 150,
                tapteDagsverk = BigDecimal(100),
                muligeDagsverk = BigDecimal(5000)
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

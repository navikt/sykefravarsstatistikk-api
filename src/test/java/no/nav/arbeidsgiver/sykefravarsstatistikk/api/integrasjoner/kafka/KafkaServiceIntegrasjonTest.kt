package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka

import net.javacrumbs.jsonunit.assertj.assertThatJson
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.SykefraværsstatistikkLocalTestApplication
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.SykefraværFlereKvartalerForEksport
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.ArbeidsmiljøportalenBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaTopicNavn
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaTopicNavn.Companion.toStringArray
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto.MetadataVirksomhetKafkamelding
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto.Sektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@ActiveProfiles("mvc-test", "kafka-test")
@SpringBootTest(
    classes = [SykefraværsstatistikkLocalTestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@DirtiesContext
@EnableMockOAuth2Server
@Import(EmbeddedKafkaBrokerConfig::class)
class KafkaServiceIntegrasjonTest {
    @Autowired
    private lateinit var kafkaService: KafkaService

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    private lateinit var container: KafkaMessageListenerContainer<String, String>
    private lateinit var consumerRecords: BlockingQueue<ConsumerRecord<String, String>>
    private val dummyData = SykefraværFlereKvartalerForEksport(
        listOf(
            UmaskertSykefraværForEttKvartal(
                EksporteringServiceTestUtils.__2020_2, BigDecimal(1100), BigDecimal(11000), 5
            )
        )
    )

    @BeforeEach
    fun setUp() {
        consumerRecords = LinkedBlockingQueue()
        val containerProperties = ContainerProperties(*TOPIC_NAMES)
        val consumerProperties =
            KafkaTestUtils.consumerProps("consumer", "false", embeddedKafkaBroker)
        consumerProperties[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] =
            StringDeserializer::class.java
        consumerProperties[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] =
            StringDeserializer::class.java
        consumerProperties[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG] = "60000"
        val kafkaConsumerFactory = DefaultKafkaConsumerFactory<String, String>(consumerProperties)
        container = KafkaMessageListenerContainer(kafkaConsumerFactory, containerProperties)
        container.setupMessageListener(
            MessageListener { record: ConsumerRecord<String, String> ->
                println("Listened message=$record")
                (consumerRecords as LinkedBlockingQueue<ConsumerRecord<String, String>>).add(record)
            } as MessageListener<String, String>)
        container.start()
        ContainerTestUtils.waitForAssignment(
            container, embeddedKafkaBroker.partitionsPerTopic * TOPIC_NAMES.size
        )
    }

    @AfterEach
    fun tearDown() {
        container.destroy()
    }

    @Test
    fun `send kafkamelding med metadata sender på riktig topic`() {
        kafkaService.send(
            MetadataVirksomhetKafkamelding(
                "999999999",
                ÅrstallOgKvartal(2023, 2),
                "86101",
                ArbeidsmiljøportalenBransje.SYKEHUS,
                Sektor.STATLIG
            ),
            KafkaTopicNavn.SYKEFRAVARSSTATISTIKK_METADATA_V1
        )
        val message = consumerRecords.poll(10, TimeUnit.SECONDS)
        assertThat(message?.topic()).isEqualTo(KafkaTopicNavn.SYKEFRAVARSSTATISTIKK_METADATA_V1.topic)
    }

    @Test
    fun `send kafkamelding med metadata sender riktig data`() {
        kafkaService.send(
            MetadataVirksomhetKafkamelding(
                "999999999",
                ÅrstallOgKvartal(2023, 2),
                "86101",
                ArbeidsmiljøportalenBransje.SYKEHUS,
                Sektor.STATLIG
            ),
            KafkaTopicNavn.SYKEFRAVARSSTATISTIKK_METADATA_V1
        )
        val message = consumerRecords.poll(10, TimeUnit.SECONDS)
        assertThatJson(message!!.value()) {
            isObject
            node("orgnr").isString.isEqualTo("999999999")
            node("arstall").isString.isEqualTo("2023")
            node("kvartal").isString.isEqualTo("2")
            node("bransje").isString.isEqualTo("SYKEHUS")
            node("naringskode").isString.isEqualTo("86101")
            node("sektor").isString.isEqualTo("STATLIG")
        }
    }

    @Test
    fun `send kafkamelding for alle kategorier sender melding til riktig topic`() {
        kafkaService.send(
            EksporteringServiceTestUtils.__2020_2,
            EksporteringServiceTestUtils.virksomhetSykefravær,
            listOf(EksporteringServiceTestUtils.næring5SifferSykefravær),
            EksporteringServiceTestUtils.næringSykefravær,
            EksporteringServiceTestUtils.sektorSykefravær,
            EksporteringServiceTestUtils.landSykefravær
        )
        val message = consumerRecords.poll(10, TimeUnit.SECONDS)
        Assertions.assertEquals(KafkaTopicNavn.SYKEFRAVARSSTATISTIKK_V1.topic, message!!.topic())
    }

    @Test
    fun `send kafkamelding for landkategori sender melding til riktig topic`() {
        kafkaService.sendTilStatistikkKategoriTopic(
            EksporteringServiceTestUtils.__2020_2,
            Statistikkategori.LAND,
            "NO",
            EksporteringServiceTestUtils.landSykefravær,
            dummyData
        )
        val message = consumerRecords.poll(10, TimeUnit.SECONDS)
        Assertions.assertEquals(
            KafkaTopicNavn.SYKEFRAVARSSTATISTIKK_LAND_V1.topic,
            message!!.topic()
        )
    }

    @Test
    fun `send for kategori næring sender til "sykefravarsstatistikk-næring-topic-v1"`() {
        kafkaService.sendTilStatistikkKategoriTopic(
            EksporteringServiceTestUtils.__2020_2,
            Statistikkategori.NÆRING,
            "11",
            EksporteringServiceTestUtils.næringSykefravær,
            dummyData
        )
        val message = consumerRecords.poll(10, TimeUnit.SECONDS)
        Assertions.assertEquals(
            KafkaTopicNavn.SYKEFRAVARSSTATISTIKK_NARING_V1.topic,
            message!!.topic()
        )
    }

    @Test
    fun send__forSektorKategori__senderTilSykefravarsstatistikkSektorV1Topic() {
        kafkaService.sendTilStatistikkKategoriTopic(
            EksporteringServiceTestUtils.__2020_2,
            Statistikkategori.SEKTOR,
            "11",
            EksporteringServiceTestUtils.næringSykefravær,
            dummyData
        )
        val message = consumerRecords.poll(10, TimeUnit.SECONDS)
        Assertions.assertEquals(
            KafkaTopicNavn.SYKEFRAVARSSTATISTIKK_SEKTOR_V1.topic,
            message!!.topic()
        )
    }

    @Test
    fun send__forVirksomhetKategori__senderTilSykefravarsstatistikkVirksomhetV1Topic() {
        kafkaService.sendTilStatistikkKategoriTopic(
            EksporteringServiceTestUtils.__2020_2,
            Statistikkategori.VIRKSOMHET,
            "11",
            EksporteringServiceTestUtils.virksomhetSykefraværMedKategori,
            dummyData
        )
        val message = consumerRecords.poll(10, TimeUnit.SECONDS)
        Assertions.assertEquals(
            KafkaTopicNavn.SYKEFRAVARSSTATISTIKK_VIRKSOMHET_V1.topic,
            message!!.topic()
        )
    }

    companion object {
        private val TOPIC_NAMES = toStringArray()
    }
}
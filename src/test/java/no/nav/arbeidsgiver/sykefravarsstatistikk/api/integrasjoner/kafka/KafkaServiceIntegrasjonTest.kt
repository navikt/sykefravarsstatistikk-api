package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.SykefraværsstatistikkLocalTestApplication
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.SykefraværFlereKvartalerForEksport
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaTopicName
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaTopicName.Companion.toStringArray
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.AfterClass
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
@EnableMockOAuth2Server
@Import(
    EmbeddedKafkaBrokerConfig::class
)
class KafkaServiceIntegrasjonTest {
    @Autowired
    private val kafkaService: KafkaService? = null

    @Autowired
    private val embeddedKafkaBroker: EmbeddedKafkaBroker? = null
    private var container: KafkaMessageListenerContainer<String, String>? = null
    private var consumerRecords: BlockingQueue<ConsumerRecord<String, String>>? = null
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
        val consumerProperties = KafkaTestUtils.consumerProps("consumer", "false", embeddedKafkaBroker)
        consumerProperties[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        consumerProperties[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        consumerProperties[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG] = "60000"
        val kafkaConsumerFactory = DefaultKafkaConsumerFactory<String, String>(consumerProperties)
        container = KafkaMessageListenerContainer(kafkaConsumerFactory, containerProperties)
        container!!.setupMessageListener(
            MessageListener { record: ConsumerRecord<String, String> ->
                println("Listened message=$record")
                (consumerRecords as LinkedBlockingQueue<ConsumerRecord<String, String>>).add(record)
            } as MessageListener<String, String>)
    }

    @AfterEach
    fun tearDown() {
        container!!.stop()
    }

    @AfterClass
    fun tearDownClass() {
        container!!.destroy()
        embeddedKafkaBroker!!.destroy()
    }

    @Test
    @Throws(Exception::class)
    fun send__forAlleKategorier__senderTilSykefravarsstatistikkV1Topic() {
        container!!.start()
        ContainerTestUtils.waitForAssignment(
            container, embeddedKafkaBroker!!.partitionsPerTopic * TOPIC_NAMES.size
        )
        kafkaService!!.send(
            EksporteringServiceTestUtils.__2020_2,
            EksporteringServiceTestUtils.virksomhetSykefravær,
            listOf(EksporteringServiceTestUtils.næring5SifferSykefravær),
            EksporteringServiceTestUtils.næringSykefravær,
            EksporteringServiceTestUtils.sektorSykefravær,
            EksporteringServiceTestUtils.landSykefravær
        )
        val message = consumerRecords!!.poll(10, TimeUnit.SECONDS)
        Assertions.assertEquals(KafkaTopicName.SYKEFRAVARSSTATISTIKK_V1.topic, message!!.topic())
    }

    @Test
    @Throws(Exception::class)
    fun send__forLandKategori__senderTilSykefravarsstatistikkLandV1Topic() {
        container!!.start()
        ContainerTestUtils.waitForAssignment(
            container, embeddedKafkaBroker!!.partitionsPerTopic * TOPIC_NAMES.size
        )
        kafkaService!!.sendTilStatistikkKategoriTopic(
            EksporteringServiceTestUtils.__2020_2,
            Statistikkategori.LAND,
            "NO",
            EksporteringServiceTestUtils.landSykefravær,
            dummyData
        )
        val message = consumerRecords!!.poll(10, TimeUnit.SECONDS)
        Assertions.assertEquals(KafkaTopicName.SYKEFRAVARSSTATISTIKK_LAND_V1.topic, message!!.topic())
    }

    @Test
    @Throws(Exception::class)
    fun send__forNæringKategori__senderTilSykefravarsstatistikkNæringV1Topic() {
        container!!.start()
        ContainerTestUtils.waitForAssignment(
            container, embeddedKafkaBroker!!.partitionsPerTopic * TOPIC_NAMES.size
        )
        kafkaService!!.sendTilStatistikkKategoriTopic(
            EksporteringServiceTestUtils.__2020_2,
            Statistikkategori.NÆRING,
            "11",
            EksporteringServiceTestUtils.næringSykefravær,
            dummyData
        )
        val message = consumerRecords!!.poll(10, TimeUnit.SECONDS)
        Assertions.assertEquals(KafkaTopicName.SYKEFRAVARSSTATISTIKK_NARING_V1.topic, message!!.topic())
    }

    @Test
    @Throws(Exception::class)
    fun send__forSektorKategori__senderTilSykefravarsstatistikkSektorV1Topic() {
        container!!.start()
        ContainerTestUtils.waitForAssignment(
            container, embeddedKafkaBroker!!.partitionsPerTopic * TOPIC_NAMES.size
        )
        kafkaService!!.sendTilStatistikkKategoriTopic(
            EksporteringServiceTestUtils.__2020_2,
            Statistikkategori.SEKTOR,
            "11",
            EksporteringServiceTestUtils.næringSykefravær,
            dummyData
        )
        val message = consumerRecords!!.poll(10, TimeUnit.SECONDS)
        Assertions.assertEquals(KafkaTopicName.SYKEFRAVARSSTATISTIKK_SEKTOR_V1.topic, message!!.topic())
    }

    @Test
    @Throws(Exception::class)
    fun send__forVirksomhetKategori__senderTilSykefravarsstatistikkVirksomhetV1Topic() {
        container!!.start()
        ContainerTestUtils.waitForAssignment(
            container, embeddedKafkaBroker!!.partitionsPerTopic * TOPIC_NAMES.size
        )
        kafkaService!!.sendTilStatistikkKategoriTopic(
            EksporteringServiceTestUtils.__2020_2,
            Statistikkategori.VIRKSOMHET,
            "11",
            EksporteringServiceTestUtils.virksomhetSykefraværMedKategori,
            dummyData
        )
        val message = consumerRecords!!.poll(10, TimeUnit.SECONDS)
        Assertions.assertEquals(KafkaTopicName.SYKEFRAVARSSTATISTIKK_VIRKSOMHET_V1.topic, message!!.topic())
    }

    companion object {
        private val TOPIC_NAMES = toStringArray()
    }
}
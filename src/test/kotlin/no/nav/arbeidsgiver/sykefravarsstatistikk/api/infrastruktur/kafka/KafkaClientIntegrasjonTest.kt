package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka

import config.SykefraværsstatistikkLocalTestApplication
import ia.felles.definisjoner.bransjer.Bransje
import net.javacrumbs.jsonunit.assertj.assertThatJson
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.LegacyEksporteringTestUtils
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.KafkaTopic
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.PrometheusMetrics
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto.MetadataVirksomhetKafkamelding
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto.SektorKafkaDto
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.EmbeddedKafkaZKBroker
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@ActiveProfiles("mvc-test", "kafka-test")
@SpringBootTest(
    classes = [SykefraværsstatistikkLocalTestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@EnableMockOAuth2Server
class KafkaClientIntegrasjonTest {
    @Autowired
    private lateinit var kafkaClient: KafkaClient

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    // Blir brukt av KafkaClient for å slippe @AutoConfigureObservability på testklassen
    @MockBean
    private lateinit var prometheusMetrics: PrometheusMetrics

    @TestConfiguration
    open class EmbeddedKafkaBrokerConfig {
        private val embeddedKafkaBroker: EmbeddedKafkaBroker =
            EmbeddedKafkaZKBroker(1, true, *KafkaTopic.entries.map { it.navn }.toTypedArray())

        init {
            embeddedKafkaBroker.brokerProperties(mapOf("listeners" to "PLAINTEXT://127.0.0.1:9092", "port" to "9092"))
        }

        @Bean("embeddedKafka", destroyMethod = "destroy")
        @Profile("kafka-test")
        open fun getEmbeddedKafkaBroker() = embeddedKafkaBroker
    }

    private lateinit var container: KafkaMessageListenerContainer<String, String>
    private lateinit var consumerRecords: BlockingQueue<ConsumerRecord<String, String>>

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
        kafkaClient.sendMelding(
            melding = MetadataVirksomhetKafkamelding(
                orgnr = "999999999",
                årstallOgKvartal = ÅrstallOgKvartal(2023, 2),
                næring = "86",
                næringskode = "86101",
                bransje = Bransje.SYKEHUS,
                sektor = SektorKafkaDto.STATLIG
            ),
            topic = KafkaTopic.SYKEFRAVARSSTATISTIKK_METADATA_V1,
        )
        val message = consumerRecords.poll(10, TimeUnit.SECONDS)
        assertThat(message?.topic()).isEqualTo(KafkaTopic.SYKEFRAVARSSTATISTIKK_METADATA_V1.navn)
    }

    @Test
    fun `send kafkamelding med metadata sender riktig data`() {
        kafkaClient.sendMelding(
            melding = MetadataVirksomhetKafkamelding(
                orgnr = "999999999",
                årstallOgKvartal = ÅrstallOgKvartal(2023, 2),
                næring = "86",
                næringskode = "86101",
                bransje = Bransje.SYKEHUS,
                sektor = SektorKafkaDto.STATLIG
            ),
            topic = KafkaTopic.SYKEFRAVARSSTATISTIKK_METADATA_V1,
        )
        val message = consumerRecords.poll(10, TimeUnit.SECONDS)
        assertThatJson(message!!.value()) {
            isObject
            node("orgnr").isString.isEqualTo("999999999")
            node("arstall").isString.isEqualTo("2023")
            node("kvartal").isString.isEqualTo("2")
            node("bransje").isString.isEqualTo("SYKEHUS")
            node("naring").isString.isEqualTo("86")
            node("naringskode").isString.isEqualTo("86101")
            node("sektor").isString.isEqualTo("STATLIG")
        }
    }

    @Test
    fun `legacy kafka sender melding til riktig topic`() {
        kafkaClient.send(
            LegacyEksporteringTestUtils.__2020_2,
            LegacyEksporteringTestUtils.virksomhetSykefravær,
            listOf(LegacyEksporteringTestUtils.næring5SifferSykefravær),
            LegacyEksporteringTestUtils.næringSykefravær,
            LegacyEksporteringTestUtils.sektorSykefravær,
            LegacyEksporteringTestUtils.landSykefravær
        )
        val message = consumerRecords.poll(10, TimeUnit.SECONDS)
        Assertions.assertEquals(KafkaTopic.SYKEFRAVARSSTATISTIKK_V1.navn, message!!.topic())
    }

    companion object {
        private val TOPIC_NAMES = KafkaTopic.entries.map { it.navn }.toTypedArray()
    }
}
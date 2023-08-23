package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka

import net.javacrumbs.jsonunit.assertj.assertThatJson
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.SykefraværsstatistikkLocalTestApplication
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.ArbeidsmiljøportalenBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.KafkaTopic
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.KafkaTopic.Companion.toStringArray
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.KafkaClient
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
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability
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
@AutoConfigureObservability
class KafkaClientIntegrasjonTest {
    @Autowired
    private lateinit var kafkaClient: KafkaClient

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

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
            MetadataVirksomhetKafkamelding(
                "999999999",
                ÅrstallOgKvartal(2023, 2),
                "86101",
                ArbeidsmiljøportalenBransje.SYKEHUS,
                SektorKafkaDto.STATLIG
            ),
            KafkaTopic.SYKEFRAVARSSTATISTIKK_METADATA_V1,
        )
        val message = consumerRecords.poll(10, TimeUnit.SECONDS)
        assertThat(message?.topic()).isEqualTo(KafkaTopic.SYKEFRAVARSSTATISTIKK_METADATA_V1.navn)
    }

    @Test
    fun `send kafkamelding med metadata sender riktig data`() {
        kafkaClient.sendMelding(
            MetadataVirksomhetKafkamelding(
                "999999999",
                ÅrstallOgKvartal(2023, 2),
                "86",
                ArbeidsmiljøportalenBransje.SYKEHUS,
                SektorKafkaDto.STATLIG
            ),
            KafkaTopic.SYKEFRAVARSSTATISTIKK_METADATA_V1,
        )
        val message = consumerRecords.poll(10, TimeUnit.SECONDS)
        assertThatJson(message!!.value()) {
            isObject
            node("orgnr").isString.isEqualTo("999999999")
            node("arstall").isString.isEqualTo("2023")
            node("kvartal").isString.isEqualTo("2")
            node("bransje").isString.isEqualTo("SYKEHUS")
            node("naring").isString.isEqualTo("86")
            node("sektor").isString.isEqualTo("STATLIG")
        }
    }

    @Test
    fun `send kafkamelding for alle kategorier sender melding til riktig topic`() {
        kafkaClient.send(
            EksporteringServiceTestUtils.__2020_2,
            EksporteringServiceTestUtils.virksomhetSykefravær,
            listOf(EksporteringServiceTestUtils.næring5SifferSykefravær),
            EksporteringServiceTestUtils.næringSykefravær,
            EksporteringServiceTestUtils.sektorSykefravær,
            EksporteringServiceTestUtils.landSykefravær
        )
        val message = consumerRecords.poll(10, TimeUnit.SECONDS)
        Assertions.assertEquals(KafkaTopic.SYKEFRAVARSSTATISTIKK_V1.navn, message!!.topic())
    }

    companion object {
        private val TOPIC_NAMES = toStringArray()
    }
}
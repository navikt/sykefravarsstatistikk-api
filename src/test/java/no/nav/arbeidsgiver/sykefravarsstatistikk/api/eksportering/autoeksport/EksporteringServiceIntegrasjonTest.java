package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.SykefraværsstatistikkTilEksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaTopicValue;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.__2020_2;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.sykefraværsstatistikkLand;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.sykefraværsstatistikkNæring;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.sykefraværsstatistikkSektor;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.sykefraværsstatistikkVirksomhet;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.virksomhetEksportPerKvartal;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.virksomhetMetadata;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@Disabled
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ExtendWith(MockitoExtension.class)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"}, topics = {"arbeidsgiver.sykefravarsstatistikk-v1"})
public class EksporteringServiceIntegrasjonTest {

    @Mock
    private EksporteringRepository eksporteringRepository;
    @Mock
    private VirksomhetMetadataRepository virksomhetMetadataRepository;
    @Mock
    private SykefraværsstatistikkTilEksporteringRepository sykefraværsstatistikkTilEksporteringRepository;

    @Autowired
    private KafkaService kafkaService;

    private EksporteringService service;


    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    private BlockingQueue<ConsumerRecord<String, String>> records;
    private KafkaMessageListenerContainer<String, String> container;

    private final static ObjectMapper objectMapper = new ObjectMapper();


    @BeforeAll
    void setUpBeforeAll() {
        DefaultKafkaConsumerFactory<String, String> consumerFactory =
                new DefaultKafkaConsumerFactory<>(getConsumerProperties());
        ContainerProperties containerProperties =
                new ContainerProperties("arbeidsgiver.sykefravarsstatistikk-v1");
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    @AfterAll
    void tearDown() {
        container.stop();
    }

    @BeforeEach
    public void setUp() {
        service = new EksporteringService(
                eksporteringRepository,
                virksomhetMetadataRepository,
                sykefraværsstatistikkTilEksporteringRepository,
                kafkaService,
                true
        );
    }

    @Test
    public void eksporter_returnerer_antall_rader_eksportert() {
        when(eksporteringRepository.hentVirksomhetEksportPerKvartal(__2020_2)).thenReturn(Collections.emptyList());

        int antallEksporterte = service.eksporter(__2020_2);

        Assertions.assertThat(antallEksporterte).isEqualTo(0);
    }

    @Test
    public void eksporter_sender_melding_til_kafka_og_returnerer_antall_meldinger_sendt() throws Exception {
        when(eksporteringRepository.hentVirksomhetEksportPerKvartal(__2020_2))
                .thenReturn(Arrays.asList(virksomhetEksportPerKvartal));
        when(virksomhetMetadataRepository.hentVirksomhetMetadata(__2020_2))
                .thenReturn(Arrays.asList(virksomhetMetadata));
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentLand(__2020_2))
                .thenReturn(sykefraværsstatistikkLand);
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleSektorer(__2020_2))
                .thenReturn(Arrays.asList(sykefraværsstatistikkSektor));
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleNæringer(__2020_2))
                .thenReturn(Arrays.asList(sykefraværsstatistikkNæring));
        when(sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentAlleVirksomheter(__2020_2))
                .thenReturn(Arrays.asList(sykefraværsstatistikkVirksomhet));

        int antallEksporterte = service.eksporter(__2020_2);

        Assertions.assertThat(antallEksporterte).isEqualTo(1);
        ConsumerRecord<String, String> message = records.poll(500, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        assertEquals("{\"orgnr\":\"987654321\",\"kvartal\":2,\"årstall\":2020}", message.key());
        // TODO: FIX me
        assertEquals(
                objectMapper.readValue(message.value(), KafkaTopicValue.class),
                objectMapper.readValue(getKafkaTopicValueAsJsonString(), KafkaTopicValue.class)
        );
    }

    private Map<String, Object> getConsumerProperties() {
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(),
                ConsumerConfig.GROUP_ID_CONFIG, "consumer",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true",
                ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "10",
                ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "60000",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    }


    // Assert metoder og 'expected' verdier

    private static String getKafkaTopicValueAsJsonString() {
        return ("{" +
                        "  \"virksomhetSykefravær\": {" +
                        "    \"prosent\": 2.0," +
                        "    \"tapteDagsverk\": 10.0," +
                        "    \"muligeDagsverk\": 500.0," +
                        "    \"erMaskert\": false," +
                        "    \"kategori\": \"VIRKSOMHET\"," +
                        "    \"orgnr\": \"987654321\"," +
                        "    \"navn\": \"\"," +
                        "    \"årstall\": 2020," +
                        "    \"kvartal\": 2" +
                        "  }," +
                        "  \"næring5SifferSykefravær\": null," +
                        "  \"næringSykefravær\": {" +
                        "    \"prosent\": 2.0," +
                        "    \"tapteDagsverk\": 100.0," +
                        "    \"muligeDagsverk\": 5000.0," +
                        "    \"erMaskert\": false," +
                        "    \"kategori\": \"NÆRING2SIFFER\"," +
                        "    \"kode\": \"11\"," +
                        "    \"årstall\": 2020," +
                        "    \"kvartal\": 2" +
                        "  }," +
                        "  \"sektorSykefravær\": {" +
                        "    \"prosent\": 1.5," +
                        "    \"tapteDagsverk\": 1340.0," +
                        "    \"muligeDagsverk\": 88000.0," +
                        "    \"erMaskert\": false," +
                        "    \"kategori\": \"SEKTOR\"," +
                        "    \"kode\": \"1\"," +
                        "    \"årstall\": 2020," +
                        "    \"kvartal\": 2" +
                        "  }," +
                        "  \"landSykefravær\": {" +
                        "    \"prosent\": 2.0," +
                        "    \"tapteDagsverk\": 10000000.0," +
                        "    \"muligeDagsverk\": 500000000.0," +
                        "    \"erMaskert\": false," +
                        "    \"kategori\": \"LAND\"," +
                        "    \"kode\": \"NO\"," +
                        "    \"årstall\": 2020," +
                        "    \"kvartal\": 2" +
                        "  }" +
                        "}").replaceAll("\\s+", "");
    }
}
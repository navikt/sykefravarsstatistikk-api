package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.SykefraværsstatistikkLocalTestApplication;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.StatistikkDto;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("mvc-test")
@SpringBootTest(
        classes = SykefraværsstatistikkLocalTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@EnableMockOAuth2Server
@EmbeddedKafka(
        controlledShutdown = true,
        topics = {"arbeidsgiver.sykefravarsstatistikk-v1", "arbeidsgiver.sykefravarsstatistikk-land-v1"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"}
)
public class KafkaServiceIntegrasjonTest {

    @Autowired
    private KafkaService kafkaService;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private static String[] TOPIC_NAME = {"arbeidsgiver.sykefravarsstatistikk-v1",
            "arbeidsgiver.sykefravarsstatistikk-land-v1"};
    private KafkaMessageListenerContainer<String, String> container;
    private BlockingQueue<ConsumerRecord<String, String>> consumerRecords;
    private final static ObjectMapper objectMapper = new ObjectMapper();


    @BeforeEach
    public void setUp() {
        consumerRecords = new LinkedBlockingQueue<>();

        ContainerProperties containerProperties = new ContainerProperties(TOPIC_NAME);
        Map<String, Object> consumerProperties =
                KafkaTestUtils.consumerProps(
                        "consumer",
                        "false",
                        embeddedKafkaBroker
                );
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "60000");
        DefaultKafkaConsumerFactory<String, String> kafkaConsumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProperties);

        container = new KafkaMessageListenerContainer<>(kafkaConsumerFactory, containerProperties);
        container.setupMessageListener((MessageListener<String, String>) record -> {
            System.out.println("Listened message=" + record.toString());
            consumerRecords.add(record);
        });
    }


    @AfterEach
    public void tearDown() {
        container.stop();
    }

    @Test
    public void send__sender_en_KafkaTopicValue_til__riktig_topic() throws Exception {
        container.start();
        ContainerTestUtils.waitForAssignment(container, 4);

        kafkaService.send(
                __2020_2,
                virksomhetSykefravær,
                Arrays.asList(næring5SifferSykefravær),
                næringSykefravær,
                sektorSykefravær,
                landSykefravær,
                statistikkDtoList(__2020_2)
        );

        ConsumerRecord<String, String> message = consumerRecords.poll(10, TimeUnit.SECONDS);

        assertNotNull(message);
        assertEquals("{\"orgnr\":\"987654321\",\"kvartal\":2,\"årstall\":2020}", message.key());
        objectMapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));

        assertKafkaTopicValueEquals(
                objectMapper.readValue(getKafkaTopicValueAsJsonString(), KafkaTopicValue.class),
                objectMapper.readValue(message.value(), KafkaTopicValue.class)
        );
    }

    @Test
    public void send_til_LAND_topic___sender_en_KafkaTopicValue_til__riktig_topic() throws Exception {
        StatistikkDto landSykefraværSiste12Måneder = statistikkDtoList(__2020_2).get(0);
        KafkaStatistikkKategoriTopicValue expectedTopicValue = new KafkaStatistikkKategoriTopicValue(
                new SykefraværMedKategori(
                        Statistikkategori.LAND,
                        landSykefravær.getKode(),
                        landSykefravær.getÅrstallOgKvartal(),
                        landSykefravær.getTapteDagsverk(),
                        landSykefravær.getMuligeDagsverk(),
                        landSykefravær.getAntallPersoner()
                ),
                StatistikkDto.builder()
                        .statistikkategori(Statistikkategori.LAND)
                        .label(landSykefraværSiste12Måneder.getLabel())
                        .verdi(landSykefraværSiste12Måneder.getVerdi())
                        .antallPersonerIBeregningen(landSykefraværSiste12Måneder.getAntallPersonerIBeregningen())
                        .kvartalerIBeregningen(landSykefraværSiste12Måneder.getKvartalerIBeregningen())
                        .build()
        );

        container.start();
        ContainerTestUtils.waitForAssignment(container, 4);

        kafkaService.sendTilSykefraværsstatistikkLandTopic(
                __2020_2,
                landSykefravær,
                landSykefraværSiste12Måneder
        );

        ConsumerRecord<String, String> message = consumerRecords.poll(10, TimeUnit.SECONDS);

        assertNotNull(message);
        assertEquals("{\"kategori\":\"LAND\",\"kode\":\"NO\",\"kvartal\":2,\"årstall\":2020}", message.key());
        objectMapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        assertKafkaStatistikkKategoriTopicValueEquals(
                expectedTopicValue,
                objectMapper.readValue(message.value(), KafkaStatistikkKategoriTopicValue.class)
        );
    }
}

package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static java.net.http.HttpClient.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.SISTE_PUBLISERTE_KVARTAL;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.opprettStatistikkForLand;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.opprettStatistikkForVirksomhet;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.opprettTestVirksomhetMetaData;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.skrivSisteImporttidspunktTilDb;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllEksportDataFraDatabase;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAlleImporttidspunkt;
import static org.assertj.core.api.Assertions.assertThat;

import common.SpringIntegrationTestbase;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestTokenUtil;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jetbrains.annotations.NotNull;
import org.junit.AfterClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.context.WebApplicationContext;

@DirtiesContext
@EmbeddedKafka(
    controlledShutdown = true,
    topics = {
      "arbeidsgiver.sykefravarsstatistikk-v1",
      "arbeidsgiver.sykefravarsstatistikk-land-v1",
      "arbeidsgiver.sykefravarsstatistikk-virksomhet-v1"
    },
    brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@Disabled // TODO: spiller dårlig sammen med KafkaServiceIntegrasjonTest (pga "adress already in use
// når EmbeddedKafkaBroker starter")
public class EksporteringControllerApiIntegrationTest extends SpringIntegrationTestbase {

  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired MockOAuth2Server mockOAuth2Server;

  @Autowired private EmbeddedKafkaBroker embeddedKafkaBroker;

  @Autowired private NamedParameterJdbcTemplate jdbcTemplate;

  private static String[] TOPIC_NAMES = {
    "arbeidsgiver.sykefravarsstatistikk-v1",
    "arbeidsgiver.sykefravarsstatistikk-land-v1",
    "arbeidsgiver.sykefravarsstatistikk-virksomhet-v1"
  };
  private KafkaMessageListenerContainer<String, String> container;
  private BlockingQueue<ConsumerRecord<String, String>> consumerRecords;

  static final String ISSUER_TOKENX = "tokenx";

  @LocalServerPort private String port;

  @BeforeEach
  public void setUp() {
    slettAllStatistikkFraDatabase(jdbcTemplate);
    slettAlleImporttidspunkt(jdbcTemplate);
    slettAllEksportDataFraDatabase(jdbcTemplate);
    skrivSisteImporttidspunktTilDb(jdbcTemplate);

    consumerRecords = new LinkedBlockingQueue<>();

    ContainerProperties containerProperties = new ContainerProperties(TOPIC_NAMES);
    Map<String, Object> consumerProperties =
        KafkaTestUtils.consumerProps("consumer", "false", embeddedKafkaBroker);
    consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProperties.put(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProperties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "60000");
    DefaultKafkaConsumerFactory<String, String> kafkaConsumerFactory =
        new DefaultKafkaConsumerFactory<>(consumerProperties);

    container = new KafkaMessageListenerContainer<>(kafkaConsumerFactory, containerProperties);
    container.setupMessageListener(
        (MessageListener<String, String>)
            record -> {
              System.out.println("Listened message=" + record.toString());
              consumerRecords.add(record);
            });
  }

  @AfterEach
  public void tearDown() {
    container.stop();
    container.destroy();
  }

  @AfterClass
  public void tearDownClass() {
    container.destroy();
    embeddedKafkaBroker.destroy();
  }

  @Test
  public void start_eksport_for_land() throws Exception {
    opprettStatistikkForLand(jdbcTemplate);
    HttpResponse<String> response =
        newBuilder()
            .build()
            .send(
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            "http://localhost:"
                                + port
                                + "/sykefravarsstatistikk-api"
                                + "/eksportering/reeksport/statistikkkategori?"
                                + "årstall="
                                + SISTE_PUBLISERTE_KVARTAL.getÅrstall()
                                + "&kvartal="
                                + SISTE_PUBLISERTE_KVARTAL.getKvartal()
                                + "&kategori=LAND"
                                + "&begrensningTil=10"))
                    .header(AUTHORIZATION, getBearerMedJwt())
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build(),
                ofString());

    assertThat(response.statusCode()).isEqualTo(200);
  }

  @Test
  public void start_eksport_for_virksomhet() throws Exception {
    opprettTestVirksomhetMetaData(
        jdbcTemplate,
        SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
        SISTE_PUBLISERTE_KVARTAL.getKvartal(),
        "987654321");
    opprettTestVirksomhetMetaData(
        jdbcTemplate,
        SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
        SISTE_PUBLISERTE_KVARTAL.getKvartal(),
        "999999999");

    opprettStatistikkForVirksomhet(
        jdbcTemplate,
        "987654321",
        SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
        SISTE_PUBLISERTE_KVARTAL.getKvartal(),
        150,
        1000,
        10);
    opprettStatistikkForVirksomhet(
        jdbcTemplate,
        "999999999",
        SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
        SISTE_PUBLISERTE_KVARTAL.getKvartal(),
        150,
        1000,
        10);
    HttpResponse<String> response =
        newBuilder()
            .build()
            .send(
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            "http://localhost:"
                                + port
                                + "/sykefravarsstatistikk-api"
                                + "/eksportering/reeksport/statistikkkategori?"
                                + "årstall="
                                + SISTE_PUBLISERTE_KVARTAL.getÅrstall()
                                + "&kvartal="
                                + SISTE_PUBLISERTE_KVARTAL.getKvartal()
                                + "&kategori=VIRKSOMHET"
                                + "&begrensningTil=10"))
                    .header(AUTHORIZATION, getBearerMedJwt())
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build(),
                ofString());

    assertThat(response.statusCode()).isEqualTo(200);
  }

  @Test
  public void start_eksport_for_virksomhet__skal_ikke_feile_dersom_virksomhet_har_ingen_statistikk()
      throws Exception {
    opprettTestVirksomhetMetaData(
        jdbcTemplate,
        SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
        SISTE_PUBLISERTE_KVARTAL.getKvartal(),
        "987654321");
    opprettTestVirksomhetMetaData(
        jdbcTemplate,
        SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
        SISTE_PUBLISERTE_KVARTAL.getKvartal(),
        "999999999");

    opprettStatistikkForVirksomhet(
        jdbcTemplate,
        "987654321",
        SISTE_PUBLISERTE_KVARTAL.getÅrstall(),
        SISTE_PUBLISERTE_KVARTAL.getKvartal(),
        150,
        1000,
        10);

    HttpResponse<String> response =
        newBuilder()
            .build()
            .send(
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            "http://localhost:"
                                + port
                                + "/sykefravarsstatistikk-api"
                                + "/eksportering/reeksport/statistikkkategori?"
                                + "årstall="
                                + SISTE_PUBLISERTE_KVARTAL.getÅrstall()
                                + "&kvartal="
                                + SISTE_PUBLISERTE_KVARTAL.getKvartal()
                                + "&kategori=VIRKSOMHET"
                                + "&begrensningTil=10"))
                    .header(AUTHORIZATION, getBearerMedJwt())
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build(),
                ofString());

    assertThat(response.statusCode()).isEqualTo(200);
  }

  @NotNull
  private String getBearerMedJwt() {
    return "Bearer "
        + TestTokenUtil.createToken(mockOAuth2Server, "15008462396", ISSUER_TOKENX, "");
  }
}

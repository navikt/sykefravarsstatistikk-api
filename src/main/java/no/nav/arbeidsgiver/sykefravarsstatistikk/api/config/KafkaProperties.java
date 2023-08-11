package no.nav.arbeidsgiver.sykefravarsstatistikk.api.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.KafkaUtsendingRapport;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Statistikkategori;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@ConfigurationProperties(prefix = "kafka.outbound")
@Component
public class KafkaProperties {

  private List<String> topic;
  private String bootstrapServers;
  private String caPath;
  private String truststorePath;
  private String keystorePath;
  private String credstorePassword;
  private String securityProtocol;

  private final String acks = "1";
  private final String clientId = "sykefravarsstatistikk-api";
  private final String valueSerializerClass = StringSerializer.class.getName();
  private final String keySerializerCLass = StringSerializer.class.getName();
  private final Integer retries = 10;
  private final Integer deliveryTimeoutMs = 120000; // 2 min (default)
  private final Integer requestTimeoutMs = 10000;
  private final Integer lingerMs = 100;
  private final Integer batchSize =
      16384
          * 10; // størrelse av en melding er mellom 1000 bytes og 20K bytes (virksomhet med 70+
  // 5siffer næringskoder)
  private final Integer maxInFlightRequestsPerConnection = 5; // default
  public static final String EKSPORT_ALLE_KATEGORIER = "ALLE_KATEGORIER";

  public Map<String, Object> asProperties() {
    HashMap<String, Object> props = new HashMap<>();

    if (bootstrapServers != null) {
      props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    }
    if (credstorePassword != null) {
      props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, credstorePassword);
      props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, credstorePassword);
      props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, credstorePassword);
      props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "JKS");
      props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12");
    }
    if (truststorePath != null) {
      props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststorePath);
    }
    if (keystorePath != null) {
      props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystorePath);
    }
    if (securityProtocol != null) {
      props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
    }
    props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializerClass);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializerCLass);

    props.put(ProducerConfig.RETRIES_CONFIG, retries);
    props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeoutMs);
    props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);
    props.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
    props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
    props.put(ProducerConfig.ACKS_CONFIG, acks);
    props.put(
        ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, maxInFlightRequestsPerConnection);

    return props;
  }

  public String getTopicNavn(String eksportNavn) {
    if (Statistikkategori.LAND.name().equals(eksportNavn) && topic.size() > 1) {
      return topic.get(1);
    } else if (Statistikkategori.VIRKSOMHET.name().equals(eksportNavn) && topic.size() > 2) {
      return topic.get(2);
    } else if (Statistikkategori.NÆRING.name().equals(eksportNavn) && topic.size() > 3) {
      return topic.get(3);
    } else if (Statistikkategori.SEKTOR.name().equals(eksportNavn) && topic.size() > 4) {
      return topic.get(4);
    } else {
      return topic.get(0);
    }
  }

  @Bean
  @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
  public KafkaUtsendingRapport getKafkaUtsendingReport() {
    return new KafkaUtsendingRapport();
  }
}
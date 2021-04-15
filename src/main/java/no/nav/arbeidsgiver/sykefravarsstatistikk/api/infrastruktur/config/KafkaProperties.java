package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaUtsendingRapport;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@ConfigurationProperties(prefix = "kafka.outbound")
@Component
public class KafkaProperties {
	private String topic;
	private String bootstrapServers;
	private String caPath;
	private String truststorePath;
	private String keystorePath;
	private String credstorePassword;
	private String securityProtocol;

	private final String acks = "all";
	private final String clientId = "sykefravarsstatistikk-api";
	private final String valueSerializerClass = StringSerializer.class.getName();
	private final String keySerializerCLass = StringSerializer.class.getName();
	private final Integer retries = Integer.MAX_VALUE;
	private final Integer deliveryTimeoutMs = 10100;
	private final Integer requestTimeoutMs = 10000;
	private final Integer lingerMs = 100;
	private final Integer batchSize = 16384*4;

	public Map<String, Object> asProperties() {
		HashMap<String, Object> props = new HashMap<>();

		if(bootstrapServers != null){
			props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		}
		if(credstorePassword != null) {
			props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, credstorePassword);
			props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, credstorePassword);
			props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, credstorePassword);
			props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "JKS");
			props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12");
		}
		if(truststorePath != null){
			props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststorePath);
		}
		if(keystorePath != null){
			props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystorePath);
		}
		if(securityProtocol != null) {
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

		return props;
	}

	@Bean
	@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
	public KafkaUtsendingRapport getKafkaUtsendingReport() {
		return new KafkaUtsendingRapport();
	}

}
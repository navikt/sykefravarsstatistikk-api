package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "kafka.outbound")
@ConstructorBinding
@AllArgsConstructor
@NoArgsConstructor
@Data
public class KafkaProperties {
	String topic;
	String bootstrapServers;
	String caPath;
	String truststorePath;
	String keystorePath;
	String credstorePassword;
	final String acks = "all";
	String securityProtocol;
	final String clientId = "sykefravarsstatistikk-api";
	final String valueSerializerClass = StringSerializer.class.getName();
	final String keySerializerCLass = StringSerializer.class.getName();
	final Integer retries = Integer.MAX_VALUE;
	final Integer deliveryTimeoutMs = 10100;
	final Integer requestTimeoutMs = 10000;
	final Integer lingerMs = 100;
	final Integer batchSize = 16384*4;

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


}
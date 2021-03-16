package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config;

import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
class KafkaConfig {
	@Bean
	KafkaTemplate<String, String> kafkaTemplate(KafkaProperties kafkaProperties) {
		return new KafkaTemplate<String, String>(
				new DefaultKafkaProducerFactory<String, String>(kafkaProperties.asProperties())
		);
	}
}
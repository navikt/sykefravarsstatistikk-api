package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.concurrent.ListenableFuture;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class KafkaService {
	private final static ObjectMapper objectMapper = new ObjectMapper();

	KafkaTemplate<String, String> kafkaTemplate;
	KafkaProperties kafkaProperties;

	KafkaService(KafkaTemplate<String, String> kafkaTemplate, KafkaProperties kafkaProperties){
		this.kafkaTemplate = kafkaTemplate;
		this.kafkaProperties = kafkaProperties;
	}

	public void send(Object object) throws JsonProcessingException {
		ListenableFuture<SendResult<String, String>> futureResult =
				kafkaTemplate.send(kafkaProperties.getTopic(),
						objectMapper.writeValueAsString(object)
				);
		futureResult.addCallback(
				(result) -> {
					log.info("Melding sendt på topic");
					try {
						JsonNode keyJson = objectMapper.readTree(result.getProducerRecord().key());
						String orgnr = keyJson.hasNonNull("orgnr") ? keyJson.get("orgnr").asText() : null;
						Integer kvartal = keyJson.hasNonNull("kvartal") ? keyJson.get("kvartal").asInt() : null;
						Integer årstall = keyJson.hasNonNull("årstall") ? keyJson.get("årstall").asInt() : null;

					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
					result.getProducerRecord().value();

				},
				(exception) -> log.error("Feil oppstod ved sending av melding", exception)
		);
	}
}

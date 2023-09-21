package no.nav.arbeidsgiver.sykefravarsstatistikk.api.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.ProducerListener;

@Slf4j
@Configuration
class KafkaConfig {
  @Bean
  KafkaTemplate<String, String> kafkaTemplate(KafkaProperties kafkaProperties) {
    KafkaTemplate<String, String> kafkaTemplate =
        new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(kafkaProperties.asProperties()));
    kafkaTemplate.setProducerListener(getProducerListener());
    return kafkaTemplate;
  }

  ProducerListener<String, String> getProducerListener() {
    return new ProducerListener<>() {
      @Override
      public void onSuccess(
          ProducerRecord<String, String> producerRecord, RecordMetadata recordMetadata) {
        log.debug(
            "ProducerListener mottok success for record med offset '{}' på topic '{}'",
            recordMetadata.topic(),
            recordMetadata.offset());
      }

      @Override
      public void onError(
          ProducerRecord<String, String> producerRecord,
          RecordMetadata recordMetadata,
          Exception exception) {
        String topicNavn =
            recordMetadata != null
                ? recordMetadata.topic()
                : "Ingen topic funnet (recordMetadat er null)";
        long offset = recordMetadata != null ? recordMetadata.offset() : 0;
        log.info(
            "ProducerListener mottok en exception med melding '{}' for record med offset '{}' på topic '{}'",
            exception.getMessage(),
            topicNavn,
            offset);
      }
    };
  }
}

package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KafkaTopicKey {
  String orgnr;
  int kvartal;
  int årstall;
}

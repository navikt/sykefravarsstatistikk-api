package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KafkaTopicKey {
    String orgnr;
    int kvartal;
    int årstall;
}

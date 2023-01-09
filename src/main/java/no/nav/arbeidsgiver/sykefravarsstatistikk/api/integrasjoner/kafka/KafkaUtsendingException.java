package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

public class KafkaUtsendingException extends RuntimeException {

  public KafkaUtsendingException(String melding) {
    super(melding);
  }
}

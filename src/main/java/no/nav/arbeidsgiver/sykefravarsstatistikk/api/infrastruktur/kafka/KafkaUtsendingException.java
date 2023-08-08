package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka;

public class KafkaUtsendingException extends RuntimeException {

  public KafkaUtsendingException(String melding) {
    super(melding);
  }
}

package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import java.time.LocalDateTime;

public class KafkaUtsendingHistorikkData {

  public String orgnr;
  public String key;
  public String value;
  public LocalDateTime opprettet;

  public KafkaUtsendingHistorikkData(
      String orgnr,
      String key,
      String value,
      LocalDateTime opprettet
  ) {
    this.orgnr = orgnr;
    this.key = key;
    this.value = value;
    this.opprettet = opprettet;
  }
}

package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KafkaUtsendingRapportTest {

  @Test
  public void addProcessingTime__gir_mulighet_til_å_beregne_snitt_prosesseringstid() {
    KafkaUtsendingRapport rapport = new KafkaUtsendingRapport();
    rapport.reset(2);

    rapport.addUtsendingTilKafkaProcessingTime(500, 550);
    rapport.addDBOppdateringProcessingTime(8000, 8800);
    rapport.addUtsendingTilKafkaProcessingTime(1500, 1530);
    rapport.addDBOppdateringProcessingTime(2000, 3000);

    assertEquals(40, rapport.getSnittTidUtsendingTilKafka());
    assertEquals(900, rapport.getSnittTidOppdateringIDB());
  }
}

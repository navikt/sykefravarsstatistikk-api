package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KafkaUtsendingRapportTest {

    @Test
    public void addProcessingTime__gir_mulighet_til_å_beregne_snitt_prosesseringstid() {
        KafkaUtsendingRapport rapport = new KafkaUtsendingRapport();
        rapport.reset(2);

        rapport.addProcessingTime(500, 550, 8000, 8800);
        rapport.addProcessingTime(1500, 1530, 2000, 3000);

        assertEquals(40, rapport.getSnittTidUtsendingTilKafka());
        assertEquals(900, rapport.getSnittTidOppdateringIDB());
    }
}
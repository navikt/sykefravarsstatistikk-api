package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class KafkaUtsendingRapportTest {
    @Test
    fun addProcessingTime__gir_mulighet_til_å_beregne_snitt_prosesseringstid() {
        val rapport = KafkaUtsendingRapport()
        rapport.reset(2)
        rapport.addUtsendingTilKafkaProcessingTime(500, 550)
        rapport.addDBOppdateringProcessingTime(8000, 8800)
        rapport.addUtsendingTilKafkaProcessingTime(1500, 1530)
        rapport.addDBOppdateringProcessingTime(2000, 3000)
        Assertions.assertEquals(40, rapport.snittTidUtsendingTilKafka)
        Assertions.assertEquals(900, rapport.snittTidOppdateringIDB)
    }
}

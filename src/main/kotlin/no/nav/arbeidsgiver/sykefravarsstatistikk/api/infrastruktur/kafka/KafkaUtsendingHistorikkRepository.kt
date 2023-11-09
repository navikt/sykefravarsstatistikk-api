package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.UsingExposed
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.datetime
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
open class KafkaUtsendingHistorikkRepository(
    override val database: Database,
): UsingExposed, Table("kafka_utsending_historikk") {
    val orgnr = text("orgnr")
    val key = text("key_json")
    val value = text("value_json")
    val opprettet = datetime("opprettet").default(LocalDateTime.now())

    fun opprettHistorikk(inOrgnr: String, inKey: String, inValue: String) {
        transaction {
            insert {
                it[orgnr] = inOrgnr
                it[key] = inKey
                it[value] = inValue
            }
        }
    }

    fun slettHistorikk(): Int {
        return transaction {
            deleteAll()
        }
    }
}

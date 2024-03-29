package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Importtidspunkt
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ImporttidspunktRepository(
    @param:Qualifier("sykefravarsstatistikkDatabase") override val database: Database
) : UsingExposed, Table("importtidspunkt") {

    private val log = LoggerFactory.getLogger(this::class.java)

    val årstall = varchar("aarstall", length = 4)
    val kvartal = varchar("kvartal", length = 1)
    val importert = date("importert")

    fun settInnImporttidspunkt(
        årstallOgKvartal: ÅrstallOgKvartal,
        importertTidspunkt: LocalDate = LocalDate.now()
    ) {
        log.info("Oppdaterer tidspunkt for import av sykefraværstatistikk for $årstallOgKvartal")

        val antallOppdatert = transaction {
            insert {
                it[årstall] = årstallOgKvartal.årstall.toString()
                it[kvartal] = årstallOgKvartal.kvartal.toString()
                it[importert] = importertTidspunkt
            }
        }
        log.info("Opprettet $antallOppdatert rader i importtidspunkt-tabellen")
    }


    fun hentNyesteImporterteKvartal(): Importtidspunkt? {
        return transaction {
            selectAll()
                .orderBy(årstall to SortOrder.DESC)
                .orderBy(kvartal to SortOrder.DESC)
                .limit(1)
                .map {
                    Importtidspunkt(
                        sistImportertTidspunkt = it[importert],
                        gjeldendePeriode = ÅrstallOgKvartal(it[årstall].toInt(), it[kvartal].toInt())
                    )
                }.firstOrNull()
        }
    }
}
package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoer.Publiseringsdato
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class PubliseringsdatoerRepository(
    @param:Qualifier("sykefravarsstatistikkDatabase") override val database: Database
) : UsingExposed, Table("publiseringsdatoer") {

    val rapportPeriode = integer("rapport_periode")
    val offentligDato = date("offentlig_dato")
    val oppdatertIDvh = date("oppdatert_i_dvh")

    private val log = LoggerFactory.getLogger(this::class.java)

    fun hentPubliseringsdatoer(): List<Publiseringsdato> {
        return transaction {
            selectAll()
                .map {
                    Publiseringsdato(
                        rapportPeriode = it[rapportPeriode],
                        offentligDato = it[offentligDato],
                        oppdatertDato = it[oppdatertIDvh],
                    )
                }
        }
    }

    fun overskrivPubliseringsdatoer(data: List<Publiseringsdato>) {
        val antallRaderSlettet = transaction { deleteAll() }
        log.info("Antall rader slettet fra 'publiseringsdatoer': $antallRaderSlettet")

        val antallRaderSattInn = transaction {
            batchInsert(data, shouldReturnGeneratedValues = false) {
                this[rapportPeriode] = it.rapportPeriode
                this[offentligDato] = it.offentligDato
                this[oppdatertIDvh] = it.oppdatertDato
            }
        }.count()
        log.info("Antall rader satt inn i 'publiseringsdatoer': $antallRaderSattInn")
    }
}

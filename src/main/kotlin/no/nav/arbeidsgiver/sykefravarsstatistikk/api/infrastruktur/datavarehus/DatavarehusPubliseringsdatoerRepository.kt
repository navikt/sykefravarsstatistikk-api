package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoer.Publiseringsdato
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.UsingExposed
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component
open class DatavarehusPubliseringsdatoerRepository(
    @param:Qualifier("datavarehusJdbcTemplate") private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate, // Fjerning av denne knekker testene...
    @param:Qualifier("datavarehusDatabase") override val database: Database,
): UsingExposed, Table("dk_p.publiseringstabell") {
    val rapportPeriode = integer("rapport_periode")
    val offentligDato = date("offentlig_dato")
    val oppdatertDato = date("oppdatert_dato")
    val tabellNavn = text("TABELL_NAVN")
    val periodeType = text("PERIODE_TYPE")

    open fun hentPubliseringsdatoerFraDvh(): List<Publiseringsdato> {
        return transaction {
            select(
                rapportPeriode,
                offentligDato,
                oppdatertDato,
            ).where {
                (tabellNavn eq "AGG_FAK_SYKEFRAVAR_DIA") and (periodeType eq "KVARTAL")
            }.orderBy(offentligDato to SortOrder.DESC)
                .map {
                    Publiseringsdato(
                        rapportPeriode = it[rapportPeriode],
                        offentligDato = it[offentligDato],
                        oppdatertDato = it[oppdatertDato],
                    )
                }

        }
    }
}

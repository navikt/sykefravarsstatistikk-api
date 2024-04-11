package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.VirksomhetMetadata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.jetbrains.exposed.sql.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
open class VirksomhetMetadataRepository(
    @param:Qualifier("sykefravarsstatistikkDatabase") override val database: Database,
) : UsingExposed, Table("virksomhet_metadata") {
    private val orgnr = text("orgnr")
    private val navn = text("navn")
    private val rectype = varchar("rectype", 1)
    private val sektor = text("sektor")
    private val primærnæring = varchar("primarnaring", 2)
    private val primærnæringskode = varchar("primarnaringskode", 5)
    private val arstall = integer("arstall")
    private val kvartal = integer("kvartal")

    open fun opprettVirksomhetMetadata(virksomhetMetadata: List<VirksomhetMetadata>): Int {
        return transaction {
            batchInsert(virksomhetMetadata, shouldReturnGeneratedValues = false) {
                this[orgnr] = it.orgnr
                this[navn] = it.navn
                this[rectype] = it.rectype
                this[sektor] = it.sektor.sektorkode
                this[primærnæring] = it.primærnæring
                this[primærnæringskode] = it.primærnæringskode
                this[arstall] = it.årstall
                this[kvartal] = it.kvartal
            }.count()
        }
    }

    open fun hentVirksomhetMetadata(årstallOgKvartal: ÅrstallOgKvartal): List<VirksomhetMetadata> {
        return transaction {
            selectAll()
                .where {
                    (arstall eq årstallOgKvartal.årstall) and (kvartal eq årstallOgKvartal.kvartal)
                }.map {
                    VirksomhetMetadata(
                        Orgnr(it[orgnr]),
                        it[navn],
                        it[rectype],
                        Sektor.fraSektorkode(it[sektor])!!,
                        it[primærnæring],
                        it[primærnæringskode],
                        ÅrstallOgKvartal(
                            it[arstall], it[kvartal]
                        )
                    )
                }
        }
    }

    open fun slettVirksomhetMetadata(): Int = transaction {
        deleteAll()
    }
}

package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

interface UsingExposed {
    val database: Database

    fun <T> transaction(statement: Transaction.() -> T): T = transaction(database, statement)
}

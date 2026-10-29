// src/nl/brul/api/database/TransactionRunner.kt
package nl.brul.api.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Laag: database (de verbinding met H2).
 *
 * Voert een stuk werk uit als één database-transactie: alles wordt opgeslagen, of niets.
 * Services gebruiken deze interface in plaats van Exposed direct aan te roepen. Zo blijft de
 * domeinlogica te testen zonder database. Repository-aanroepen binnen het blok doen mee in
 * dezelfde transactie.
 * `<T>` is een generic: het blok mag elk soort resultaat teruggeven, en `inTransaction` geeft
 * precies dat type terug.
 */
interface TransactionRunner {
    fun <T> inTransaction(block: () -> T): T
}

/** De echte implementatie, met een transactie van Exposed. */
class ExposedTransactionRunner(
    private val database: Database
) : TransactionRunner {

    override fun <T> inTransaction(block: () -> T): T =
        transaction(database) {
            block()
        }
}

/** Voor tests met nep-repositories in het geheugen: voert het blok gewoon uit. */
object NoTransactionRunner : TransactionRunner {
    override fun <T> inTransaction(block: () -> T): T = block()
}

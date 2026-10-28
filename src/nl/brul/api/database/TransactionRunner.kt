// src/nl/brul/api/database/TransactionRunner.kt
package nl.brul.api.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Runs a block of work as one database transaction.
 *
 * Services use this interface instead of calling Exposed directly, so the
 * domain logic stays testable without a database. Repository calls made
 * inside the block join the same transaction: either everything is saved,
 * or nothing is.
 */
interface TransactionRunner {
    fun <T> inTransaction(block: () -> T): T
}

/** Production implementation backed by an Exposed transaction. */
class ExposedTransactionRunner(
    private val database: Database
) : TransactionRunner {

    override fun <T> inTransaction(block: () -> T): T =
        transaction(database) {
            block()
        }
}

/** Test implementation for in-memory fakes: simply runs the block. */
object NoTransactionRunner : TransactionRunner {
    override fun <T> inTransaction(block: () -> T): T = block()
}

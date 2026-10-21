// src/nl/brul/api/features/user/repository/ExposedUserRepository.kt
package nl.brul.api.features.user.repository

import nl.brul.api.features.user.model.NewUser
import nl.brul.api.features.user.model.User
import nl.brul.api.features.user.model.UserRole
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.Locale

class ExposedUserRepository(
    private val database: Database,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis
) : UserRepository {

    override fun create(user: NewUser): User = transaction(database) {
        val normalizedEmail = user.email.lowercase(Locale.ROOT)
        val timestamp = currentTimeMillis()

        require(timestamp > 0) {
            "Creation timestamp must be positive."
        }

        require(findByEmail(normalizedEmail) == null) {
            "Email is already registered."
        }

        val id = UsersTable.insert {
            it[username] = user.username
            it[email] = normalizedEmail
            it[passwordHash] = user.passwordHash
            it[createdAt] = timestamp
        } get UsersTable.id

        requireNotNull(findById(id)) {
            "Created user could not be retrieved."
        }
    }

    override fun findById(id: Long): User? = transaction(database) {
        UsersTable
            .selectAll()
            .where { UsersTable.id eq id }
            .singleOrNull()
            ?.toUser()
    }

    override fun findByEmail(email: String): User? = transaction(database) {
        val normalizedEmail = email.lowercase(Locale.ROOT)

        UsersTable
            .selectAll()
            .where { UsersTable.email eq normalizedEmail }
            .singleOrNull()
            ?.toUser()
    }

    override fun findAll(): List<User> = transaction(database) {
        UsersTable
            .selectAll()
            .orderBy(UsersTable.id)
            .map { it.toUser() }
    }

    override fun updateRole(id: Long, role: UserRole): User? =
        transaction(database) {
            val updatedRows = UsersTable.update(
                where = { UsersTable.id eq id }
            ) {
                it[UsersTable.role] = role
            }

            if (updatedRows == 0) {
                null
            } else {
                findById(id)
            }
        }

    private fun ResultRow.toUser(): User = User(
        id = this[UsersTable.id],
        username = this[UsersTable.username],
        email = this[UsersTable.email],
        passwordHash = this[UsersTable.passwordHash],
        createdAt = this[UsersTable.createdAt],
        role = this[UsersTable.role]
    )
}
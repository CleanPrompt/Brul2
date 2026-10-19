// src/nl/brul/api/features/user/repository/UsersTable.kt
package nl.brul.api.features.user.repository

import org.jetbrains.exposed.sql.Table

object UsersTable : Table("users") {

    val id = long("id").autoIncrement()

    val username = varchar("username", 50)

    val email = varchar("email", 254).uniqueIndex()

    val passwordHash = varchar("password_hash", 255)

    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}
// src/nl/brul/api/security/PasswordHasher.kt
package nl.brul.api.security

interface PasswordHasher {

    fun hash(password: String): String

    fun verify(password: String, passwordHash: String): Boolean
}
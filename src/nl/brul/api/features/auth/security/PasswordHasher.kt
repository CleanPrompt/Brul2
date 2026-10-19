// src/nl/brul/api/features/auth/security/PasswordHasher.kt
package nl.brul.api.features.auth.security

interface PasswordHasher {

    fun hash(password: String): String

    fun verify(password: String, passwordHash: String): Boolean
}
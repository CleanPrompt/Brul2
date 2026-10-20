// src/nl/brul/api/security/BCryptPasswordHasher.kt
package nl.brul.api.security

import org.mindrot.jbcrypt.BCrypt

class BCryptPasswordHasher(
    private val workFactor: Int = 12
) : PasswordHasher {

    init {
        require(workFactor in 10..16) {
            "BCrypt work factor must be between 10 and 16."
        }
    }

    override fun hash(password: String): String {
        require(password.isNotBlank()) {
            "Password cannot be blank."
        }

        require(password.toByteArray(Charsets.UTF_8).size <= 72) {
            "Password cannot exceed 72 UTF-8 bytes."
        }

        return BCrypt.hashpw(
            password,
            BCrypt.gensalt(workFactor)
        )
    }

    override fun verify(
        password: String,
        passwordHash: String
    ): Boolean {
        if (password.isBlank()) {
            return false
        }

        if (password.toByteArray(Charsets.UTF_8).size > 72) {
            return false
        }

        return try {
            BCrypt.checkpw(password, passwordHash)
        } catch (_: IllegalArgumentException) {
            false
        }
    }
}
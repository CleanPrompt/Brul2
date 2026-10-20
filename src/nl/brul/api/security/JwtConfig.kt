// src/nl/brul/api/security/JwtConfig.kt
package nl.brul.api.security

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val validityMillis: Long
) {
    init {
        require(secret.toByteArray(Charsets.UTF_8).size >= 32) {
            "JWT secret must contain at least 32 UTF-8 bytes."
        }
        require(issuer.isNotBlank()) {
            "JWT issuer cannot be blank."
        }
        require(audience.isNotBlank()) {
            "JWT audience cannot be blank."
        }
        require(validityMillis > 0) {
            "JWT validity must be positive."
        }
    }
}
// src/nl/brul/api/features/user/model/User.kt
package nl.brul.api.features.user.model

data class User(
    val id: Long,
    val username: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Long,
    val role: UserRole = UserRole.USER
) {
    init {
        require(id > 0) {
            "User ID must be positive."
        }
        require(username.isNotBlank()) {
            "Username cannot be blank."
        }
        require(username.length <= 50) {
            "Username cannot exceed 50 characters."
        }
        require(email.isNotBlank() && email == email.trim() && '@' in email) {
            "Email must be valid."
        }
        require(email.length <= 254) {
            "Email cannot exceed 254 characters."
        }
        require(passwordHash.isNotBlank()) {
            "Password hash cannot be blank."
        }
        require(createdAt > 0) {
            "Creation timestamp must be positive."
        }
    }
}
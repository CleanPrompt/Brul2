// src/nl/brul/api/features/user/repository/NewUser.kt
package nl.brul.api.features.user.repository

data class NewUser(
    val username: String,
    val email: String,
    val passwordHash: String
) {
    init {
        require(username.isNotBlank() && username.length <= 50) {
            "Username must contain 1 to 50 characters."
        }
        require(email.isNotBlank() && email == email.trim() && '@' in email && email.length <= 254) {
            "Email must be valid."
        }
        require(passwordHash.isNotBlank()) {
            "Password hash cannot be blank."
        }
    }
}
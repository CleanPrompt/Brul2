// src/nl/brul/api/features/auth/service/AuthService.kt
package nl.brul.api.features.auth.service

import nl.brul.api.features.user.model.User
import nl.brul.api.features.user.model.NewUser
import nl.brul.api.features.user.repository.UserRepository
import nl.brul.api.security.PasswordHasher
import java.util.Locale

class AuthService(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher
) {

    fun register(
        username: String,
        email: String,
        password: String
    ): User {
        val normalizedUsername = username.trim()
        val normalizedEmail = email.trim().lowercase(Locale.ROOT)

        require(normalizedUsername.isNotBlank()) {
            "Username cannot be blank."
        }
        require(normalizedUsername.length <= 50) {
            "Username cannot exceed 50 characters."
        }
        require(normalizedEmail.isNotBlank() && '@' in normalizedEmail) {
            "Email address is invalid."
        }
        require(normalizedEmail.length <= 254) {
            "Email cannot exceed 254 characters."
        }
        require(password.isNotBlank()) {
            "Password cannot be blank."
        }
        require(password.toByteArray(Charsets.UTF_8).size <= 72) {
            "Password cannot exceed 72 UTF-8 bytes."
        }

        if (userRepository.findByEmail(normalizedEmail) != null) {
            throw EmailAlreadyRegisteredException()
        }

        val passwordHash = passwordHasher.hash(password)

        return userRepository.create(
            NewUser(
                username = normalizedUsername,
                email = normalizedEmail,
                passwordHash = passwordHash
            )
        )
    }

    fun login(
        email: String,
        password: String
    ): User {
        val normalizedEmail = email.trim().lowercase(Locale.ROOT)

        val user = userRepository.findByEmail(normalizedEmail)
            ?: throw InvalidCredentialsException()

        if (!passwordHasher.verify(password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }

        return user
    }
}
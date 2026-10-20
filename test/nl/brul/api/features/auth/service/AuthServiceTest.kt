// test/nl/brul/api/features/auth/service/AuthServiceTest.kt
package nl.brul.api.features.auth.service

import nl.brul.api.features.user.model.User
import nl.brul.api.features.user.model.NewUser
import nl.brul.api.features.user.repository.UserRepository
import nl.brul.api.security.PasswordHasher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthServiceTest {

    private class FakeUserRepository : UserRepository {
        private val users = mutableMapOf<Long, User>()
        private var nextId = 1L

        override fun create(user: NewUser): User {
            require(findByEmail(user.email) == null) {
                "Email address is already registered."
            }

            val createdUser = User(
                id = nextId++,
                username = user.username,
                email = user.email,
                passwordHash = user.passwordHash,
                createdAt = 1_700_000_000_000L
            )

            users[createdUser.id] = createdUser
            return createdUser
        }

        override fun findById(id: Long): User? {
            return users[id]
        }

        override fun findByEmail(email: String): User? {
            return users.values.firstOrNull {
                it.email.equals(email, ignoreCase = true)
            }
        }
    }

    private class FakePasswordHasher : PasswordHasher {

        override fun hash(password: String): String {
            return "hashed:$password"
        }

        override fun verify(
            password: String,
            passwordHash: String
        ): Boolean {
            return hash(password) == passwordHash
        }
    }

    private val repository = FakeUserRepository()
    private val passwordHasher = FakePasswordHasher()
    private val service = AuthService(repository, passwordHasher)

    @Test
    fun `register creates a user`() {
        val user = service.register(
            username = "Alice",
            email = "alice@example.com",
            password = "SecurePassword123"
        )

        assertEquals(1L, user.id)
        assertEquals("Alice", user.username)
        assertEquals("alice@example.com", user.email)
        assertNotNull(repository.findById(user.id))
    }

    @Test
    fun `register hashes the password before storing it`() {
        val user = service.register(
            username = "Alice",
            email = "alice@example.com",
            password = "SecurePassword123"
        )

        assertNotEquals("SecurePassword123", user.passwordHash)
        assertEquals("hashed:SecurePassword123", user.passwordHash)
    }

    @Test
    fun `register normalizes username and email`() {
        val user = service.register(
            username = "  Alice  ",
            email = "  ALICE@EXAMPLE.COM  ",
            password = "SecurePassword123"
        )

        assertEquals("Alice", user.username)
        assertEquals("alice@example.com", user.email)
    }

    @Test
    fun `register rejects duplicate email addresses`() {
        service.register(
            username = "Alice",
            email = "alice@example.com",
            password = "SecurePassword123"
        )

        assertFailsWith<EmailAlreadyRegisteredException> {
            service.register(
                username = "Bob",
                email = "ALICE@EXAMPLE.COM",
                password = "AnotherPassword123"
            )
        }
    }

    @Test
    fun `register rejects blank username`() {
        assertFailsWith<IllegalArgumentException> {
            service.register(
                username = "   ",
                email = "alice@example.com",
                password = "SecurePassword123"
            )
        }
    }

    @Test
    fun `register rejects invalid email`() {
        assertFailsWith<IllegalArgumentException> {
            service.register(
                username = "Alice",
                email = "invalid-email",
                password = "SecurePassword123"
            )
        }
    }

    @Test
    fun `register rejects blank password`() {
        assertFailsWith<IllegalArgumentException> {
            service.register(
                username = "Alice",
                email = "alice@example.com",
                password = "   "
            )
        }
    }

    @Test
    fun `register rejects passwords exceeding 72 UTF-8 bytes`() {
        assertFailsWith<IllegalArgumentException> {
            service.register(
                username = "Alice",
                email = "alice@example.com",
                password = "a".repeat(73)
            )
        }
    }

    @Test
    fun `register rejects usernames exceeding 50 characters`() {
        assertFailsWith<IllegalArgumentException> {
            service.register(
                username = "a".repeat(51),
                email = "alice@example.com",
                password = "SecurePassword123"
            )
        }
    }

    @Test
    fun `login accepts valid credentials`() {
        val registeredUser = service.register(
            username = "Alice",
            email = "alice@example.com",
            password = "SecurePassword123"
        )

        val loggedInUser = service.login(
            email = "alice@example.com",
            password = "SecurePassword123"
        )

        assertEquals(registeredUser.id, loggedInUser.id)
    }

    @Test
    fun `login normalizes email addresses`() {
        service.register(
            username = "Alice",
            email = "alice@example.com",
            password = "SecurePassword123"
        )

        val user = service.login(
            email = "  ALICE@EXAMPLE.COM  ",
            password = "SecurePassword123"
        )

        assertEquals("alice@example.com", user.email)
    }

    @Test
    fun `login rejects incorrect passwords`() {
        service.register(
            username = "Alice",
            email = "alice@example.com",
            password = "SecurePassword123"
        )

        assertFailsWith<InvalidCredentialsException> {
            service.login(
                email = "alice@example.com",
                password = "WrongPassword"
            )
        }
    }

    @Test
    fun `login rejects unknown email addresses`() {
        assertFailsWith<InvalidCredentialsException> {
            service.login(
                email = "unknown@example.com",
                password = "SecurePassword123"
            )
        }
    }

    @Test
    fun `login uses the same error message for unknown email and wrong password`() {
        service.register(
            username = "Alice",
            email = "alice@example.com",
            password = "SecurePassword123"
        )

        val wrongPassword = assertFailsWith<InvalidCredentialsException> {
            service.login(
                email = "alice@example.com",
                password = "WrongPassword"
            )
        }

        val unknownEmail = assertFailsWith<InvalidCredentialsException> {
            service.login(
                email = "unknown@example.com",
                password = "SecurePassword123"
            )
        }

        assertEquals(wrongPassword.message, unknownEmail.message)
    }

    @Test
    fun `registered user can be found in the repository`() {
        val user = service.register(
            username = "Alice",
            email = "alice@example.com",
            password = "SecurePassword123"
        )

        val storedUser = repository.findByEmail("alice@example.com")

        assertNotNull(storedUser)
        assertEquals(user.id, storedUser.id)
        assertTrue(storedUser.passwordHash.startsWith("hashed:"))
        assertFalse(storedUser.passwordHash == "SecurePassword123")
    }
}
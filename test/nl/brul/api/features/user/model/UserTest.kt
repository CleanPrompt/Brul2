// test/nl/brul/api/features/user/model/UserTest.kt
package nl.brul.api.features.user.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UserTest {

    @Test
    fun `valid user is created`() {
        val user = User(
            id = 1,
            username = "Alice",
            email = "alice@example.com",
            passwordHash = "stored-test-hash",
            createdAt = 1000L
        )

        assertEquals(1L, user.id)
        assertEquals("Alice", user.username)
    }

    @Test
    fun `user ID must be positive`() {
        assertFailsWith<IllegalArgumentException> {
            User(
                id = 0,
                username = "Alice",
                email = "alice@example.com",
                passwordHash = "stored-test-hash",
                createdAt = 1000L
            )
        }
    }

    @Test
    fun `user email cannot be blank`() {
        assertFailsWith<IllegalArgumentException> {
            User(
                id = 1,
                username = "Alice",
                email = "",
                passwordHash = "stored-test-hash",
                createdAt = 1000L
            )
        }
    }

    @Test
    fun `password hash cannot be blank`() {
        assertFailsWith<IllegalArgumentException> {
            User(
                id = 1,
                username = "Alice",
                email = "alice@example.com",
                passwordHash = "",
                createdAt = 1000L
            )
        }
    }

    @Test
    fun `creation timestamp must be positive`() {
        assertFailsWith<IllegalArgumentException> {
            User(
                id = 1,
                username = "Alice",
                email = "alice@example.com",
                passwordHash = "stored-test-hash",
                createdAt = 0L
            )
        }
    }
}
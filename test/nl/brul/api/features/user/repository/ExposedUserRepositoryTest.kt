// test/nl/brul/api/features/user/repository/ExposedUserRepositoryTest.kt
package nl.brul.api.features.user.repository

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ExposedUserRepositoryTest {

    private fun createRepository(): ExposedUserRepository {
        val database = Database.connect(
            url = "jdbc:h2:mem:user_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver"
        )

        transaction(database) {
            SchemaUtils.create(UsersTable)
        }

        return ExposedUserRepository(
            database = database,
            currentTimeMillis = { 123456789L }
        )
    }

    private fun newUser(
        email: String = "alice@example.com"
    ) = NewUser(
        username = "Alice",
        email = email,
        passwordHash = "stored-test-hash"
    )

    @Test
    fun `create user stores data in H2`() {
        val repository = createRepository()

        val user = repository.create(newUser())

        assertEquals(1L, user.id)
        assertEquals("Alice", user.username)
        assertEquals("alice@example.com", user.email)
        assertEquals("stored-test-hash", user.passwordHash)
        assertEquals(123456789L, user.createdAt)
    }

    @Test
    fun `find user by ID returns stored user`() {
        val repository = createRepository()
        val created = repository.create(newUser())

        val found = repository.findById(created.id)

        assertNotNull(found)
        assertEquals(created, found)
    }

    @Test
    fun `find user by email is case insensitive`() {
        val repository = createRepository()
        repository.create(newUser("Alice@Example.com"))

        val found = repository.findByEmail("ALICE@EXAMPLE.COM")

        assertNotNull(found)
        assertEquals("alice@example.com", found.email)
    }

    @Test
    fun `missing user returns null`() {
        val repository = createRepository()

        assertNull(repository.findById(999))
        assertNull(repository.findByEmail("missing@example.com"))
    }

    @Test
    fun `duplicate email is rejected`() {
        val repository = createRepository()
        repository.create(newUser("alice@example.com"))

        assertFailsWith<IllegalArgumentException> {
            repository.create(newUser("ALICE@example.com"))
        }
    }

    @Test
    fun `multiple users receive different IDs`() {
        val repository = createRepository()

        val first = repository.create(newUser("alice@example.com"))
        val second = repository.create(newUser("bob@example.com"))

        assertEquals(1L, first.id)
        assertEquals(2L, second.id)
    }
}
// test/nl/brul/api/security/BCryptPasswordHasherTest.kt
package nl.brul.api.security

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BCryptPasswordHasherTest {

    // A lower work factor keeps the unit tests fast.
    private val hasher = BCryptPasswordHasher(workFactor = 10)

    @Test
    fun `hashing produces a value different from the password`() {
        val password = "MySecurePassword123!"

        val hash = hasher.hash(password)

        assertNotEquals(password, hash)
        assertTrue(hash.startsWith("\$2a\$"))
    }

    @Test
    fun `correct password matches stored hash`() {
        val password = "MySecurePassword123!"
        val hash = hasher.hash(password)

        assertTrue(hasher.verify(password, hash))
    }

    @Test
    fun `incorrect password does not match`() {
        val hash = hasher.hash("CorrectPassword123!")

        assertFalse(
            hasher.verify("WrongPassword123!", hash)
        )
    }

    @Test
    fun `hashing same password twice produces different hashes`() {
        val password = "MySecurePassword123!"

        val firstHash = hasher.hash(password)
        val secondHash = hasher.hash(password)

        assertNotEquals(firstHash, secondHash)

        assertTrue(hasher.verify(password, firstHash))
        assertTrue(hasher.verify(password, secondHash))
    }

    @Test
    fun `blank password is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            hasher.hash("   ")
        }
    }

    @Test
    fun `password longer than 72 bytes is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            hasher.hash("a".repeat(73))
        }
    }

    @Test
    fun `multibyte password longer than 72 bytes is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            hasher.hash("é".repeat(37))
        }
    }

    @Test
    fun `malformed hash does not authenticate`() {
        assertFalse(
            hasher.verify("MySecurePassword123!", "invalid-hash")
        )
    }

    @Test
    fun `blank password does not authenticate`() {
        val hash = hasher.hash("MySecurePassword123!")

        assertFalse(hasher.verify("", hash))
    }

    @Test
    fun `invalid work factor is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            BCryptPasswordHasher(workFactor = 4)
        }
    }
}
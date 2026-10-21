// test/nl/brul/api/features/vote/repository/ExposedVoteRepositoryTest.kt
package nl.brul.api.features.vote.repository

import nl.brul.api.features.report.dto.CreateReportRequest
import nl.brul.api.features.report.dto.toNewReport
import nl.brul.api.features.report.repository.ExposedReportRepository
import nl.brul.api.features.report.repository.ReportsTable
import nl.brul.api.features.user.model.NewUser
import nl.brul.api.features.user.repository.ExposedUserRepository
import nl.brul.api.features.user.repository.UsersTable
import nl.brul.api.features.vote.model.VoteType
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExposedVoteRepositoryTest {

    private fun withRepository(
        block: (VoteRepository) -> Unit
    ) {
        val databaseName = "votes_${UUID.randomUUID()}"

        val database = Database.connect(
            url = "jdbc:h2:mem:$databaseName;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction(database) {
            SchemaUtils.create(
                UsersTable,
                ReportsTable,
                VotesTable
            )
        }

        val userRepository = ExposedUserRepository(database)

        // Create four users for report ownership and voting.
        repeat(4) { index ->
            val number = index + 1

            userRepository.create(
                NewUser(
                    username = "testuser$number",
                    email = "testuser$number@example.com",
                    passwordHash = "test-password-hash"
                )
            )
        }

        val reportRepository = ExposedReportRepository(database)

        // Create three reports for the existing repository tests.
        repeat(3) { index ->
            reportRepository.create(
                CreateReportRequest(
                    type = "SAFETY",
                    title = "Test report ${index + 1}",
                    description = "Test report for voting",
                    category = "TRAFFIC_SAFETY",
                    latitude = 52.0705,
                    longitude = 4.3007,
                    urgent = false
                ).toNewReport().copy(ownerId = 1L)
            )
        }

        val repository = ExposedVoteRepository(database)

        try {
            block(repository)
        } finally {
            transaction(database) {
                SchemaUtils.drop(
                    VotesTable,
                    ReportsTable,
                    UsersTable
                )
            }
        }
    }

    @Test
    fun `save LIKE vote`() = withRepository { repository ->
        val vote = repository.save(
            reportId = 1,
            userId = 2,
            type = VoteType.LIKE
        )

        assertTrue(vote.id > 0)
        assertEquals(1L, vote.reportId)
        assertEquals(2L, vote.userId)
        assertEquals(VoteType.LIKE, vote.type)
    }

    @Test
    fun `save DISLIKE vote`() = withRepository { repository ->
        val vote = repository.save(
            reportId = 1,
            userId = 2,
            type = VoteType.DISLIKE
        )

        assertEquals(VoteType.DISLIKE, vote.type)
    }

    @Test
    fun `find existing vote`() = withRepository { repository ->
        repository.save(1, 2, VoteType.LIKE)

        val vote = repository.findByReportAndUser(1, 2)

        assertNotNull(vote)
        assertEquals(VoteType.LIKE, vote.type)
    }

    @Test
    fun `unknown vote returns null`() = withRepository { repository ->
        val vote = repository.findByReportAndUser(999, 888)

        assertNull(vote)
    }

    @Test
    fun `changing vote preserves vote ID`() =
        withRepository { repository ->
            val original = repository.save(
                reportId = 1,
                userId = 2,
                type = VoteType.LIKE
            )

            val changed = repository.save(
                reportId = 1,
                userId = 2,
                type = VoteType.DISLIKE
            )

            assertEquals(original.id, changed.id)
            assertEquals(VoteType.DISLIKE, changed.type)
        }

    @Test
    fun `changing vote does not create duplicate`() =
        withRepository { repository ->
            repository.save(1, 2, VoteType.LIKE)
            repository.save(1, 2, VoteType.DISLIKE)

            val votes = repository.findByReport(1)

            assertEquals(1, votes.size)
            assertEquals(VoteType.DISLIKE, votes.single().type)
        }

    @Test
    fun `different users can vote on same report`() =
        withRepository { repository ->
            repository.save(1, 2, VoteType.LIKE)
            repository.save(1, 3, VoteType.DISLIKE)

            val votes = repository.findByReport(1)

            assertEquals(2, votes.size)
        }

    @Test
    fun `same user can vote on different reports`() =
        withRepository { repository ->
            repository.save(1, 2, VoteType.LIKE)
            repository.save(3, 2, VoteType.DISLIKE)

            assertEquals(
                1,
                repository.findByReport(1).size
            )

            assertEquals(
                1,
                repository.findByReport(3).size
            )
        }

    @Test
    fun `delete existing vote`() = withRepository { repository ->
        repository.save(1, 2, VoteType.LIKE)

        val deleted = repository.delete(1, 2)

        assertTrue(deleted)
        assertNull(repository.findByReportAndUser(1, 2))
    }

    @Test
    fun `delete unknown vote returns false`() =
        withRepository { repository ->
            val deleted = repository.delete(999, 888)

            assertFalse(deleted)
        }

    @Test
    fun `delete affects only selected user`() =
        withRepository { repository ->
            repository.save(1, 2, VoteType.LIKE)
            repository.save(1, 3, VoteType.DISLIKE)

            repository.delete(1, 2)

            assertNull(repository.findByReportAndUser(1, 2))
            assertNotNull(repository.findByReportAndUser(1, 3))
        }

    @Test
    fun `votes are isolated by report ID`() =
        withRepository { repository ->
            repository.save(1, 2, VoteType.LIKE)
            repository.save(3, 4, VoteType.DISLIKE)

            assertEquals(
                VoteType.LIKE,
                repository.findByReport(1).single().type
            )

            assertEquals(
                VoteType.DISLIKE,
                repository.findByReport(3).single().type
            )
        }

    @Test
    fun `invalid report ID is rejected`() =
        withRepository { repository ->
            assertFailsWith<IllegalArgumentException> {
                repository.save(
                    reportId = 0,
                    userId = 2,
                    type = VoteType.LIKE
                )
            }
        }

    @Test
    fun `invalid user ID is rejected`() =
        withRepository { repository ->
            assertFailsWith<IllegalArgumentException> {
                repository.save(
                    reportId = 1,
                    userId = -1,
                    type = VoteType.LIKE
                )
            }
        }
}
// test/nl/brul/api/features/help/repository/HelpParticipationRepositoryTest.kt
package nl.brul.api.features.help.repository

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import nl.brul.api.features.help.model.ParticipationStatus
import nl.brul.api.features.report.repository.ExposedReportRepository
import nl.brul.api.support.TestDatabase
import nl.brul.api.support.createHelpRequest
import org.jetbrains.exposed.exceptions.ExposedSQLException

class HelpParticipationRepositoryTest {

    private val joined = Instant.parse("2026-11-20T07:00:00Z")

    private fun withRepositories(
        block: (ExposedReportRepository, HelpParticipationRepository) -> Unit
    ) {
        val database = TestDatabase.create("help_repo")

        block(
            ExposedReportRepository(database),
            ExposedHelpParticipationRepository(database)
        )
    }

    @Test
    fun `user can join a help request`() =
        withRepositories { reports, participations ->
            val requestId = reports.createHelpRequest().id

            val participation = participations.add(
                helpRequestId = requestId,
                userId = 2,
                joinedAt = joined
            )

            assertEquals(requestId, participation.helpRequestId)
            assertEquals(2L, participation.userId)
            assertEquals(ParticipationStatus.SIGNED_UP, participation.status)
            assertEquals(1, participations.countSignedUp(requestId))
        }

    // T15
    @Test
    fun `status and times are stored and read back`() =
        withRepositories { reports, participations ->
            val requestId = reports.createHelpRequest().id

            val added = participations.add(
                helpRequestId = requestId,
                userId = 2,
                status = ParticipationStatus.WAITLISTED,
                joinedAt = joined
            )

            val promoted = added.moveTo(
                ParticipationStatus.SIGNED_UP,
                Instant.parse("2026-11-20T07:45:00Z")
            )

            assertTrue(participations.update(promoted))

            val found = assertNotNull(
                participations.findByHelpRequestAndUser(requestId, 2)
            )

            assertEquals(ParticipationStatus.SIGNED_UP, found.status)
            assertEquals(joined, found.joinedAt)
            assertEquals(Instant.parse("2026-11-20T07:45:00Z"), found.changedAt)
        }

    @Test
    fun `only signed up participations are counted as places`() =
        withRepositories { reports, participations ->
            val requestId = reports.createHelpRequest().id

            participations.add(requestId, 2, ParticipationStatus.SIGNED_UP, joined)
            participations.add(requestId, 3, ParticipationStatus.WAITLISTED, joined)
            participations.add(requestId, 4, ParticipationStatus.CANCELLED, joined)

            assertEquals(1, participations.countSignedUp(requestId))
            assertEquals(3, participations.findByHelpRequest(requestId).size)
        }

    @Test
    fun `participations of one user are found across help requests`() =
        withRepositories { reports, participations ->
            val first = reports.createHelpRequest().id
            val second = reports.createHelpRequest(title = "Second request").id

            participations.add(first, 2, joinedAt = joined)
            participations.add(second, 2, joinedAt = joined)
            participations.add(second, 3, joinedAt = joined)

            assertEquals(
                listOf(first, second),
                participations.findByUser(2).map { it.helpRequestId }
            )
        }

    @Test
    fun `the same user cannot have two rows for one help request`() =
        withRepositories { reports, participations ->
            val requestId = reports.createHelpRequest().id

            participations.add(requestId, 2, joinedAt = joined)

            assertFailsWith<ExposedSQLException> {
                participations.add(requestId, 2, joinedAt = joined)
            }
        }

    @Test
    fun `unknown participation is not found`() =
        withRepositories { reports, participations ->
            val requestId = reports.createHelpRequest().id

            assertNull(participations.findByHelpRequestAndUser(requestId, 3))
        }

    @Test
    fun `participations are removed with their help request`() =
        withRepositories { reports, participations ->
            val requestId = reports.createHelpRequest().id

            participations.add(requestId, 2, joinedAt = joined)

            assertTrue(reports.delete(requestId))
            assertEquals(0, participations.findByUser(2).size)
        }
}

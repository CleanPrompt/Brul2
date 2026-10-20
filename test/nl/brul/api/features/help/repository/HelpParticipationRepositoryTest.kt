// test/nl/brul/api/features/help/repository/HelpParticipationRepositoryTest.kt
package nl.brul.api.features.help.repository

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import nl.brul.api.features.report.dto.CreateReportRequest
import nl.brul.api.features.report.dto.toNewReport
import nl.brul.api.features.report.repository.ExposedReportRepository
import nl.brul.api.features.report.repository.ReportsTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class HelpParticipationRepositoryTest {

    private fun withRepositories(
        block: (
            ExposedReportRepository,
            HelpParticipationRepository
        ) -> Unit
    ) {
        val database = Database.connect(
            url = "jdbc:h2:mem:help_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction(database) {
            SchemaUtils.create(
                ReportsTable,
                HelpParticipationsTable
            )
        }

        val reports = ExposedReportRepository(database)
        val participations =
            ExposedHelpParticipationRepository(database)

        try {
            block(reports, participations)
        } finally {
            transaction(database) {
                SchemaUtils.drop(
                    HelpParticipationsTable,
                    ReportsTable
                )
            }
        }
    }

    private fun ExposedReportRepository.createHelpRequest(): Long =
        create(
            CreateReportRequest(
                type = "HELP_REQUEST",
                title = "Help with groceries",
                description = "I need help carrying groceries",
                category = "GROCERIES",
                latitude = 52.0705,
                longitude = 4.3007,
                volunteersNeeded = 3
            ).toNewReport().copy(ownerId = 1L)
        ).id

    @Test
    fun `user can join a help request`() =
        withRepositories { reports, participations ->
            val requestId = reports.createHelpRequest()

            val participation = participations.add(
                helpRequestId = requestId,
                userId = 2
            )

            assertTrue(participation.id > 0)
            assertEquals(requestId, participation.helpRequestId)
            assertEquals(2L, participation.userId)
            assertEquals(
                1,
                participations.countByHelpRequest(requestId)
            )
        }

    @Test
    fun `multiple users can join the same help request`() =
        withRepositories { reports, participations ->
            val requestId = reports.createHelpRequest()

            participations.add(requestId, 2)
            participations.add(requestId, 3)

            assertEquals(
                2,
                participations.countByHelpRequest(requestId)
            )

            assertEquals(
                2,
                participations.findByHelpRequest(requestId).size
            )
        }

    @Test
    fun `user can leave a help request`() =
        withRepositories { reports, participations ->
            val requestId = reports.createHelpRequest()

            participations.add(requestId, 2)

            assertTrue(
                participations.remove(requestId, 2)
            )

            assertNull(
                participations.findByHelpRequestAndUser(
                    requestId,
                    2
                )
            )

            assertEquals(
                0,
                participations.countByHelpRequest(requestId)
            )
        }

    @Test
    fun `user can join again after leaving`() =
        withRepositories { reports, participations ->
            val requestId = reports.createHelpRequest()

            participations.add(requestId, 2)
            participations.remove(requestId, 2)

            val joinedAgain = participations.add(requestId, 2)

            assertNotNull(joinedAgain)
            assertEquals(
                1,
                participations.countByHelpRequest(requestId)
            )
        }

    @Test
    fun `leaving without participation returns false`() =
        withRepositories { reports, participations ->
            val requestId = reports.createHelpRequest()

            assertFalse(
                participations.remove(requestId, 2)
            )
        }

    @Test
    fun `deleting help request removes its participants`() =
        withRepositories { reports, participations ->
            val requestId = reports.createHelpRequest()

            participations.add(requestId, 2)
            participations.add(requestId, 3)

            assertTrue(reports.delete(requestId))

            assertEquals(
                0,
                participations.countByHelpRequest(requestId)
            )
        }
}
// test/nl/brul/api/features/help/service/HelpParticipationServiceTest.kt
package nl.brul.api.features.help.service

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import nl.brul.api.features.help.model.HelpParticipationStatus
import nl.brul.api.features.help.repository.ExposedHelpParticipationRepository
import nl.brul.api.features.help.repository.HelpParticipationsTable
import nl.brul.api.features.report.dto.CreateReportRequest
import nl.brul.api.features.report.dto.toNewReport
import nl.brul.api.features.report.repository.ExposedReportRepository
import nl.brul.api.features.report.repository.ReportsTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class HelpParticipationServiceTest {

    private fun withService(
        volunteersNeeded: Int = 2,
        block: (HelpParticipationService, Long) -> Unit
    ) {
        val database = Database.connect(
            url = "jdbc:h2:mem:help_service_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
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
        val participants = ExposedHelpParticipationRepository(database)

        val service = HelpParticipationService(
            participationRepository = participants,
            reportRepository = reports
        )

        val report = reports.create(
            CreateReportRequest(
                type = "HELP_REQUEST",
                title = "Help with groceries",
                description = "I need help carrying groceries",
                category = "GROCERIES",
                latitude = 52.0705,
                longitude = 4.3007,
                volunteersNeeded = volunteersNeeded
            ).toNewReport().copy(ownerId = 1L)
        )

        try {
            block(service, report.id)
        } finally {
            transaction(database) {
                SchemaUtils.drop(
                    HelpParticipationsTable,
                    ReportsTable
                )
            }
        }
    }

    @Test
    fun `user can join an open help request`() =
        withService { service, requestId ->
            val participation = service.join(requestId, 2)

            assertEquals(requestId, participation.helpRequestId)
            assertEquals(2L, participation.userId)

            val summary = service.getSummary(requestId)

            assertEquals(1, summary.participantCount)
            assertEquals(1, summary.availablePlaces)
            assertEquals(HelpParticipationStatus.OPEN, summary.status)
        }

    @Test
    fun `owner cannot join their own help request`() =
        withService { service, requestId ->
            assertFailsWith<HelpParticipationForbiddenException> {
                service.join(requestId, 1)
            }

            assertEquals(
                0,
                service.getSummary(requestId).participantCount
            )
        }

    @Test
    fun `user cannot join twice`() =
        withService { service, requestId ->
            service.join(requestId, 2)

            assertFailsWith<HelpParticipationConflictException> {
                service.join(requestId, 2)
            }

            assertEquals(
                1,
                service.getSummary(requestId).participantCount
            )
        }

    @Test
    fun `help request becomes full when capacity is reached`() =
        withService(volunteersNeeded = 2) { service, requestId ->
            service.join(requestId, 2)
            service.join(requestId, 3)

            val summary = service.getSummary(requestId)

            assertEquals(2, summary.participantCount)
            assertEquals(0, summary.availablePlaces)
            assertEquals(HelpParticipationStatus.FULL, summary.status)

            assertFailsWith<HelpParticipationConflictException> {
                service.join(requestId, 4)
            }
        }

    @Test
    fun `leaving a full help request makes space available`() =
        withService(volunteersNeeded = 1) { service, requestId ->
            service.join(requestId, 2)

            assertEquals(
                HelpParticipationStatus.FULL,
                service.getSummary(requestId).status
            )

            assertTrue(service.leave(requestId, 2))

            val summary = service.getSummary(requestId)

            assertEquals(0, summary.participantCount)
            assertEquals(1, summary.availablePlaces)
            assertEquals(HelpParticipationStatus.OPEN, summary.status)

            service.join(requestId, 3)

            assertEquals(
                1,
                service.getSummary(requestId).participantCount
            )
        }

    @Test
    fun `user cannot leave if they are not participating`() =
        withService { service, requestId ->
            assertFalse(service.leave(requestId, 2))
        }

    @Test
    fun `unknown help request is rejected`() =
        withService { service, _ ->
            assertFailsWith<HelpRequestNotFoundException> {
                service.join(999999, 2)
            }

            assertFailsWith<HelpRequestNotFoundException> {
                service.getSummary(999999)
            }
        }
}
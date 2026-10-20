// test/nl/brul/api/features/vote/repository/VoteCascadeDeleteTest.kt
package nl.brul.api.features.vote.repository

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import nl.brul.api.features.report.dto.CreateReportRequest
import nl.brul.api.features.report.dto.toNewReport
import nl.brul.api.features.report.repository.ExposedReportRepository
import nl.brul.api.features.report.repository.ReportsTable
import nl.brul.api.features.vote.model.VoteType
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class VoteCascadeDeleteTest {

    private fun withRepositories(
        block: (
            ExposedReportRepository,
            ExposedVoteRepository
        ) -> Unit
    ) {
        val databaseName = "vote_cascade_${UUID.randomUUID()}"

        val database = Database.connect(
            url = "jdbc:h2:mem:$databaseName;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction(database) {
            SchemaUtils.create(
                ReportsTable,
                VotesTable
            )
        }

        val reportRepository = ExposedReportRepository(database)
        val voteRepository = ExposedVoteRepository(database)

        try {
            block(reportRepository, voteRepository)
        } finally {
            transaction(database) {
                SchemaUtils.drop(
                    VotesTable,
                    ReportsTable
                )
            }
        }
    }

    private fun ExposedReportRepository.createTestReport(
        title: String
    ): Long {
        return create(
            CreateReportRequest(
                type = "SAFETY",
                title = title,
                description = "A report used for cascade deletion tests",
                category = "TRAFFIC_SAFETY",
                latitude = 52.0705,
                longitude = 4.3007,
                urgent = false
            ).toNewReport().copy(ownerId = 1L)
        ).id
    }

    @Test
    fun `deleting report removes all associated votes`() =
        withRepositories { reports, votes ->

            val reportId = reports.createTestReport(
                "Unsafe crossing"
            )

            votes.save(
                reportId = reportId,
                userId = 2,
                type = VoteType.LIKE
            )

            votes.save(
                reportId = reportId,
                userId = 3,
                type = VoteType.DISLIKE
            )

            assertEquals(
                2,
                votes.findByReport(reportId).size
            )

            val deleted = reports.delete(reportId)

            assertTrue(deleted)

            assertEquals(
                0,
                votes.findByReport(reportId).size
            )
        }

    @Test
    fun `deleting one report preserves votes on another report`() =
        withRepositories { reports, votes ->

            val firstReportId = reports.createTestReport(
                "First crossing"
            )

            val secondReportId = reports.createTestReport(
                "Second crossing"
            )

            votes.save(
                reportId = firstReportId,
                userId = 2,
                type = VoteType.LIKE
            )

            votes.save(
                reportId = secondReportId,
                userId = 3,
                type = VoteType.DISLIKE
            )

            val deleted = reports.delete(firstReportId)

            assertTrue(deleted)

            assertEquals(
                0,
                votes.findByReport(firstReportId).size
            )

            val remainingVotes = votes.findByReport(
                secondReportId
            )

            assertEquals(1, remainingVotes.size)
            assertEquals(
                VoteType.DISLIKE,
                remainingVotes.single().type
            )

            assertFalse(
                reports.findById(secondReportId) == null
            )
        }

    @Test
    fun `database rejects vote for nonexistent report`() =
        withRepositories { _, votes ->

            assertFailsWith<Exception> {
                votes.save(
                    reportId = 999999,
                    userId = 2,
                    type = VoteType.LIKE
                )
            }

            assertEquals(
                0,
                votes.findByReport(999999).size
            )
        }
}
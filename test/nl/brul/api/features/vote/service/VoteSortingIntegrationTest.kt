// test/nl/brul/api/features/vote/service/VoteSortingIntegrationTest.kt
package nl.brul.api.features.vote.service

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import nl.brul.api.features.report.dto.CreateReportRequest
import nl.brul.api.features.report.dto.toNewReport
import nl.brul.api.features.report.repository.ExposedReportRepository
import nl.brul.api.features.report.repository.ReportsTable
import nl.brul.api.features.report.service.ReportSearch
import nl.brul.api.features.report.service.ReportService
import nl.brul.api.features.report.service.ReportSort
import nl.brul.api.features.vote.model.VoteType
import nl.brul.api.features.vote.repository.ExposedVoteRepository
import nl.brul.api.features.vote.repository.VotesTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class VoteSortingIntegrationTest {

    private fun testWithServices(
        block: (ReportService, VoteService) -> Unit
    ) {
        val database = Database.connect(
            url = "jdbc:h2:mem:vote_sort_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction(database) {
            SchemaUtils.create(ReportsTable, VotesTable)
        }

        val reportRepository = ExposedReportRepository(database)
        val voteRepository = ExposedVoteRepository(database)

        block(
            ReportService(reportRepository, voteRepository),
            VoteService(voteRepository, reportRepository)
        )
    }

    private fun ReportService.createTestReport(
        title: String,
        ownerId: Long
    ): Long = create(
        CreateReportRequest(
            type = "SAFETY",
            title = title,
            description = "Traffic safety report",
            category = "TRAFFIC_SAFETY",
            latitude = 52.0705,
            longitude = 4.3007,
            urgent = false
        ).toNewReport().copy(ownerId = ownerId)
    ).id

    @Test
    fun `MOST_LIKED sorts reports by number of likes`() =
        testWithServices { reports, votes ->
            val first = reports.createTestReport(
                "First crossing",
                1
            )

            val second = reports.createTestReport(
                "Second crossing",
                1
            )

            val third = reports.createTestReport(
                "Third crossing",
                1
            )

            votes.castVote(first, 2, VoteType.LIKE)

            votes.castVote(second, 2, VoteType.LIKE)
            votes.castVote(second, 3, VoteType.LIKE)
            votes.castVote(second, 4, VoteType.LIKE)

            votes.castVote(third, 2, VoteType.LIKE)
            votes.castVote(third, 3, VoteType.LIKE)

            val sorted = reports.search(
                ReportSearch(sort = ReportSort.MOST_LIKED)
            )

            assertEquals(
                listOf(second, third, first),
                sorted.map { it.id }
            )
        }

    @Test
    fun `MOST_LIKED counts likes rather than net score`() =
        testWithServices { reports, votes ->
            val first = reports.createTestReport(
                "First crossing",
                1
            )

            val second = reports.createTestReport(
                "Second crossing",
                1
            )

            votes.castVote(first, 2, VoteType.LIKE)
            votes.castVote(first, 3, VoteType.LIKE)
            votes.castVote(first, 4, VoteType.DISLIKE)
            votes.castVote(first, 5, VoteType.DISLIKE)

            votes.castVote(second, 2, VoteType.LIKE)

            val sorted = reports.search(
                ReportSearch(sort = ReportSort.MOST_LIKED)
            )

            assertEquals(first, sorted.first().id)
            assertEquals(second, sorted.last().id)
        }
}
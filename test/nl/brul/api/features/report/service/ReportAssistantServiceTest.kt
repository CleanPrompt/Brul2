// test/nl/brul/api/features/report/service/ReportAssistantServiceTest.kt
package nl.brul.api.features.report.service

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import nl.brul.api.features.report.ai.ReportAssistantAi
import nl.brul.api.features.report.model.DuplicateCandidate
import nl.brul.api.features.report.model.DuplicateQuery
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.NewReport
import nl.brul.api.features.report.model.NuisanceReport
import nl.brul.api.features.report.model.Report
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportSuggestion
import nl.brul.api.features.report.model.ReportType
import nl.brul.api.features.report.repository.ReportRepository
import nl.brul.api.support.TestClock

/**
 * The assistant with a fake AI: the AI is tried first, and every failure of the AI falls back
 * to the fixed rules (S3, D5).
 */
class ReportAssistantServiceTest {

    private val clock = TestClock(Instant.parse("2026-11-20T12:00:00Z"))

    private val here = Location(51.5500, 4.6300)

    private class FakeReportRepository(
        private val reports: List<Report>
    ) : ReportRepository {

        override fun create(report: NewReport): Report =
            error("Not needed for this test.")

        override fun findAll(): List<Report> = reports

        override fun findById(id: Long): Report? = reports.firstOrNull { it.id == id }

        override fun update(report: Report): Boolean = false

        override fun delete(id: Long): Boolean = false
    }

    /** A fake AI that returns fixed answers, or throws when [fail] is true. */
    private class FakeAi(
        private val suggestion: ReportSuggestion? = null,
        private val picks: Set<Long>? = null,
        private val fail: Boolean = false
    ) : ReportAssistantAi {

        var duplicateCalls = 0

        override fun suggestCategory(title: String, description: String): ReportSuggestion? {
            check(!fail) { "AI is down." }
            return suggestion
        }

        override fun pickDuplicates(
            query: DuplicateQuery,
            candidates: List<DuplicateCandidate>
        ): Set<Long>? {
            duplicateCalls++
            check(!fail) { "AI is down." }
            return picks
        }
    }

    private fun waste(id: Long, title: String, description: String) = NuisanceReport(
        id = id,
        title = title,
        description = description,
        category = ReportCategory.WASTE,
        location = Location(51.5505, 4.6300),
        createdAt = clock.instant().toEpochMilli()
    )

    private val reports = listOf(
        waste(1, "Vuilniszakken bij de container", "Vuilniszakken naast de container"),
        waste(2, "Glasbak vol", "De glasbak zit helemaal vol")
    )

    private val query = DuplicateQuery(
        type = ReportType.NUISANCE,
        title = "Afval naast container",
        description = "Vuilniszakken naast de container",
        location = here
    )

    private fun service(ai: ReportAssistantAi? = null) = ReportAssistantService(
        repository = FakeReportRepository(reports),
        ai = ai,
        clock = clock
    )

    private val aiSuggestion = ReportSuggestion(
        ReportType.SAFETY,
        ReportCategory.THEFT,
        urgent = false,
        source = ReportSuggestion.Source.AI
    )

    @Test
    fun `S1 the AI suggestion is used when there is one`() {
        val suggestion = service(FakeAi(suggestion = aiSuggestion))
            .suggest("Rijwiel weggenomen", "Mijn rijwiel is weg")

        assertEquals(aiSuggestion, suggestion)
    }

    @Test
    fun `S3 without AI the keywords make the suggestion`() {
        val suggestion = service(ai = null).suggest("Fiets gestolen", "Bij het station")

        assertEquals(ReportCategory.THEFT, suggestion?.category)
        assertEquals(ReportSuggestion.Source.KEYWORDS, suggestion?.source)
    }

    @Test
    fun `S3 a failing AI falls back to the keywords`() {
        val suggestion = service(FakeAi(fail = true)).suggest("Lawaai", "Harde muziek")

        assertEquals(ReportCategory.NOISE, suggestion?.category)
        assertEquals(ReportSuggestion.Source.KEYWORDS, suggestion?.source)
    }

    @Test
    fun `S3 an AI without an answer falls back to the keywords`() {
        val suggestion = service(FakeAi(suggestion = null)).suggest("Lawaai", "Harde muziek")

        assertEquals(ReportSuggestion.Source.KEYWORDS, suggestion?.source)
    }

    @Test
    fun `a suggestion needs some text`() {
        assertFailsWith<IllegalArgumentException> {
            service().suggest(" ", "")
        }
    }

    @Test
    fun `too long text is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            service().suggest("x".repeat(ReportAssistantService.MAX_TEXT_LENGTH + 1), "")
        }
    }

    @Test
    fun `D5 without AI the words decide`() {
        val ids = service(ai = null).findDuplicates(query).map { it.report.id }

        assertEquals(listOf(1L), ids)
    }

    @Test
    fun `D6 the AI chooses from the candidates`() {
        val ids = service(FakeAi(picks = setOf(2L))).findDuplicates(query).map { it.report.id }

        assertEquals(listOf(2L), ids)
    }

    @Test
    fun `D6 ids the AI made up are ignored`() {
        val ids = service(FakeAi(picks = setOf(99L))).findDuplicates(query)

        assertTrue(ids.isEmpty())
    }

    @Test
    fun `D5 a failing AI falls back to the words`() {
        val ids = service(FakeAi(fail = true)).findDuplicates(query).map { it.report.id }

        assertEquals(listOf(1L), ids)
    }

    @Test
    fun `the AI is not asked when nothing is nearby`() {
        val ai = FakeAi(picks = setOf(1L))
        val farAway = query.copy(location = Location(52.3700, 4.9000))

        val duplicates = service(ai).findDuplicates(farAway)

        assertTrue(duplicates.isEmpty())
        assertEquals(0, ai.duplicateCalls)
    }
}

// test/nl/brul/api/features/report/ai/ClaudeReportAssistantAiTest.kt
package nl.brul.api.features.report.ai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import nl.brul.api.features.report.model.DuplicateCandidate
import nl.brul.api.features.report.model.DuplicateQuery
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.NuisanceReport
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportSuggestion
import nl.brul.api.features.report.model.ReportType

/**
 * The prompts and the parsing of AI answers, with a fake client instead of the internet.
 * The AI is never trusted blindly (S2, D6).
 */
class ClaudeReportAssistantAiTest {

    private fun ai(answer: String?) = ClaudeReportAssistantAi(AiTextClient { answer })

    private fun waste(id: Long) = NuisanceReport(
        id = id,
        title = "Afval $id",
        description = "Vuilniszakken bij container $id",
        category = ReportCategory.WASTE,
        location = Location(51.55, 4.63)
    )

    private val query = DuplicateQuery(
        type = ReportType.NUISANCE,
        title = "Afval",
        description = "Vuilniszakken bij de container",
        location = Location(51.55, 4.63)
    )

    private val candidates = listOf(
        DuplicateCandidate(waste(1), distanceKm = 0.05, similarity = 0.5),
        DuplicateCandidate(waste(2), distanceKm = 0.10, similarity = 0.2)
    )

    @Test
    fun `valid JSON answer becomes a suggestion`() {
        val suggestion = ai("""{"type": "NUISANCE", "category": "WASTE", "urgent": null}""")
            .suggestCategory("Afval", "Vuilniszakken")

        assertEquals(
            ReportSuggestion(ReportType.NUISANCE, ReportCategory.WASTE, null, ReportSuggestion.Source.AI),
            suggestion
        )
    }

    @Test
    fun `text around the JSON is ignored`() {
        val suggestion = ClaudeReportAssistantAi.parseSuggestion(
            """Dit is mijn antwoord: {"type": "SAFETY", "category": "THEFT", "urgent": true} klaar"""
        )

        assertEquals(ReportCategory.THEFT, suggestion?.category)
        assertEquals(true, suggestion?.urgent)
    }

    @Test
    fun `S2 a category that does not fit the type is no suggestion`() {
        assertNull(
            ClaudeReportAssistantAi.parseSuggestion("""{"type": "SAFETY", "category": "WASTE"}""")
        )
    }

    @Test
    fun `S2 an unknown category is no suggestion`() {
        assertNull(
            ClaudeReportAssistantAi.parseSuggestion("""{"type": "NUISANCE", "category": "UFO"}""")
        )
    }

    @Test
    fun `S2 urgent is dropped for other types than safety`() {
        val suggestion = ClaudeReportAssistantAi.parseSuggestion(
            """{"type": "NUISANCE", "category": "NOISE", "urgent": true}"""
        )

        assertNull(suggestion?.urgent)
    }

    @Test
    fun `an answer without JSON is no suggestion`() {
        assertNull(ClaudeReportAssistantAi.parseSuggestion("Ik weet het niet."))
    }

    @Test
    fun `no answer at all is no suggestion`() {
        assertNull(ai(null).suggestCategory("Afval", "Vuilniszakken"))
    }

    @Test
    fun `the prompt lists every allowed category and the user text`() {
        val prompt = ClaudeReportAssistantAi.suggestionPrompt("Fiets weg", "Gestolen bij het station")

        ReportType.entries.forEach { type ->
            type.allowedCategories.forEach { category ->
                assertTrue(category.name in prompt)
            }
        }

        assertTrue("Fiets weg" in prompt)
        assertTrue("Gestolen bij het station" in prompt)
    }

    @Test
    fun `D6 chosen ids come back`() {
        val ids = ai("""{"ids": [2]}""").pickDuplicates(query, candidates)

        assertEquals(setOf(2L), ids)
    }

    @Test
    fun `D6 ids that are not candidates are dropped`() {
        val ids = ai("""{"ids": [1, 42]}""").pickDuplicates(query, candidates)

        assertEquals(setOf(1L), ids)
    }

    @Test
    fun `D6 an empty list means no duplicates`() {
        assertEquals(emptySet(), ai("""{"ids": []}""").pickDuplicates(query, candidates))
    }

    @Test
    fun `D6 an unreadable answer gives null so the rules decide`() {
        assertNull(ai("geen idee").pickDuplicates(query, candidates))
    }

    @Test
    fun `the duplicate prompt names every candidate`() {
        val prompt = ClaudeReportAssistantAi.duplicatePrompt(query, candidates)

        assertTrue("id 1:" in prompt)
        assertTrue("id 2:" in prompt)
    }

    @Test
    fun `text is read from a Claude API response`() {
        val body = """{"content": [{"type": "text", "text": "{\"ids\": []}"}]}"""

        assertEquals("""{"ids": []}""", ClaudeTextClient.firstText(body))
    }
}

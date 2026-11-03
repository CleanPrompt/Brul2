// test/nl/brul/api/features/report/model/ReportSuggestionTest.kt
package nl.brul.api.features.report.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** S2: a suggestion follows the same category rules as a real report. */
class ReportSuggestionTest {

    @Test
    fun `fitting type and category is allowed`() {
        val suggestion = ReportSuggestion(
            ReportType.HELP_REQUEST,
            ReportCategory.GROCERIES,
            source = ReportSuggestion.Source.AI
        )

        assertEquals(ReportCategory.GROCERIES, suggestion.category)
    }

    @Test
    fun `category of another type is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            ReportSuggestion(ReportType.SAFETY, ReportCategory.NOISE, source = ReportSuggestion.Source.AI)
        }
    }

    @Test
    fun `urgent only belongs to safety reports`() {
        assertFailsWith<IllegalArgumentException> {
            ReportSuggestion(
                ReportType.NUISANCE,
                ReportCategory.NOISE,
                urgent = true,
                source = ReportSuggestion.Source.KEYWORDS
            )
        }
    }

    @Test
    fun `duplicate query needs some text`() {
        assertFailsWith<IllegalArgumentException> {
            DuplicateQuery(ReportType.NUISANCE, " ", "", Location(51.55, 4.63))
        }
    }

    @Test
    fun `duplicate query category must fit the type`() {
        assertFailsWith<IllegalArgumentException> {
            DuplicateQuery(
                ReportType.NUISANCE,
                "Afval",
                "",
                Location(51.55, 4.63),
                category = ReportCategory.THEFT
            )
        }
    }
}

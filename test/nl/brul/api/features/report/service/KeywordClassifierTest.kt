// test/nl/brul/api/features/report/service/KeywordClassifierTest.kt
package nl.brul.api.features.report.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportSuggestion
import nl.brul.api.features.report.model.ReportType

/** S3: the keyword fallback, without any AI. */
class KeywordClassifierTest {

    @Test
    fun `stolen bike is a theft safety report`() {
        val suggestion = KeywordClassifier.suggest(
            "Fiets weg",
            "Mijn fiets is vannacht gestolen bij het station"
        )

        assertEquals(ReportType.SAFETY, suggestion?.type)
        assertEquals(ReportCategory.THEFT, suggestion?.category)
        assertEquals(ReportSuggestion.Source.KEYWORDS, suggestion?.source)
    }

    @Test
    fun `safety report is urgent when danger is happening now`() {
        val suggestion = KeywordClassifier.suggest(
            "Inbraak",
            "Er wordt nu ingebroken bij de buren"
        )

        assertEquals(ReportCategory.THEFT, suggestion?.category)
        assertEquals(true, suggestion?.urgent)
    }

    @Test
    fun `safety report is not urgent without danger words`() {
        val suggestion = KeywordClassifier.suggest(
            "Fiets gestolen",
            "Vorige week gestolen bij de school"
        )

        assertEquals(false, suggestion?.urgent)
    }

    @Test
    fun `garbage bags are a waste nuisance report`() {
        val suggestion = KeywordClassifier.suggest(
            "Rommel",
            "Vuilniszakken gedumpt naast de container"
        )

        assertEquals(ReportType.NUISANCE, suggestion?.type)
        assertEquals(ReportCategory.WASTE, suggestion?.category)
        assertNull(suggestion?.urgent)
    }

    @Test
    fun `help with moving is a help request`() {
        val suggestion = KeywordClassifier.suggest(
            "Hulp gezocht",
            "Wie kan zaterdag helpen sjouwen met mijn verhuizing?"
        )

        assertEquals(ReportType.HELP_REQUEST, suggestion?.type)
        assertEquals(ReportCategory.MOVING, suggestion?.category)
    }

    @Test
    fun `keywords ignore capitals and accents`() {
        val suggestion = KeywordClassifier.suggest("LAWAAI", "Hárde müziek tot 3 uur")

        assertEquals(ReportCategory.NOISE, suggestion?.category)
    }

    @Test
    fun `unknown text gives no suggestion`() {
        assertNull(KeywordClassifier.suggest("Hallo", "Even een vraagje"))
    }
}

// test/nl/brul/api/features/report/service/DuplicateRulesTest.kt
package nl.brul.api.features.report.service

import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import nl.brul.api.features.report.model.DuplicateCandidate
import nl.brul.api.features.report.model.DuplicateQuery
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.NuisanceReport
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportStatus
import nl.brul.api.features.report.model.ReportType
import nl.brul.api.features.report.model.SafetyReport

/** Pure tests of the duplicate rules D1-D5. */
class DuplicateRulesTest {

    private val now: Instant = Instant.parse("2026-11-20T12:00:00Z")

    private val here = Location(51.5500, 4.6300)

    /** About 100 metres north of [here]. */
    private val near = Location(51.5509, 4.6300)

    /** About 1 kilometre north of [here]. */
    private val far = Location(51.5590, 4.6300)

    private fun waste(
        id: Long,
        location: Location = near,
        title: String = "Vuilniszakken bij de container",
        description: String = "Er staan al dagen vuilniszakken naast de container",
        status: ReportStatus = ReportStatus.OPEN,
        age: Duration = Duration.ofDays(1)
    ) = NuisanceReport(
        id = id,
        title = title,
        description = description,
        category = ReportCategory.WASTE,
        location = location,
        status = status,
        createdAt = now.minus(age).toEpochMilli()
    )

    private val query = DuplicateQuery(
        type = ReportType.NUISANCE,
        title = "Afval naast container",
        description = "Vuilniszakken naast de container in de straat",
        location = here,
        category = ReportCategory.WASTE
    )

    @Test
    fun `nearby open report of the same type is a candidate`() {
        val candidates = DuplicateRules.candidates(query, listOf(waste(1)), now)

        assertEquals(listOf(1L), candidates.map { it.report.id })
        assertTrue(candidates.single().distanceKm in 0.09..0.11)
    }

    @Test
    fun `D1 other type is no candidate`() {
        val theft = SafetyReport(
            id = 2,
            title = "Vuilniszakken bij de container",
            description = "Iemand stal een vuilniszak",
            category = ReportCategory.THEFT,
            location = near,
            urgent = false,
            createdAt = now.toEpochMilli()
        )

        assertTrue(DuplicateRules.candidates(query, listOf(theft), now).isEmpty())
    }

    @Test
    fun `D2 resolved report is no candidate`() {
        val resolved = waste(1, status = ReportStatus.RESOLVED)

        assertTrue(DuplicateRules.candidates(query, listOf(resolved), now).isEmpty())
    }

    @Test
    fun `D3 report further than 250 metres is no candidate`() {
        assertTrue(DuplicateRules.candidates(query, listOf(waste(1, location = far)), now).isEmpty())
    }

    @Test
    fun `D4 report older than 14 days is no candidate`() {
        val old = waste(1, age = Duration.ofDays(15))

        assertTrue(DuplicateRules.candidates(query, listOf(old), now).isEmpty())
    }

    @Test
    fun `candidates with more similar text come first`() {
        val similar = waste(1)
        val different = waste(2, title = "Glasbak vol", description = "De glasbak zit helemaal vol")

        val ids = DuplicateRules.candidates(query, listOf(different, similar), now)
            .map { it.report.id }

        assertEquals(listOf(1L, 2L), ids)
    }

    @Test
    fun `similarity ignores order, stop words and word endings`() {
        val score = DuplicateRules.similarity(
            "fiets gestolen station",
            "Gestolen fietsen bij het station"
        )

        assertEquals(1.0, score)
    }

    @Test
    fun `similarity of unrelated text is zero`() {
        assertEquals(0.0, DuplicateRules.similarity("hond blaft", "fiets gestolen"))
    }

    @Test
    fun `D5 similar words make a likely duplicate`() {
        val candidate = DuplicateRules.candidates(query, listOf(waste(1)), now).single()

        assertTrue(candidate.similarity >= DuplicateRules.MIN_SIMILARITY)
        assertTrue(DuplicateRules.isLikelyDuplicate(query, candidate))
    }

    @Test
    fun `D5 same category very close counts without similar words`() {
        val report = waste(1, title = "Glasbak vol", description = "De glasbak zit helemaal vol")
        val candidate = DuplicateCandidate(report, distanceKm = 0.03, similarity = 0.0)

        assertTrue(DuplicateRules.isLikelyDuplicate(query, candidate))
    }

    @Test
    fun `D5 different words further away is no duplicate`() {
        val report = waste(1, title = "Glasbak vol", description = "De glasbak zit helemaal vol")
        val candidate = DuplicateCandidate(report, distanceKm = 0.2, similarity = 0.0)

        assertFalse(DuplicateRules.isLikelyDuplicate(query, candidate))
    }
}

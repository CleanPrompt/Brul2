// test/nl/brul/api/features/report/model/ReportModelTest.kt
package nl.brul.api.features.report.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ReportModelTest {

    private val location = Location(
        latitude = 52.0705,
        longitude = 4.3007
    )

    @Test
    fun `safety report has correct type and summary`() {
        val report = SafetyReport(
            id = 1,
            title = "Broken streetlight",
            description = "Streetlight is not working.",
            location = location,
            urgent = true
        )

        assertEquals(ReportType.SAFETY, report.type)
        assertEquals(ReportStatus.OPEN, report.status)
        assertEquals(
            "Safety: Broken streetlight (Urgent)",
            report.summary()
        )
    }

    @Test
    fun `nuisance report has correct type and summary`() {
        val report = NuisanceReport(
            id = 2,
            title = "Noise at night",
            description = "Loud music after midnight.",
            location = location,
            category = "Noise"
        )

        assertEquals(ReportType.NUISANCE, report.type)
        assertEquals(
            "Nuisance: Noise at night (Noise)",
            report.summary()
        )
    }

    @Test
    fun `help request has correct type and summary`() {
        val report = HelpRequest(
            id = 3,
            title = "Help with groceries",
            description = "Need help carrying groceries.",
            location = location,
            volunteersNeeded = 2
        )

        assertEquals(ReportType.HELP_REQUEST, report.type)
        assertEquals(
            "Help: Help with groceries (2 volunteers needed)",
            report.summary()
        )
    }

    @Test
    fun `different report types support polymorphism`() {
        val reports: List<Report> = listOf(
            SafetyReport(
                id = 1,
                title = "Unsafe crossing",
                description = "Cars are speeding.",
                location = location,
                urgent = true
            ),
            NuisanceReport(
                id = 2,
                title = "Litter",
                description = "Trash on the sidewalk.",
                location = location,
                category = "Waste"
            ),
            HelpRequest(
                id = 3,
                title = "Moving furniture",
                description = "Need help moving a table.",
                location = location,
                volunteersNeeded = 2
            )
        )

        val summaries = reports.map { it.summary() }

        assertEquals(3, summaries.size)
        assertTrue(summaries[0].startsWith("Safety:"))
        assertTrue(summaries[1].startsWith("Nuisance:"))
        assertTrue(summaries[2].startsWith("Help:"))
    }

    @Test
    fun `report title cannot be blank`() {
        assertFailsWith<IllegalArgumentException> {
            SafetyReport(
                id = 1,
                title = " ",
                description = "Invalid report.",
                location = location,
                urgent = false
            )
        }
    }

    @Test
    fun `location rejects invalid coordinates`() {
        assertFailsWith<IllegalArgumentException> {
            Location(
                latitude = 100.0,
                longitude = 4.3007
            )
        }
    }

    @Test
    fun `help request requires at least one volunteer`() {
        assertFailsWith<IllegalArgumentException> {
            HelpRequest(
                id = 3,
                title = "Help needed",
                description = "Need some assistance.",
                location = location,
                volunteersNeeded = 0
            )
        }
    }

    @Test
    fun `nuisance category cannot be blank`() {
        assertFailsWith<IllegalArgumentException> {
            NuisanceReport(
                id = 2,
                title = "Noise",
                description = "Loud noise outside.",
                location = location,
                category = ""
            )
        }
    }
}
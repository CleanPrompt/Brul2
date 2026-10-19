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
    fun `safety report has correct type and category`() {
        val report = SafetyReport(
            id = 1,
            title = "Unsafe crossing",
            description = "Cars are speeding.",
            category = ReportCategory.TRAFFIC_SAFETY,
            location = location,
            urgent = true
        )

        assertEquals(ReportType.SAFETY, report.type)
        assertEquals(ReportCategory.TRAFFIC_SAFETY, report.category)
        assertEquals(ReportStatus.OPEN, report.status)
        assertTrue(report.urgent)
    }

    @Test
    fun `nuisance report has correct type and category`() {
        val report = NuisanceReport(
            id = 2,
            title = "Noise at night",
            description = "Loud music after midnight.",
            category = ReportCategory.NOISE,
            location = location
        )

        assertEquals(ReportType.NUISANCE, report.type)
        assertEquals(ReportCategory.NOISE, report.category)
    }

    @Test
    fun `help request has correct type and category`() {
        val report = HelpRequest(
            id = 3,
            title = "Help with groceries",
            description = "Need help carrying groceries.",
            category = ReportCategory.GROCERIES,
            location = location,
            volunteersNeeded = 2
        )

        assertEquals(ReportType.HELP_REQUEST, report.type)
        assertEquals(ReportCategory.GROCERIES, report.category)
        assertEquals(2, report.volunteersNeeded)
    }

    @Test
    fun `different report types support polymorphism`() {
        val reports: List<Report> = listOf(
            SafetyReport(
                id = 1,
                title = "Unsafe crossing",
                description = "Cars are speeding.",
                category = ReportCategory.TRAFFIC_SAFETY,
                location = location,
                urgent = true
            ),
            NuisanceReport(
                id = 2,
                title = "Litter",
                description = "Trash on the sidewalk.",
                category = ReportCategory.WASTE,
                location = location
            ),
            HelpRequest(
                id = 3,
                title = "Moving furniture",
                description = "Need help moving a table.",
                category = ReportCategory.MOVING,
                location = location,
                volunteersNeeded = 2
            )
        )

        val types = reports.map { it.type }

        assertEquals(
            listOf(
                ReportType.SAFETY,
                ReportType.NUISANCE,
                ReportType.HELP_REQUEST
            ),
            types
        )
    }

    @Test
    fun `report title cannot be blank`() {
        assertFailsWith<IllegalArgumentException> {
            SafetyReport(
                id = 1,
                title = " ",
                description = "Invalid report.",
                category = ReportCategory.THEFT,
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
                category = ReportCategory.GROCERIES,
                location = location,
                volunteersNeeded = 0
            )
        }
    }

    @Test
    fun `safety report rejects nuisance category`() {
        assertFailsWith<IllegalArgumentException> {
            SafetyReport(
                id = 1,
                title = "Invalid safety report",
                description = "Incorrect category.",
                category = ReportCategory.NOISE,
                location = location,
                urgent = false
            )
        }
    }

    @Test
    fun `nuisance report rejects help category`() {
        assertFailsWith<IllegalArgumentException> {
            NuisanceReport(
                id = 2,
                title = "Invalid nuisance report",
                description = "Incorrect category.",
                category = ReportCategory.GROCERIES,
                location = location
            )
        }
    }

    @Test
    fun `help request rejects safety category`() {
        assertFailsWith<IllegalArgumentException> {
            HelpRequest(
                id = 3,
                title = "Invalid help request",
                description = "Incorrect category.",
                category = ReportCategory.THEFT,
                location = location,
                volunteersNeeded = 2
            )
        }
    }
}
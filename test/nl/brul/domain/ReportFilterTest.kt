package nl.brul.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class ReportFilterTest {

    private val user = User(
        id = "user-1",
        username = "user",
        role = Role.USER
    )

    private val location = Location(
        latitude = 52.0705,
        longitude = 4.3007
    )

    private val streetlight = SafetyReport(
        title = "Broken streetlight",
        description = null,
        photo = null,
        location = location,
        createdBy = user,
        urgency = Urgency.HIGH
    )

    private val crossing = SafetyReport(
        title = "Unsafe crossing",
        description = "Near the school",
        photo = null,
        location = location,
        createdBy = user,
        urgency = Urgency.LOW
    )

    private val music = NuisanceReport(
        title = "Loud music",
        description = "Every Friday near the school",
        photo = null,
        location = location,
        createdBy = user,
        nuisanceType = NuisanceType.NOISE
    )

    private val garden = HelpRequest(
        title = "Help with the garden",
        description = null,
        photo = null,
        location = location,
        createdBy = user
    )

    private val allReports = listOf(streetlight, crossing, music, garden)

    @Test
    fun `empty filter matches every report`() {
        assertEquals(allReports, allReports.filterBy(ReportFilter()))
    }

    @Test
    fun `filter by type`() {
        assertEquals(
            listOf(streetlight, crossing),
            allReports.filterBy(ReportFilter(type = ReportType.SAFETY))
        )
    }

    @Test
    fun `filter by urgency only matches safety reports`() {
        assertEquals(
            listOf(streetlight),
            allReports.filterBy(ReportFilter(urgency = Urgency.HIGH))
        )
    }

    @Test
    fun `search and type are combined`() {
        assertEquals(
            listOf(crossing),
            allReports.filterBy(
                ReportFilter(search = "school", type = ReportType.SAFETY)
            )
        )
    }

    @Test
    fun `blank search is ignored`() {
        assertEquals(allReports, allReports.filterBy(ReportFilter(search = "   ")))
    }
}

package nl.brul.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReportTest {

    private val owner = User(
        id = "user-1",
        username = "owner",
        role = Role.USER
    )

    private val otherUser = User(
        id = "user-2",
        username = "otherUser",
        role = Role.USER
    )

    private val admin = User(
        id = "admin-1",
        username = "admin",
        role = Role.ADMIN
    )

    private val location = Location(
        latitude = 52.0705,
        longitude = 4.3007
    )

    @Test
    fun `new report has open status`() {
        val report = createSafetyReport()

        assertEquals(ReportStatus.OPEN, report.status)
        assertFalse(report.isResolved)
    }

    @Test
    fun `blank title is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            SafetyReport(
                title = "   ",
                description = null,
                photo = null,
                location = location,
                createdBy = owner,
                urgency = Urgency.LOW
            )
        }
    }

    @Test
    fun `every subtype reports its own type`() {
        val reports: List<Report> = listOf(
            createSafetyReport(),
            NuisanceReport(
                title = "Loud music",
                description = null,
                photo = null,
                location = location,
                createdBy = owner,
                nuisanceType = NuisanceType.NOISE
            ),
            HelpRequest(
                title = "Need help",
                description = null,
                photo = null,
                location = location,
                createdBy = owner
            )
        )

        assertEquals(
            listOf(ReportType.SAFETY, ReportType.NUISANCE, ReportType.HELP),
            reports.map { report -> report.type }
        )
    }

    @Test
    fun `owner can resolve own report`() {
        val report = createSafetyReport()

        report.resolve(resolvedBy = owner)

        assertEquals(ReportStatus.RESOLVED, report.status)
        assertTrue(report.isResolved)
    }

    @Test
    fun `admin can resolve report of another user`() {
        val report = createSafetyReport()

        report.resolve(resolvedBy = admin)

        assertEquals(ReportStatus.RESOLVED, report.status)
    }

    @Test
    fun `other user cannot resolve report`() {
        val report = createSafetyReport()

        assertFailsWith<NotAllowedException> {
            report.resolve(resolvedBy = otherUser)
        }

        assertEquals(ReportStatus.OPEN, report.status)
    }

    @Test
    fun `resolved report cannot be resolved again`() {
        val report = createSafetyReport()

        report.resolve(resolvedBy = owner)

        assertFailsWith<IllegalStateException> {
            report.resolve(resolvedBy = owner)
        }
    }

    @Test
    fun `owner can update own report`() {
        val report = createSafetyReport()
        val newLocation = Location(latitude = 52.08, longitude = 4.31)

        report.update(
            title = "Streetlight still broken",
            description = "Now also the one next to it.",
            photo = "streetlight.jpg",
            location = newLocation,
            updatedBy = owner
        )

        assertEquals("Streetlight still broken", report.title)
        assertEquals("Now also the one next to it.", report.description)
        assertEquals("streetlight.jpg", report.photo)
        assertEquals(newLocation, report.location)
    }

    @Test
    fun `other user cannot update report`() {
        val report = createSafetyReport()

        assertFailsWith<NotAllowedException> {
            report.update(
                title = "Changed by someone else",
                description = null,
                photo = null,
                location = location,
                updatedBy = otherUser
            )
        }

        assertEquals("Broken streetlight", report.title)
    }

    @Test
    fun `resolved report cannot be updated`() {
        val report = createSafetyReport()

        report.resolve(resolvedBy = owner)

        assertFailsWith<IllegalStateException> {
            report.update(
                title = "Too late",
                description = null,
                photo = null,
                location = location,
                updatedBy = owner
            )
        }
    }

    @Test
    fun `update with blank title is rejected and keeps the old title`() {
        val report = createSafetyReport()

        assertFailsWith<IllegalArgumentException> {
            report.update(
                title = "",
                description = null,
                photo = null,
                location = location,
                updatedBy = owner
            )
        }

        assertEquals("Broken streetlight", report.title)
    }

    @Test
    fun `only owner and admin can manage a report`() {
        val report = createSafetyReport()

        assertTrue(report.canBeManagedBy(owner))
        assertTrue(report.canBeManagedBy(admin))
        assertFalse(report.canBeManagedBy(otherUser))
    }

    @Test
    fun `search matches title and description case insensitive`() {
        val report = createSafetyReport()

        assertTrue(report.matchesSearch("STREETLIGHT"))
        assertTrue(report.matchesSearch("  dark  "))
        assertFalse(report.matchesSearch("graffiti"))
    }

    private fun createSafetyReport(): SafetyReport {
        return SafetyReport(
            title = "Broken streetlight",
            description = "The street is very dark at night.",
            photo = null,
            location = location,
            createdBy = owner,
            urgency = Urgency.MEDIUM
        )
    }
}

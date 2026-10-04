package nl.brul.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReportTest {

    private val user = User(
        id = "user-1",
        username = "testuser",
        role = Role.USER
    )

    private val location = Location(
        latitude = 52.0705,
        longitude = 4.3007
    )

    @Test
    fun `new report has open status`() {
        val report = SafetyReport(
            title = "Broken streetlight",
            description = "Streetlight is not working.",
            photo = null,
            location = location,
            createdBy = user,
            urgency = Urgency.MEDIUM
        )

        assertEquals(ReportStatus.OPEN, report.status)
    }

    @Test
    fun `blank title is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            SafetyReport(
                title = "   ",
                description = null,
                photo = null,
                location = location,
                createdBy = user,
                urgency = Urgency.LOW
            )
        }
    }

    @Test
    fun `open report can be resolved`() {
        val report = SafetyReport(
            title = "Unsafe crossing",
            description = null,
            photo = null,
            location = location,
            createdBy = user,
            urgency = Urgency.HIGH
        )

        report.resolve()

        assertEquals(ReportStatus.RESOLVED, report.status)
    }

    @Test
    fun `resolved report cannot be resolved again`() {
        val report = SafetyReport(
            title = "Unsafe crossing",
            description = null,
            photo = null,
            location = location,
            createdBy = user,
            urgency = Urgency.HIGH
        )

        report.resolve()

        assertFailsWith<IllegalStateException> {
            report.resolve()
        }
    }
}
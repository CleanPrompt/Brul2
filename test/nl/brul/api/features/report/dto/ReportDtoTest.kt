// test/nl/brul/api/features/report/dto/ReportDtoTest.kt
package nl.brul.api.features.report.dto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.brul.api.features.report.model.HelpRequest
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.NuisanceReport
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportStatus
import nl.brul.api.features.report.model.ReportType
import nl.brul.api.features.report.model.SafetyReport

class ReportDtoTest {

    private val location = Location(52.0705, 4.3007)

    private fun safetyRequest() = CreateReportRequest(
        type = "SAFETY",
        title = "Unsafe crossing",
        description = "Cars drive too fast near the school",
        category = "TRAFFIC_SAFETY",
        latitude = location.latitude,
        longitude = location.longitude,
        urgent = true
    )

    private fun existingSafetyReport() = SafetyReport(
        id = 10,
        title = "Unsafe crossing",
        description = "Cars drive too fast near the school",
        category = ReportCategory.TRAFFIC_SAFETY,
        location = location,
        urgent = true,
        createdAt = 123456789L,
        viewCount = 15
    )

    @Test
    fun `create request converts to safety report data`() {
        val result = safetyRequest().toNewReport()

        assertEquals(ReportType.SAFETY, result.type)
        assertEquals(ReportCategory.TRAFFIC_SAFETY, result.category)
        assertEquals(true, result.urgent)
        assertEquals(location, result.location)
    }

    @Test
    fun `create request converts to nuisance report data`() {
        val result = CreateReportRequest(
            type = "NUISANCE",
            title = "Loud music",
            description = "Music during the night",
            category = "NOISE",
            latitude = 52.0,
            longitude = 4.0
        ).toNewReport()

        assertEquals(ReportType.NUISANCE, result.type)
        assertEquals(ReportCategory.NOISE, result.category)
    }

    @Test
    fun `create request converts to help request data`() {
        val result = CreateReportRequest(
            type = "HELP_REQUEST",
            title = "Help with groceries",
            description = "Looking for someone to buy groceries",
            category = "GROCERIES",
            latitude = 52.0,
            longitude = 4.0,
            volunteersNeeded = 2
        ).toNewReport()

        assertEquals(ReportType.HELP_REQUEST, result.type)
        assertEquals(2, result.volunteersNeeded)
    }

    @Test
    fun `invalid report type is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            safetyRequest().copy(type = "UNKNOWN").toNewReport()
        }
    }

    @Test
    fun `invalid category is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            safetyRequest().copy(category = "UNKNOWN").toNewReport()
        }
    }

    @Test
    fun `wrong category for report type is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            safetyRequest().copy(category = "NOISE").toNewReport()
        }
    }

    @Test
    fun `missing safety field is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            safetyRequest().copy(urgent = null).toNewReport()
        }
    }

    @Test
    fun `update request preserves metadata`() {
        val existing = existingSafetyReport()

        val request = UpdateReportRequest(
            type = "SAFETY",
            title = "Updated crossing",
            description = "The situation is still dangerous",
            category = "TRAFFIC_SAFETY",
            latitude = location.latitude,
            longitude = location.longitude,
            status = "IN_PROGRESS",
            urgent = false
        )

        val updated = request.toReport(existing)

        assertEquals(existing.id, updated.id)
        assertEquals(existing.createdAt, updated.createdAt)
        assertEquals(existing.viewCount, updated.viewCount)
        assertEquals(ReportStatus.IN_PROGRESS, updated.status)
        assertEquals("Updated crossing", updated.title)
        assertFalse((updated as SafetyReport).urgent)
    }

    @Test
    fun `update cannot change report type`() {
        val request = UpdateReportRequest(
            type = "NUISANCE",
            title = "Different type",
            description = "Trying to change the report type",
            category = "NOISE",
            latitude = 52.0,
            longitude = 4.0,
            status = "OPEN"
        )

        assertFailsWith<IllegalArgumentException> {
            request.toReport(existingSafetyReport())
        }
    }

    @Test
    fun `invalid update status is rejected`() {
        val request = UpdateReportRequest(
            type = "SAFETY",
            title = "Updated crossing",
            description = "The situation is still dangerous",
            category = "TRAFFIC_SAFETY",
            latitude = 52.0,
            longitude = 4.0,
            status = "UNKNOWN",
            urgent = true
        )

        assertFailsWith<IllegalArgumentException> {
            request.toReport(existingSafetyReport())
        }
    }

    @Test
    fun `response contains safety metadata`() {
        val response = existingSafetyReport().toResponse()

        assertEquals(10L, response.id)
        assertEquals("SAFETY", response.type)
        assertEquals("OPEN", response.status)
        assertEquals(true, response.urgent)
        assertEquals(null, response.volunteersNeeded)
        assertEquals(123456789L, response.createdAt)
        assertEquals(15, response.viewCount)
    }

    @Test
    fun `response serializes to JSON`() {
        val report = HelpRequest(
            id = 5,
            title = "Help with groceries",
            description = "Looking for help with shopping",
            category = ReportCategory.GROCERIES,
            location = location,
            volunteersNeeded = 2,
            createdAt = 123456789L
        )

        val json = Json.encodeToString(report.toResponse())

        assertTrue(json.contains("\"type\":\"HELP_REQUEST\""))
        assertTrue(json.contains("\"volunteersNeeded\":2"))
        assertTrue(json.contains("\"createdAt\":123456789"))
    }
}
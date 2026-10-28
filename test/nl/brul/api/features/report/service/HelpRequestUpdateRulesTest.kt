// test/nl/brul/api/features/report/service/HelpRequestUpdateRulesTest.kt
package nl.brul.api.features.report.service

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import nl.brul.api.common.error.DomainConflictException
import nl.brul.api.features.report.model.HelpRequest
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportStatus
import nl.brul.api.features.report.model.ReportType
import nl.brul.api.features.report.model.TimeSlot

class HelpRequestUpdateRulesTest {

    private fun at(time: String): Instant = Instant.parse("2026-11-20T${time}:00Z")

    private fun helpRequest(
        places: Int = 2,
        start: String = "10:00",
        end: String = "12:00"
    ) = HelpRequest(
        id = 1,
        title = "Help with groceries",
        description = "Carrying groceries upstairs",
        category = ReportCategory.GROCERIES,
        location = Location(52.0, 4.0),
        volunteersNeeded = places,
        slot = TimeSlot(at(start), at(end)),
        ownerId = 1
    )

    @Test
    fun `capacity cannot drop below the signed up volunteers (R10)`() {
        assertFailsWith<IllegalArgumentException> {
            HelpRequestUpdateRules.validate(
                existing = helpRequest(places = 5),
                updated = helpRequest(places = 2),
                signedUpCount = 3,
                now = at("08:00")
            )
        }
    }

    @Test
    fun `slot can move while nobody is signed up (R12)`() {
        HelpRequestUpdateRules.validate(
            existing = helpRequest(),
            updated = helpRequest(start = "14:00", end = "16:00"),
            signedUpCount = 0,
            now = at("08:00")
        )
    }

    @Test
    fun `slot is locked once volunteers signed up (R12)`() {
        val error = assertFailsWith<DomainConflictException> {
            HelpRequestUpdateRules.validate(
                existing = helpRequest(),
                updated = helpRequest(start = "14:00", end = "16:00"),
                signedUpCount = 1,
                now = at("08:00")
            )
        }

        assertEquals("SLOT_LOCKED", error.code)
    }

    @Test
    fun `slot is locked once the help request started (R12)`() {
        val error = assertFailsWith<DomainConflictException> {
            HelpRequestUpdateRules.validate(
                existing = helpRequest(),
                updated = helpRequest(start = "14:00", end = "16:00"),
                signedUpCount = 0,
                now = at("10:30")
            )
        }

        assertEquals("SLOT_LOCKED", error.code)
    }

    @Test
    fun `slot cannot move into the past`() {
        assertFailsWith<IllegalArgumentException> {
            HelpRequestUpdateRules.validate(
                existing = helpRequest(start = "10:00", end = "12:00"),
                updated = helpRequest(start = "07:00", end = "09:00"),
                signedUpCount = 0,
                now = at("08:00")
            )
        }
    }

    @Test
    fun `help request can only be closed by hand (R14)`() {
        assertFailsWith<IllegalArgumentException> {
            ReportStatusRules.validateTransition(
                ReportStatus.OPEN,
                ReportStatus.IN_PROGRESS,
                ReportType.HELP_REQUEST
            )
        }

        ReportStatusRules.validateTransition(
            ReportStatus.OPEN,
            ReportStatus.RESOLVED,
            ReportType.HELP_REQUEST
        )

        // Other report types keep the old rules.
        ReportStatusRules.validateTransition(
            ReportStatus.OPEN,
            ReportStatus.IN_PROGRESS,
            ReportType.SAFETY
        )
    }
}

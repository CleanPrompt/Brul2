// test/nl/brul/api/features/help/model/HelpRequestBookingTest.kt
package nl.brul.api.features.help.model

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import nl.brul.api.features.help.model.ParticipationStatus.ATTENDED
import nl.brul.api.features.help.model.ParticipationStatus.CANCELLED
import nl.brul.api.features.help.model.ParticipationStatus.LATE_CANCELLED
import nl.brul.api.features.help.model.ParticipationStatus.NO_SHOW
import nl.brul.api.features.help.model.ParticipationStatus.SIGNED_UP
import nl.brul.api.features.help.model.ParticipationStatus.WAITLISTED
import nl.brul.api.features.report.model.HelpRequest
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportStatus
import nl.brul.api.features.report.model.TimeSlot

/**
 * Pure unit tests of the booking rules: no database, no real clock.
 *
 * Starting point in every test (see the design document):
 * help request H, owned by user 1, slot 10:00-12:00 on 20 November 2026,
 * 2 places. "now" is passed in explicitly.
 */
class HelpRequestBookingTest {

    private val owner = 1L

    private fun at(time: String): Instant = Instant.parse("2026-11-20T${time}:00Z")

    private fun slot(start: String, end: String) = TimeSlot(at(start), at(end))

    private fun helpRequest(
        places: Int = 2,
        status: ReportStatus = ReportStatus.OPEN
    ) = HelpRequest(
        id = 10,
        title = "Help with groceries",
        description = "Carrying groceries upstairs",
        category = ReportCategory.GROCERIES,
        location = Location(52.0, 4.0),
        volunteersNeeded = places,
        slot = slot("10:00", "12:00"),
        status = status,
        ownerId = owner
    )

    private var nextId = 1L

    private fun participation(
        userId: Long,
        status: ParticipationStatus,
        joinedAt: String = "07:00"
    ) = HelpParticipation(
        id = nextId++,
        helpRequestId = 10,
        userId = userId,
        status = status,
        joinedAt = at(joinedAt)
    )

    private fun booking(
        vararg participations: HelpParticipation,
        places: Int = 2,
        status: ReportStatus = ReportStatus.OPEN
    ) = HelpRequestBooking(helpRequest(places, status), participations.toList())

    private fun volunteer(
        userId: Long,
        commitments: List<TimeSlot> = emptyList(),
        noShows: List<Instant> = emptyList()
    ) = VolunteerRecord(userId, commitments, noShows)

    // ---- phase -------------------------------------------------------------

    @Test
    fun `phase follows time, capacity and status`() {
        val full = booking(participation(2, SIGNED_UP), participation(3, SIGNED_UP))

        assertEquals(HelpRequestPhase.OPEN, booking().phaseAt(at("08:00")))
        assertEquals(HelpRequestPhase.FULL, full.phaseAt(at("08:00")))
        assertEquals(HelpRequestPhase.IN_PROGRESS, full.phaseAt(at("10:00")))
        assertEquals(HelpRequestPhase.COMPLETED, full.phaseAt(at("12:00")))
        assertEquals(
            HelpRequestPhase.CLOSED,
            booking(status = ReportStatus.RESOLVED).phaseAt(at("08:00"))
        )
    }

    // ---- join (R2-R6, R11) -------------------------------------------------

    @Test
    fun `volunteer can sign up for an open help request`() {
        assertEquals(JoinOutcome.SignedUp, booking().join(volunteer(2), at("08:00")))
    }

    @Test
    fun `owner cannot sign up for their own request (R2)`() {
        assertEquals(
            JoinOutcome.Rejected(JoinRejection.OWN_REQUEST),
            booking().join(volunteer(owner), at("08:00"))
        )
    }

    // T3
    @Test
    fun `cannot sign up once the help request has started (R3)`() {
        assertEquals(
            JoinOutcome.Rejected(JoinRejection.NOT_OPEN),
            booking().join(volunteer(2), at("10:30"))
        )
    }

    @Test
    fun `cannot sign up for a closed help request (R3)`() {
        assertEquals(
            JoinOutcome.Rejected(JoinRejection.NOT_OPEN),
            booking(status = ReportStatus.RESOLVED).join(volunteer(2), at("08:00"))
        )
    }

    // T4
    @Test
    fun `cannot sign up when busy elsewhere at the same time (R5)`() {
        val busy = volunteer(2, commitments = listOf(slot("11:00", "13:00")))

        assertEquals(
            JoinOutcome.Rejected(JoinRejection.TIME_CONFLICT),
            booking().join(busy, at("08:00"))
        )
    }

    @Test
    fun `a commitment that only touches the slot is no conflict (R5)`() {
        val afterwards = volunteer(2, commitments = listOf(slot("12:00", "14:00")))

        assertEquals(JoinOutcome.SignedUp, booking().join(afterwards, at("08:00")))
    }

    // T5
    @Test
    fun `full help request puts new volunteers on the waitlist in order (R6)`() {
        val full = booking(participation(2, SIGNED_UP), participation(3, SIGNED_UP))

        assertEquals(JoinOutcome.Waitlisted(1), full.join(volunteer(4), at("08:00")))

        val withWaitlist = booking(
            participation(2, SIGNED_UP),
            participation(3, SIGNED_UP),
            participation(4, WAITLISTED)
        )

        assertEquals(JoinOutcome.Waitlisted(2), withWaitlist.join(volunteer(5), at("08:00")))
    }

    @Test
    fun `volunteer cannot sign up twice (R4)`() {
        val signedUp = booking(participation(2, SIGNED_UP))
        val waitlisted = booking(participation(2, WAITLISTED))

        assertEquals(
            JoinOutcome.Rejected(JoinRejection.ALREADY_JOINED),
            signedUp.join(volunteer(2), at("08:00"))
        )
        assertEquals(
            JoinOutcome.Rejected(JoinRejection.ALREADY_JOINED),
            waitlisted.join(volunteer(2), at("08:00"))
        )
    }

    // T10
    @Test
    fun `signing up again is allowed after a normal cancellation only (R4)`() {
        assertEquals(
            JoinOutcome.SignedUp,
            booking(participation(2, CANCELLED)).join(volunteer(2), at("08:00"))
        )
        assertEquals(
            JoinOutcome.Rejected(JoinRejection.ALREADY_JOINED),
            booking(participation(2, LATE_CANCELLED)).join(volunteer(2), at("08:00"))
        )
    }

    @Test
    fun `blocked volunteer cannot sign up (R11)`() {
        val unreliable = volunteer(
            2,
            noShows = listOf(at("07:00").minusSeconds(3600), at("06:00"), at("05:00"))
        )

        assertEquals(
            JoinOutcome.Rejected(JoinRejection.BLOCKED),
            booking().join(unreliable, at("08:00"))
        )
    }

    // ---- leave (R7) --------------------------------------------------------

    // T6
    @Test
    fun `leaving two hours or more before the start is a normal cancellation (R7)`() {
        val outcome = booking(participation(2, SIGNED_UP)).leave(2, at("08:00"))

        assertIs<LeaveOutcome.Left>(outcome)
        assertEquals(CANCELLED, outcome.participation.status)
        assertTrue(outcome.freedPlace)
    }

    @Test
    fun `leaving within two hours of the start is a late cancellation (R7)`() {
        val outcome = booking(participation(2, SIGNED_UP)).leave(2, at("08:30"))

        assertIs<LeaveOutcome.Left>(outcome)
        assertEquals(LATE_CANCELLED, outcome.participation.status)
    }

    @Test
    fun `leaving after the start is not possible (R7)`() {
        assertEquals(
            LeaveOutcome.Rejected(LeaveRejection.ALREADY_STARTED),
            booking(participation(2, SIGNED_UP)).leave(2, at("10:00"))
        )
    }

    @Test
    fun `leaving the waitlist frees no place`() {
        val outcome = booking(participation(2, WAITLISTED)).leave(2, at("09:30"))

        assertIs<LeaveOutcome.Left>(outcome)
        assertEquals(CANCELLED, outcome.participation.status)
        assertEquals(false, outcome.freedPlace)
    }

    @Test
    fun `cannot leave without participating`() {
        assertEquals(
            LeaveOutcome.Rejected(LeaveRejection.NOT_PARTICIPATING),
            booking().leave(2, at("08:00"))
        )
    }

    // ---- promotions (R8) ---------------------------------------------------

    // T7
    @Test
    fun `waitlist promotion skips a volunteer who is busy elsewhere (R8)`() {
        val booking = booking(
            participation(2, SIGNED_UP),
            participation(3, WAITLISTED, joinedAt = "07:10"), // A: busy elsewhere
            participation(4, WAITLISTED, joinedAt = "07:20") // B
        )

        val promoted = booking.promotions(
            volunteerOf = { userId ->
                if (userId == 3L) {
                    volunteer(3, commitments = listOf(slot("11:00", "12:00")))
                } else {
                    volunteer(userId)
                }
            },
            now = at("08:00")
        )

        assertEquals(listOf(4L), promoted.map { it.userId })
        assertEquals(SIGNED_UP, promoted.single().status)
    }

    @Test
    fun `promotion fills only the free places, first come first served (R8)`() {
        val booking = booking(
            participation(2, WAITLISTED, joinedAt = "07:30"),
            participation(3, WAITLISTED, joinedAt = "07:10"),
            participation(4, WAITLISTED, joinedAt = "07:20"),
            places = 2
        )

        val promoted = booking.promotions({ volunteer(it) }, at("08:00"))

        assertEquals(listOf(3L, 4L), promoted.map { it.userId })
    }

    @Test
    fun `no promotions after the start`() {
        val booking = booking(participation(2, WAITLISTED))

        assertEquals(emptyList(), booking.promotions({ volunteer(it) }, at("10:00")))
    }

    // ---- attendance (R9) ---------------------------------------------------

    // T8
    @Test
    fun `owner records attendance only after the start (R9)`() {
        val booking = booking(participation(2, SIGNED_UP))

        assertEquals(
            AttendanceOutcome.Rejected(AttendanceRejection.NOT_STARTED),
            booking.markAttendance(owner, 2, attended = true, now = at("09:59"))
        )

        val marked = booking.markAttendance(owner, 2, attended = true, now = at("12:30"))

        assertIs<AttendanceOutcome.Marked>(marked)
        assertEquals(ATTENDED, marked.participation.status)

        val noShow = booking.markAttendance(owner, 2, attended = false, now = at("12:30"))

        assertIs<AttendanceOutcome.Marked>(noShow)
        assertEquals(NO_SHOW, noShow.participation.status)
    }

    @Test
    fun `only the owner records attendance (R9)`() {
        assertEquals(
            AttendanceOutcome.Rejected(AttendanceRejection.NOT_OWNER),
            booking(participation(2, SIGNED_UP)).markAttendance(3, 2, true, at("12:30"))
        )
    }

    @Test
    fun `attendance only for volunteers holding a place (R9)`() {
        assertEquals(
            AttendanceOutcome.Rejected(AttendanceRejection.NOT_SIGNED_UP),
            booking(participation(2, WAITLISTED)).markAttendance(owner, 2, true, at("12:30"))
        )
    }

    // ---- closing (R13) -----------------------------------------------------

    @Test
    fun `closing before the start cancels every active participation (R13)`() {
        val booking = booking(
            participation(2, SIGNED_UP),
            participation(3, WAITLISTED),
            participation(4, LATE_CANCELLED)
        )

        val cancelled = booking.cancellationsOnClose(at("08:00"))

        assertEquals(listOf(2L, 3L), cancelled.map { it.userId })
        assertTrue(cancelled.all { it.status == CANCELLED })
    }

    @Test
    fun `closing after the start keeps the participations`() {
        val booking = booking(participation(2, SIGNED_UP))

        assertEquals(emptyList(), booking.cancellationsOnClose(at("10:30")))
    }
}

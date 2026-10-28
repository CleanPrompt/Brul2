// src/nl/brul/api/features/help/model/HelpRequestBooking.kt
package nl.brul.api.features.help.model

import java.time.Instant
import nl.brul.api.features.report.model.HelpRequest
import nl.brul.api.features.report.model.ReportStatus

/**
 * Aggregate: one help request together with all its participations.
 *
 * Every booking rule about a single help request lives here, comparable to
 * the reservations of one car in a rent-my-car system. The class is pure and
 * immutable: methods receive `now` and return a decision. The service loads
 * the data, asks this class what to do and saves the result in one
 * transaction.
 */
class HelpRequestBooking(
    val request: HelpRequest,
    participations: List<HelpParticipation>
) {
    val participations: List<HelpParticipation> = participations.toList()

    init {
        require(this.participations.all { it.helpRequestId == request.id }) {
            "All participations must belong to this help request."
        }
    }

    val signedUp: List<HelpParticipation>
        get() = participations.filter {
            it.status == ParticipationStatus.SIGNED_UP
        }

    /** R6: first come, first served. */
    val waitlist: List<HelpParticipation>
        get() = participations
            .filter { it.status == ParticipationStatus.WAITLISTED }
            .sortedWith(compareBy({ it.joinedAt }, { it.id }))

    val availablePlaces: Int
        get() = (request.volunteersNeeded - signedUp.size).coerceAtLeast(0)

    fun participationOf(userId: Long): HelpParticipation? =
        participations.find { it.userId == userId }

    /** Position on the waitlist (1 = next in line), or null. */
    fun waitlistPositionOf(userId: Long): Int? =
        waitlist.indexOfFirst { it.userId == userId }
            .takeIf { it >= 0 }
            ?.plus(1)

    /** The phase follows from status, time and capacity; it is never stored. */
    fun phaseAt(now: Instant): HelpRequestPhase = when {
        request.status == ReportStatus.RESOLVED -> HelpRequestPhase.CLOSED
        request.slot.hasEnded(now) -> HelpRequestPhase.COMPLETED
        request.slot.hasStarted(now) -> HelpRequestPhase.IN_PROGRESS
        signedUp.size >= request.volunteersNeeded -> HelpRequestPhase.FULL
        else -> HelpRequestPhase.OPEN
    }

    /** R2-R6 and R11: may this volunteer sign up, and where do they end up? */
    fun join(volunteer: VolunteerRecord, now: Instant): JoinOutcome {
        val phase = phaseAt(now)
        val existing = participationOf(volunteer.userId)

        return when {
            request.ownerId == volunteer.userId ->
                JoinOutcome.Rejected(JoinRejection.OWN_REQUEST)

            phase != HelpRequestPhase.OPEN && phase != HelpRequestPhase.FULL ->
                JoinOutcome.Rejected(JoinRejection.NOT_OPEN)

            // Only a normal cancellation can be undone by signing up again.
            existing != null && existing.status != ParticipationStatus.CANCELLED ->
                JoinOutcome.Rejected(JoinRejection.ALREADY_JOINED)

            volunteer.isBlocked(now) ->
                JoinOutcome.Rejected(JoinRejection.BLOCKED)

            volunteer.hasConflictWith(request.slot) ->
                JoinOutcome.Rejected(JoinRejection.TIME_CONFLICT)

            phase == HelpRequestPhase.FULL ->
                JoinOutcome.Waitlisted(position = waitlist.size + 1)

            else -> JoinOutcome.SignedUp
        }
    }

    /** R7: leaving is free until two hours before the start, late after that. */
    fun leave(userId: Long, now: Instant): LeaveOutcome {
        val participation = participationOf(userId)

        if (participation == null || !participation.isActive) {
            return LeaveOutcome.Rejected(LeaveRejection.NOT_PARTICIPATING)
        }

        if (request.slot.hasStarted(now)) {
            return LeaveOutcome.Rejected(LeaveRejection.ALREADY_STARTED)
        }

        if (participation.status == ParticipationStatus.WAITLISTED) {
            return LeaveOutcome.Left(
                participation = participation.moveTo(ParticipationStatus.CANCELLED, now),
                freedPlace = false
            )
        }

        val onTime = request.slot.timeUntilStart(now) >= BookingRules.LATE_CANCELLATION_WINDOW

        val next = if (onTime) {
            ParticipationStatus.CANCELLED
        } else {
            ParticipationStatus.LATE_CANCELLED
        }

        return LeaveOutcome.Left(
            participation = participation.moveTo(next, now),
            freedPlace = true
        )
    }

    /**
     * R8: who moves up from the waitlist into free places.
     *
     * Candidates are taken in waitlist order. A candidate who is now busy
     * elsewhere (R5) or blocked (R11) is skipped and keeps their spot.
     * Returns the promoted participations, ready to be saved.
     */
    fun promotions(
        volunteerOf: (Long) -> VolunteerRecord,
        now: Instant
    ): List<HelpParticipation> {
        if (phaseAt(now) != HelpRequestPhase.OPEN) {
            return emptyList()
        }

        val promoted = mutableListOf<HelpParticipation>()

        for (candidate in waitlist) {
            if (promoted.size == availablePlaces) {
                break
            }

            val volunteer = volunteerOf(candidate.userId)

            if (volunteer.isBlocked(now) || volunteer.hasConflictWith(request.slot)) {
                continue
            }

            promoted += candidate.moveTo(ParticipationStatus.SIGNED_UP, now)
        }

        return promoted
    }

    /** R9: after the start, the owner records who came and who did not. */
    fun markAttendance(
        actingUserId: Long,
        volunteerUserId: Long,
        attended: Boolean,
        now: Instant
    ): AttendanceOutcome {
        val participation = participationOf(volunteerUserId)

        return when {
            request.ownerId != actingUserId ->
                AttendanceOutcome.Rejected(AttendanceRejection.NOT_OWNER)

            !request.slot.hasStarted(now) ->
                AttendanceOutcome.Rejected(AttendanceRejection.NOT_STARTED)

            participation == null || participation.status != ParticipationStatus.SIGNED_UP ->
                AttendanceOutcome.Rejected(AttendanceRejection.NOT_SIGNED_UP)

            else -> {
                val next = if (attended) {
                    ParticipationStatus.ATTENDED
                } else {
                    ParticipationStatus.NO_SHOW
                }

                AttendanceOutcome.Marked(participation.moveTo(next, now))
            }
        }
    }

    /**
     * R13: closing before the start cancels every active participation.
     * Nobody is blamed for this, so it is a normal cancellation.
     */
    fun cancellationsOnClose(now: Instant): List<HelpParticipation> {
        if (request.slot.hasStarted(now)) {
            return emptyList()
        }

        return participations
            .filter { it.isActive }
            .map { it.moveTo(ParticipationStatus.CANCELLED, now) }
    }
}

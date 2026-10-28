// src/nl/brul/api/features/help/service/HelpParticipationService.kt
package nl.brul.api.features.help.service

import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import nl.brul.api.database.NoTransactionRunner
import nl.brul.api.database.TransactionRunner
import nl.brul.api.features.help.model.AttendanceOutcome
import nl.brul.api.features.help.model.AttendanceRejection
import nl.brul.api.features.help.model.HelpParticipation
import nl.brul.api.features.help.model.HelpRequestBooking
import nl.brul.api.features.help.model.HelpRequestPhase
import nl.brul.api.features.help.model.JoinOutcome
import nl.brul.api.features.help.model.JoinRejection
import nl.brul.api.features.help.model.LeaveOutcome
import nl.brul.api.features.help.model.LeaveRejection
import nl.brul.api.features.help.model.ParticipationStatus
import nl.brul.api.features.help.model.VolunteerRecord
import nl.brul.api.features.help.repository.HelpParticipationRepository
import nl.brul.api.features.report.model.HelpRequest
import nl.brul.api.features.report.model.TimeSlot
import nl.brul.api.features.report.repository.ReportRepository

data class HelpParticipationSummary(
    val helpRequestId: Long,
    val volunteersNeeded: Int,
    val participantCount: Int,
    val availablePlaces: Int,
    val waitlistCount: Int,
    val phase: HelpRequestPhase,
    val slot: TimeSlot
)

/** Result of signing up: the saved participation and, if waitlisted, the position. */
data class JoinResult(
    val participation: HelpParticipation,
    val waitlistPosition: Int?
)

/** One line in a volunteer's agenda. */
data class AgendaItem(
    val participation: HelpParticipation,
    val helpRequest: HelpRequest,
    val phase: HelpRequestPhase
)

data class Reliability(
    val noShowsInWindow: Int,
    val blocked: Boolean,
    val blockedUntil: Instant?
)

/**
 * Application service for help participation.
 *
 * It does not contain the booking rules itself. Every action follows the
 * same steps inside one transaction:
 *   1. lock the help request row (so concurrent requests wait their turn),
 *   2. load the HelpRequestBooking and the VolunteerRecord,
 *   3. ask the domain model for a decision,
 *   4. save the result, or turn a rejection into the matching error.
 */
class HelpParticipationService(
    private val participationRepository: HelpParticipationRepository,
    private val reportRepository: ReportRepository,
    private val clock: Clock = Clock.systemUTC(),
    private val transactions: TransactionRunner = NoTransactionRunner
) {

    fun join(
        helpRequestId: Long,
        userId: Long
    ): JoinResult {
        require(userId > 0) {
            "User ID must be positive."
        }

        return transactions.inTransaction {
            val now = now()
            val booking = loadBookingForUpdate(helpRequestId)
            val volunteer = loadVolunteer(userId, excludeHelpRequestId = helpRequestId)

            val (status, position) = when (val outcome = booking.join(volunteer, now)) {
                JoinOutcome.SignedUp ->
                    ParticipationStatus.SIGNED_UP to null

                is JoinOutcome.Waitlisted ->
                    ParticipationStatus.WAITLISTED to outcome.position

                is JoinOutcome.Rejected ->
                    throw joinError(outcome.reason)
            }

            val existing = booking.participationOf(userId)

            val saved = if (existing == null) {
                participationRepository.add(
                    helpRequestId = helpRequestId,
                    userId = userId,
                    status = status,
                    joinedAt = now
                )
            } else {
                // Signing up again after a normal cancellation (R4).
                existing.moveTo(status, now).also {
                    participationRepository.update(it)
                }
            }

            JoinResult(saved, position)
        }
    }

    /** Returns the participation with its new status (CANCELLED or LATE_CANCELLED). */
    fun leave(
        helpRequestId: Long,
        userId: Long
    ): HelpParticipation {
        require(userId > 0) {
            "User ID must be positive."
        }

        return transactions.inTransaction {
            val now = now()
            val booking = loadBookingForUpdate(helpRequestId)

            when (val outcome = booking.leave(userId, now)) {
                is LeaveOutcome.Rejected -> throw when (outcome.reason) {
                    LeaveRejection.NOT_PARTICIPATING ->
                        HelpParticipationNotFoundException()

                    LeaveRejection.ALREADY_STARTED ->
                        HelpParticipationConflictException(
                            code = "ALREADY_STARTED",
                            message = "You cannot leave a help request that has already started."
                        )
                }

                is LeaveOutcome.Left -> {
                    participationRepository.update(outcome.participation)

                    if (outcome.freedPlace) {
                        promoteWaitlist(helpRequestId, now)
                    }

                    outcome.participation
                }
            }
        }
    }

    /** R9: the owner records whether a volunteer came. */
    fun markAttendance(
        helpRequestId: Long,
        actingUserId: Long,
        volunteerUserId: Long,
        attended: Boolean
    ): HelpParticipation = transactions.inTransaction {
        val now = now()
        val booking = loadBookingForUpdate(helpRequestId)

        when (val outcome = booking.markAttendance(actingUserId, volunteerUserId, attended, now)) {
            is AttendanceOutcome.Marked -> {
                participationRepository.update(outcome.participation)
                outcome.participation
            }

            is AttendanceOutcome.Rejected -> throw when (outcome.reason) {
                AttendanceRejection.NOT_OWNER ->
                    HelpParticipationForbiddenException(
                        code = "NOT_OWNER",
                        message = "Only the owner can record attendance."
                    )

                AttendanceRejection.NOT_STARTED ->
                    HelpParticipationConflictException(
                        code = "NOT_STARTED",
                        message = "Attendance can only be recorded after the help request has started."
                    )

                AttendanceRejection.NOT_SIGNED_UP ->
                    HelpParticipationConflictException(
                        code = "NOT_SIGNED_UP",
                        message = "This volunteer does not hold a place in this help request."
                    )
            }
        }
    }

    /**
     * Called after the owner changed a help request:
     * closing it cancels everyone (R13), extra places promote the waitlist (R10).
     */
    fun afterHelpRequestUpdated(
        helpRequestId: Long
    ) {
        transactions.inTransaction {
            val now = now()
            val booking = loadBookingForUpdate(helpRequestId)

            if (booking.phaseAt(now) == HelpRequestPhase.CLOSED) {
                booking.cancellationsOnClose(now).forEach {
                    participationRepository.update(it)
                }
            } else {
                promoteWaitlist(helpRequestId, now)
            }
        }
    }

    fun getParticipants(
        helpRequestId: Long
    ): List<HelpParticipation> {
        findHelpRequest(helpRequestId)

        return participationRepository.findByHelpRequest(helpRequestId)
    }

    fun getSummary(
        helpRequestId: Long
    ): HelpParticipationSummary {
        val booking = loadBooking(helpRequestId)

        return HelpParticipationSummary(
            helpRequestId = helpRequestId,
            volunteersNeeded = booking.request.volunteersNeeded,
            participantCount = booking.signedUp.size,
            availablePlaces = booking.availablePlaces,
            waitlistCount = booking.waitlist.size,
            phase = booking.phaseAt(now()),
            slot = booking.request.slot
        )
    }

    /** The volunteer's agenda, sorted by start time. */
    fun getAgenda(
        userId: Long,
        upcomingOnly: Boolean
    ): List<AgendaItem> {
        val now = now()

        return participationRepository.findByUser(userId)
            .mapNotNull { participation ->
                val request = reportRepository.findById(participation.helpRequestId)
                    as? HelpRequest
                    ?: return@mapNotNull null

                val booking = HelpRequestBooking(
                    request,
                    participationRepository.findByHelpRequest(request.id)
                )

                AgendaItem(participation, request, booking.phaseAt(now))
            }
            .filter { item ->
                !upcomingOnly ||
                        (item.participation.isActive && !item.helpRequest.slot.hasEnded(now))
            }
            .sortedBy { it.helpRequest.slot.start }
    }

    fun getReliability(
        userId: Long
    ): Reliability {
        val now = now()
        val volunteer = loadVolunteer(userId, excludeHelpRequestId = null)

        return Reliability(
            noShowsInWindow = volunteer.recentNoShows(now).size,
            blocked = volunteer.isBlocked(now),
            blockedUntil = volunteer.blockedUntil(now)
        )
    }

    /** R8: fill free places from the waitlist. */
    private fun promoteWaitlist(
        helpRequestId: Long,
        now: Instant
    ) {
        val booking = loadBooking(helpRequestId)

        booking.promotions(
            volunteerOf = { userId -> loadVolunteer(userId, excludeHelpRequestId = helpRequestId) },
            now = now
        ).forEach {
            participationRepository.update(it)
        }
    }

    /** Builds the snapshot that the booking rules need for one volunteer. */
    private fun loadVolunteer(
        userId: Long,
        excludeHelpRequestId: Long?
    ): VolunteerRecord {
        val participations = participationRepository.findByUser(userId)

        val commitments = participations
            .filter {
                it.status == ParticipationStatus.SIGNED_UP &&
                        it.helpRequestId != excludeHelpRequestId
            }
            .mapNotNull {
                (reportRepository.findById(it.helpRequestId) as? HelpRequest)?.slot
            }

        val noShowTimes = participations
            .filter { it.status == ParticipationStatus.NO_SHOW }
            .map { it.changedAt }

        return VolunteerRecord(
            userId = userId,
            commitments = commitments,
            noShowTimes = noShowTimes
        )
    }

    private fun loadBooking(helpRequestId: Long): HelpRequestBooking =
        HelpRequestBooking(
            findHelpRequest(helpRequestId),
            participationRepository.findByHelpRequest(helpRequestId)
        )

    /** Same as [loadBooking], but locks the help request row first. */
    private fun loadBookingForUpdate(helpRequestId: Long): HelpRequestBooking {
        require(helpRequestId > 0) {
            "Help request ID must be positive."
        }

        val request = reportRepository.findByIdForUpdate(helpRequestId) as? HelpRequest
            ?: throw HelpRequestNotFoundException()

        return HelpRequestBooking(
            request,
            participationRepository.findByHelpRequest(helpRequestId)
        )
    }

    private fun findHelpRequest(id: Long): HelpRequest {
        require(id > 0) {
            "Help request ID must be positive."
        }

        return reportRepository.findById(id) as? HelpRequest
            ?: throw HelpRequestNotFoundException()
    }

    // Stored with millisecond precision, so we use the same precision here.
    private fun now(): Instant =
        clock.instant().truncatedTo(ChronoUnit.MILLIS)

    private fun joinError(reason: JoinRejection): RuntimeException = when (reason) {
        JoinRejection.OWN_REQUEST ->
            HelpParticipationForbiddenException()

        JoinRejection.NOT_OPEN ->
            HelpParticipationConflictException(
                code = "NOT_OPEN",
                message = "This help request is no longer open."
            )

        JoinRejection.ALREADY_JOINED ->
            HelpParticipationConflictException(
                code = "ALREADY_JOINED",
                message = "You already participate in this help request."
            )

        JoinRejection.BLOCKED ->
            HelpParticipationConflictException(
                code = "BLOCKED",
                message = "You cannot sign up for a while because of recent no-shows."
            )

        JoinRejection.TIME_CONFLICT ->
            HelpParticipationConflictException(
                code = "TIME_CONFLICT",
                message = "You are already signed up for another help request at this time."
            )
    }
}

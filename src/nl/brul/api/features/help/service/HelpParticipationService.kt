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

/**
 * Samenvatting van één hulpvraag voor de app: hoeveel plekken, hoeveel bezet, hoe lang de
 * wachtlijst is, in welke fase hij staat en wanneer het tijdslot is.
 */
data class HelpParticipationSummary(
    val helpRequestId: Long,
    val volunteersNeeded: Int,
    val participantCount: Int,
    val availablePlaces: Int,
    val waitlistCount: Int,
    val phase: HelpRequestPhase,
    val slot: TimeSlot
)

/** Resultaat van aanmelden: de opgeslagen aanmelding en, bij de wachtlijst, je positie. */
data class JoinResult(
    val participation: HelpParticipation,
    val waitlistPosition: Int?
)

/** Eén regel in de agenda van een vrijwilliger: zijn aanmelding, de hulpvraag en de fase. */
data class AgendaItem(
    val participation: HelpParticipation,
    val helpRequest: HelpRequest,
    val phase: HelpRequestPhase
)

/**
 * Betrouwbaarheid van een vrijwilliger (R11): het aantal no-shows in de laatste 30 dagen, of hij
 * geblokkeerd is en zo ja tot wanneer.
 */
data class Reliability(
    val noShowsInWindow: Int,
    val blocked: Boolean,
    val blockedUntil: Instant?
)

/**
 * Laag: service (de regels).
 *
 * De service van de feature help. De reserveringsregels zelf staan niet hier, maar in
 * HelpRequestBooking (het domein). Elke actie volgt dezelfde stappen, binnen één transactie:
 *
 * 1. de rij van de hulpvraag vergrendelen, zodat gelijktijdige verzoeken op hun beurt wachten
 *    (twee mensen die tegelijk de laatste plek nemen: precies één krijgt hem, zie test T14);
 * 2. de HelpRequestBooking en het VolunteerRecord laden;
 * 3. het domein om een beslissing vragen;
 * 4. het resultaat opslaan, of een afwijzing omzetten in de juiste fout.
 *
 * De klok en de TransactionRunner komen via de constructor binnen (dependency injection): tests
 * geven een vaste tijd mee en hebben geen echte transactie nodig (NoTransactionRunner).
 */
class HelpParticipationService(
    private val participationRepository: HelpParticipationRepository,
    private val reportRepository: ReportRepository,
    private val clock: Clock = Clock.systemUTC(),
    private val transactions: TransactionRunner = NoTransactionRunner
) {

    /**
     * R2 t/m R6 en R11: meldt een vrijwilliger aan. Is de hulpvraag vol, dan komt hij op de
     * wachtlijst en staat zijn positie in het resultaat.
     * Had hij zich eerder gewoon afgemeld, dan wordt die oude aanmelding hergebruikt (R4).
     */
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

    /**
     * R7: meldt een vrijwilliger af en geeft de aanmelding terug met de nieuwe status (CANCELLED of
     * LATE_CANCELLED). Komt er een plek vrij, dan schuift de wachtlijst meteen door (R8).
     */
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

    /** R9: de eigenaar legt vast of een vrijwilliger is gekomen. */
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
     * Wordt aangeroepen door ReportService nadat de eigenaar een hulpvraag heeft gewijzigd:
     * sluiten annuleert iedereen (R13), extra plekken laten de wachtlijst doorschuiven (R10).
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

    /** Alle aanmeldingen van een hulpvraag, in elke status; 404 als die niet bestaat. */
    fun getParticipants(
        helpRequestId: Long
    ): List<HelpParticipation> {
        findHelpRequest(helpRequestId)

        return participationRepository.findByHelpRequest(helpRequestId)
    }

    /** De samenvatting voor de app; de fase wordt op dit moment berekend. */
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

    /**
     * De agenda van een vrijwilliger, gesorteerd op starttijd.
     * Met `upcomingOnly` alleen actieve aanmeldingen voor hulpvragen die nog niet voorbij zijn.
     * `mapNotNull` slaat aanmeldingen over waarvan de hulpvraag niet (meer) bestaat.
     */
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

    /** R11: hoeveel no-shows tellen mee voor deze vrijwilliger, en is hij geblokkeerd? */
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

    /**
     * R8: vult vrije plekken aan vanaf de wachtlijst en slaat de doorgeschoven aanmeldingen op.
     */
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

    /**
     * Bouwt de momentopname (VolunteerRecord) die de regels nodig hebben voor één vrijwilliger:
     * de tijdsloten waar hij SIGNED_UP is en de momenten van zijn no-shows.
     * De hulpvraag waar het nu om gaat, telt niet mee als afspraak (`excludeHelpRequestId`); anders
     * zou die altijd met zichzelf overlappen.
     */
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

    /** Laadt de hulpvraag met al zijn aanmeldingen, zonder vergrendeling (alleen lezen). */
    private fun loadBooking(helpRequestId: Long): HelpRequestBooking =
        HelpRequestBooking(
            findHelpRequest(helpRequestId),
            participationRepository.findByHelpRequest(helpRequestId)
        )

    /**
     * Hetzelfde als [loadBooking], maar vergrendelt eerst de rij van de hulpvraag (`FOR UPDATE`).
     * Een tweede verzoek voor dezelfde hulpvraag wacht tot deze transactie klaar is.
     */
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

    /** Zoekt de hulpvraag op; 404 als hij niet bestaat of geen hulpvraag is. */
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

    /** Zet een afwijzing uit het domein om in de fout die de API teruggeeft (403 of 409). */
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

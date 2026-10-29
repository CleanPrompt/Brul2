// src/nl/brul/api/features/help/model/HelpRequestBooking.kt
package nl.brul.api.features.help.model

import java.time.Instant
import nl.brul.api.features.report.model.HelpRequest
import nl.brul.api.features.report.model.ReportStatus

/**
 * Laag: model (het domein).
 *
 * Aggregate: één hulpvraag samen met al zijn aanmeldingen. Alle reserveringsregels over één
 * hulpvraag staan hier bij elkaar, net als de reserveringen van één auto bij "rent my car".
 *
 * De klasse is puur en onveranderlijk: elke functie krijgt `now` mee en geeft een beslissing
 * terug, zonder database en zonder klok. De service laadt de gegevens, vraagt deze klasse wat er
 * moet gebeuren en slaat het resultaat op in één transactie.
 * Daardoor zijn alle regels te testen met gewone objecten (zie HelpRequestBookingTest).
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

    /** R6: wie het eerst komt, het eerst maalt: op `joinedAt`, bij gelijke tijd op id. */
    val waitlist: List<HelpParticipation>
        get() = participations
            .filter { it.status == ParticipationStatus.WAITLISTED }
            .sortedWith(compareBy({ it.joinedAt }, { it.id }))

    val availablePlaces: Int
        get() = (request.volunteersNeeded - signedUp.size).coerceAtLeast(0)

    fun participationOf(userId: Long): HelpParticipation? =
        participations.find { it.userId == userId }

    /** Positie op de wachtlijst (1 = de volgende), of `null` als je er niet op staat. */
    fun waitlistPositionOf(userId: Long): Int? =
        waitlist.indexOfFirst { it.userId == userId }
            .takeIf { it >= 0 }
            ?.plus(1)

    /**
     * De fase volgt uit status, tijd en capaciteit en wordt nooit opgeslagen.
     * De volgorde van de takken in `when` telt: gesloten gaat voor afgelopen, afgelopen voor bezig.
     */
    fun phaseAt(now: Instant): HelpRequestPhase = when {
        request.status == ReportStatus.RESOLVED -> HelpRequestPhase.CLOSED
        request.slot.hasEnded(now) -> HelpRequestPhase.COMPLETED
        request.slot.hasStarted(now) -> HelpRequestPhase.IN_PROGRESS
        signedUp.size >= request.volunteersNeeded -> HelpRequestPhase.FULL
        else -> HelpRequestPhase.OPEN
    }

    /**
     * R2 t/m R6 en R11: mag deze vrijwilliger zich aanmelden, en waar komt hij terecht?
     * De controles staan in een vaste volgorde; de eerste die niet klopt, bepaalt de afwijzing.
     * Is de hulpvraag vol, dan komt hij op de wachtlijst (R6).
     */
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

    /**
     * R7: afmelden is gewoon (CANCELLED) tot twee uur vóór de start, daarna laat (LATE_CANCELLED).
     * Na de start kan het niet meer. Wie van de wachtlijst afgaat, maakt geen plek vrij.
     */
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
     * R8: wie schuift er van de wachtlijst door naar een vrije plek?
     *
     * Kandidaten gaan op volgorde van de wachtlijst. Wie inmiddels ergens anders is ingeschreven
     * (R5) of geblokkeerd is (R11), wordt overgeslagen en houdt zijn plek op de wachtlijst.
     * `volunteerOf` is een functie als parameter: de service geeft mee hoe je een VolunteerRecord
     * ophaalt, zodat deze klasse de database niet hoeft te kennen.
     * Geeft de doorgeschoven aanmeldingen terug, klaar om op te slaan.
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

    /** R9: na de start legt de eigenaar vast wie er kwam (ATTENDED) en wie niet (NO_SHOW). */
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
     * R13: sluit de eigenaar de hulpvraag vóór de start, dan worden alle actieve aanmeldingen
     * geannuleerd. De vrijwilligers kunnen daar niets aan doen, dus het telt als een gewone
     * afmelding en niet als een late.
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

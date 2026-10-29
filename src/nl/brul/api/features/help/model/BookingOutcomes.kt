// src/nl/brul/api/features/help/model/BookingOutcomes.kt
package nl.brul.api.features.help.model

/**
 * Laag: model (het domein).
 *
 * De uitkomsten van de reserveringsregels, als waarden in plaats van exceptions.
 * `sealed interface`: de lijst met mogelijke uitkomsten ligt vast. Een `when` over een uitkomst
 * moet daardoor elke mogelijkheid afhandelen, anders compileert de code niet.
 * Tests kunnen uitkomsten gewoon vergelijken, bijvoorbeeld met
 * `assertEquals(JoinOutcome.SignedUp, ...)`.
 * De service maakt van een afwijzing de juiste HTTP-fout (403 of 409 met een `code`).
 */

/** Uitkomst van aanmelden: ingeschreven, op de wachtlijst (met positie) of afgewezen. */
sealed interface JoinOutcome {
    data object SignedUp : JoinOutcome
    data class Waitlisted(val position: Int) : JoinOutcome
    data class Rejected(val reason: JoinRejection) : JoinOutcome
}

enum class JoinRejection {
    /** R2: je eigen hulpvraag. */
    OWN_REQUEST,

    /** R3: al begonnen, afgelopen of gesloten. */
    NOT_OPEN,

    /** R4: al actief, of een eindstatus zoals LATE_CANCELLED. */
    ALREADY_JOINED,

    /** R11: te veel no-shows. */
    BLOCKED,

    /** R5: in dezelfde tijd al ergens anders ingeschreven. */
    TIME_CONFLICT
}

/** Uitkomst van afmelden: gelukt (met de nieuwe status) of afgewezen. */
sealed interface LeaveOutcome {
    /** [freedPlace] is true als er een SIGNED_UP-plek vrijkwam (R8). */
    data class Left(
        val participation: HelpParticipation,
        val freedPlace: Boolean
    ) : LeaveOutcome

    data class Rejected(val reason: LeaveRejection) : LeaveOutcome
}

enum class LeaveRejection {
    NOT_PARTICIPATING,

    /** R7: na de start kun je niet meer afmelden. */
    ALREADY_STARTED
}

/** Uitkomst van aanwezigheid vastleggen (R9): gemarkeerd of afgewezen. */
sealed interface AttendanceOutcome {
    data class Marked(val participation: HelpParticipation) : AttendanceOutcome
    data class Rejected(val reason: AttendanceRejection) : AttendanceOutcome
}

enum class AttendanceRejection {
    NOT_OWNER,
    NOT_STARTED,
    NOT_SIGNED_UP
}

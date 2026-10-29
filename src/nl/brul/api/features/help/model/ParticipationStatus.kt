// src/nl/brul/api/features/help/model/ParticipationStatus.kt
package nl.brul.api.features.help.model

import kotlinx.serialization.Serializable

/**
 * Laag: model (het domein).
 *
 * De levensloop van één aanmelding van een vrijwilliger voor één hulpvraag.
 * De toegestane overgangen vormen een kleine toestandsmachine. Die staat in de enum zelf, zodat
 * geen enkele code een aanmelding in een onmogelijke toestand kan zetten:
 *
 * - WAITLISTED kan naar SIGNED_UP (doorgeschoven, R8) of CANCELLED
 * - SIGNED_UP kan naar CANCELLED, LATE_CANCELLED (R7), ATTENDED of NO_SHOW (R9)
 * - CANCELLED kan opnieuw naar SIGNED_UP of WAITLISTED (opnieuw aanmelden, R4)
 * - LATE_CANCELLED, ATTENDED en NO_SHOW zijn eindstations
 *
 * `@Serializable`: de status gaat als tekst mee in de JSON, bijvoorbeeld "WAITLISTED".
 */
@Serializable
enum class ParticipationStatus {
    WAITLISTED,
    SIGNED_UP,
    CANCELLED,
    LATE_CANCELLED,
    ATTENDED,
    NO_SHOW;

    /** Actief betekent: telt nog mee, als plek of als plaats op de wachtlijst. */
    val isActive: Boolean
        get() = this == WAITLISTED || this == SIGNED_UP

    /**
     * Mag deze status naar `next`? Er is een tak per status in de `when`: vergeet je er een, dan
     * compileert de code niet.
     */
    fun canMoveTo(next: ParticipationStatus): Boolean = when (this) {
        WAITLISTED -> next == SIGNED_UP || next == CANCELLED
        SIGNED_UP -> next in setOf(CANCELLED, LATE_CANCELLED, ATTENDED, NO_SHOW)
        CANCELLED -> next == SIGNED_UP || next == WAITLISTED
        LATE_CANCELLED, ATTENDED, NO_SHOW -> false
    }
}

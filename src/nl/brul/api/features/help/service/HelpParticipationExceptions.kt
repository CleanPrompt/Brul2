// src/nl/brul/api/features/help/service/HelpParticipationExceptions.kt
package nl.brul.api.features.help.service

import nl.brul.api.common.error.DomainConflictException
import nl.brul.api.common.error.DomainForbiddenException
import nl.brul.api.common.error.DomainNotFoundException

/**
 * Laag: service (de regels).
 *
 * De fouten van de feature help. Ze erven van de algemene domeinfouten in common.error, zodat
 * ErrorHandling ze zonder extra code omzet naar de juiste statuscode, met een `code` in de JSON:
 * 404 niet gevonden, 403 niet toegestaan, 409 een gebroken regel.
 *
 * Deze: 404 als de hulpvraag niet bestaat, of als de melding geen hulpvraag is.
 */
class HelpRequestNotFoundException :
    DomainNotFoundException(
        code = "HELP_REQUEST_NOT_FOUND",
        message = "Help request not found."
    )

/** 404: je bent niet (meer) aangemeld bij deze hulpvraag. */
class HelpParticipationNotFoundException :
    DomainNotFoundException(
        code = "NOT_PARTICIPATING",
        message = "You are not participating in this help request."
    )

/** 403 bij R2 (je eigen hulpvraag) en R9 (alleen de eigenaar legt aanwezigheid vast). */
class HelpParticipationForbiddenException(
    code: String = "OWN_REQUEST",
    message: String = "You cannot join your own help request."
) : DomainForbiddenException(code, message)

/**
 * 409 voor elke andere gebroken reserveringsregel; [code] zegt welke, bijvoorbeeld TIME_CONFLICT
 * of BLOCKED.
 */
class HelpParticipationConflictException(
    code: String,
    message: String
) : DomainConflictException(code, message)

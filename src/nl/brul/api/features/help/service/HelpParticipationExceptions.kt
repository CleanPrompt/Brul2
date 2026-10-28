// src/nl/brul/api/features/help/service/HelpParticipationExceptions.kt
package nl.brul.api.features.help.service

import nl.brul.api.common.error.DomainConflictException
import nl.brul.api.common.error.DomainForbiddenException
import nl.brul.api.common.error.DomainNotFoundException

class HelpRequestNotFoundException :
    DomainNotFoundException(
        code = "HELP_REQUEST_NOT_FOUND",
        message = "Help request not found."
    )

class HelpParticipationNotFoundException :
    DomainNotFoundException(
        code = "NOT_PARTICIPATING",
        message = "You are not participating in this help request."
    )

/** 403 for R2 (own request) and R9 (only the owner records attendance). */
class HelpParticipationForbiddenException(
    code: String = "OWN_REQUEST",
    message: String = "You cannot join your own help request."
) : DomainForbiddenException(code, message)

/** 409 for every other broken booking rule; [code] names the rule. */
class HelpParticipationConflictException(
    code: String,
    message: String
) : DomainConflictException(code, message)

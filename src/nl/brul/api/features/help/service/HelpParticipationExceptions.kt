// src/nl/brul/api/features/help/service/HelpParticipationExceptions.kt
package nl.brul.api.features.help.service

class HelpRequestNotFoundException :
    RuntimeException("Help request not found.")

class HelpParticipationForbiddenException :
    RuntimeException("You cannot join your own help request.")

class HelpParticipationConflictException(
    message: String
) : RuntimeException(message)
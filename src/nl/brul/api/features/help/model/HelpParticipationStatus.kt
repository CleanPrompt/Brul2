// src/nl/brul/api/features/help/model/HelpParticipationStatus.kt
package nl.brul.api.features.help.model

import kotlinx.serialization.Serializable

@Serializable
enum class HelpParticipationStatus {
    OPEN,
    FULL,
    CLOSED
}
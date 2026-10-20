// src/nl/brul/api/features/help/model/HelpParticipation.kt
package nl.brul.api.features.help.model

data class HelpParticipation(
    val id: Long,
    val helpRequestId: Long,
    val userId: Long
) {
    init {
        require(id > 0) {
            "Participation ID must be positive."
        }

        require(helpRequestId > 0) {
            "Help request ID must be positive."
        }

        require(userId > 0) {
            "User ID must be positive."
        }
    }
}
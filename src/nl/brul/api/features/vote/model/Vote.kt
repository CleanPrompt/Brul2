// src/nl/brul/api/features/vote/model/Vote.kt
package nl.brul.api.features.vote.model

data class Vote(
    val id: Long,
    val reportId: Long,
    val userId: Long,
    val type: VoteType
) {
    init {
        require(id > 0) { "Vote ID must be positive" }
        require(reportId > 0) { "Report ID must be positive" }
        require(userId > 0) { "User ID must be positive" }
    }
}
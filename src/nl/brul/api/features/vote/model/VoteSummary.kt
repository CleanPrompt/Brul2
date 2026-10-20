// src/nl/brul/api/features/vote/model/VoteSummary.kt
package nl.brul.api.features.vote.model

data class VoteSummary(
    val reportId: Long,
    val likes: Int,
    val dislikes: Int
) {
    val score: Int
        get() = likes - dislikes

    val totalVotes: Int
        get() = likes + dislikes
}
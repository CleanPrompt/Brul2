// src/nl/brul/api/features/vote/dto/VoteSummaryResponse.kt
package nl.brul.api.features.vote.dto

import kotlinx.serialization.Serializable
import nl.brul.api.features.vote.model.VoteSummary

@Serializable
data class VoteSummaryResponse(
    val reportId: Long,
    val likes: Int,
    val dislikes: Int,
    val score: Int,
    val totalVotes: Int
)

fun VoteSummary.toResponse(): VoteSummaryResponse =
    VoteSummaryResponse(
        reportId = reportId,
        likes = likes,
        dislikes = dislikes,
        score = score,
        totalVotes = totalVotes
    )
// src/nl/brul/api/features/vote/dto/VoteResponse.kt
package nl.brul.api.features.vote.dto

import kotlinx.serialization.Serializable
import nl.brul.api.features.vote.model.Vote
import nl.brul.api.features.vote.model.VoteType

@Serializable
data class VoteResponse(
    val id: Long,
    val reportId: Long,
    val userId: Long,
    val type: VoteType
)

fun Vote.toResponse(): VoteResponse =
    VoteResponse(
        id = id,
        reportId = reportId,
        userId = userId,
        type = type
    )
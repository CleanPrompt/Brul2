// src/nl/brul/api/features/vote/dto/VoteRequest.kt
package nl.brul.api.features.vote.dto

import kotlinx.serialization.Serializable
import nl.brul.api.features.vote.model.VoteType

@Serializable
data class VoteRequest(
    val type: VoteType
)
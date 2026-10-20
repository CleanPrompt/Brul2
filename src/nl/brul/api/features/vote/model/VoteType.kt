// src/nl/brul/api/features/vote/model/VoteType.kt
package nl.brul.api.features.vote.model

import kotlinx.serialization.Serializable

@Serializable
enum class VoteType {
    LIKE,
    DISLIKE
}
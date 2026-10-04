package nl.brul.domain

import kotlinx.serialization.Serializable

@Serializable
enum class Urgency {
    LOW,
    MEDIUM,
    HIGH
}
package nl.brul.domain

import kotlinx.serialization.Serializable

@Serializable
enum class NuisanceType {
    NOISE,
    WASTE,
    VANDALISM,
    OTHER
}
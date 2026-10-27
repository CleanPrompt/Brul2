// src/nl/brul/api/features/report/model/Location.kt
package nl.brul.api.features.report.model

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Laag: model (het domein).
 *
 * Een plek op de kaart. `init` controleert: breedtegraad tussen -90 en 90, lengtegraad tussen -180
 * en 180.
 * `distanceTo()` berekent de afstand in km met de haversine-formule, die rekening houdt met dat de
 * aarde rond is.
 * De locatie weet dus zelf hoe ver hij van een andere locatie is.
 * `data class`: Kotlin maakt automatisch equals, toString en copy.
 */
data class Location(
    val latitude: Double,
    val longitude: Double
) {
    init {
        require(latitude in -90.0..90.0) {
            "Latitude must be between -90 and 90."
        }

        require(longitude in -180.0..180.0) {
            "Longitude must be between -180 and 180."
        }
    }

    fun distanceTo(other: Location): Double {
        val earthRadiusKm = 6371.0

        val latitudeDifference = Math.toRadians(other.latitude - latitude)
        val longitudeDifference = Math.toRadians(other.longitude - longitude)

        val firstLatitude = Math.toRadians(latitude)
        val secondLatitude = Math.toRadians(other.latitude)

        val a = sin(latitudeDifference / 2).pow(2) +
                cos(firstLatitude) *
                cos(secondLatitude) *
                sin(longitudeDifference / 2).pow(2)

        val c = 2 * asin(sqrt(a.coerceIn(0.0, 1.0)))

        return earthRadiusKm * c
    }
}
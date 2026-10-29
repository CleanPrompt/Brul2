// src/nl/brul/api/common/error/ApiError.kt
package nl.brul.api.common.error

import kotlinx.serialization.Serializable

/**
 * Laag: common (gedeeld door alle features).
 *
 * De JSON die de API terugstuurt bij een fout, bijvoorbeeld
 * `{"message": "...", "code": "TIME_CONFLICT"}`.
 * `code` is een vaste waarde voor programma's: de Android-app kan daarop een eigen melding tonen,
 * in plaats van de tekst in `message` uit te pluizen.
 * Is er geen specifieke code, dan blijft `code` weg uit de JSON.
 */
@Serializable
data class ApiError(
    val message: String,
    val code: String? = null
)

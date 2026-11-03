// src/nl/brul/api/features/report/model/ReportPhoto.kt
package nl.brul.api.features.report.model

/**
 * Laag: model (het domein).
 *
 * De gegevens van één foto bij een melding. De afbeelding zelf staat niet in de database maar
 * in PhotoStorage, onder de sleutel `storageKey`.
 *
 * - `location`: waar de foto genomen is. Komt uit de GPS-gegevens in de foto zelf, of, als die
 *   ontbreken, uit de coördinaten die de app meestuurt. Kan ook leeg zijn.
 * - `locationSource`: waar de locatie vandaan komt (PHOTO of APP); leeg als er geen locatie is.
 * - `uploaderId` mag leeg zijn: wordt de gebruiker verwijderd, dan blijft de foto bestaan.
 *
 * `init` controleert de id's, de grootte en dat een locatie altijd een bron heeft (en andersom).
 */
data class ReportPhoto(
    val id: Long,
    val reportId: Long,
    val uploaderId: Long?,
    val contentType: String,
    val sizeBytes: Long,
    val storageKey: String,
    val createdAt: Long,
    val location: Location? = null,
    val locationSource: LocationSource? = null
) {
    /** Waar de GPS-coördinaten van een foto vandaan komen. */
    enum class LocationSource {
        /** Uit de EXIF-gegevens die de camera van de telefoon in de foto zet. */
        PHOTO,

        /** Meegestuurd door de app, omdat de foto zelf geen GPS bevat. */
        APP
    }

    init {
        require(id > 0) {
            "Photo ID must be positive."
        }

        require(reportId > 0) {
            "Report ID must be positive."
        }

        require(uploaderId == null || uploaderId > 0) {
            "Uploader ID must be positive."
        }

        require(sizeBytes > 0) {
            "A photo cannot be empty."
        }

        require(storageKey.isNotBlank()) {
            "Storage key cannot be blank."
        }

        require(createdAt > 0) {
            "Creation timestamp must be positive."
        }

        require((location == null) == (locationSource == null)) {
            "A photo location always needs a source."
        }
    }
}

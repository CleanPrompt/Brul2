// src/nl/brul/api/config/AppConfig.kt
package nl.brul.api.config

import io.ktor.server.application.Application

/**
 * Laag: config (de instellingen).
 *
 * De instellingen uit application.yaml die de app nodig heeft, ingelezen met
 * `AppConfig.from(application)`. Ontbreekt `brul.uploadsDir`, dan wordt het "uploads".
 */
data class AppConfig(
    val name: String,
    /** Map waarin geüploade foto's worden bewaard (brul.uploadsDir, standaard "uploads"). */
    val uploadsDir: String = DEFAULT_UPLOADS_DIR
) {
    companion object {
        const val DEFAULT_UPLOADS_DIR = "uploads"

        fun from(application: Application): AppConfig {
            val config = application.environment.config

            val name = config
                .property("brul.name")
                .getString()

            require(name.isNotBlank()) {
                "Application name cannot be blank."
            }

            val uploadsDir = config
                .propertyOrNull("brul.uploadsDir")
                ?.getString()
                ?.takeIf { it.isNotBlank() }
                ?: DEFAULT_UPLOADS_DIR

            return AppConfig(name, uploadsDir)
        }
    }
}

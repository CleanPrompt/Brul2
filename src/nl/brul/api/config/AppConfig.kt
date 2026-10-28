// src/nl/brul/api/config/AppConfig.kt
package nl.brul.api.config

import io.ktor.server.application.Application

data class AppConfig(
    val name: String,
    /** Folder where uploaded photos are stored (brul.uploadsDir). */
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

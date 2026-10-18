// src/nl/brul/api/config/AppConfig.kt
package nl.brul.api.config

import io.ktor.server.application.Application

data class AppConfig(
    val name: String
) {
    companion object {
        fun from(application: Application): AppConfig {
            val name = application.environment.config
                .property("brul.name")
                .getString()

            require(name.isNotBlank()) {
                "Application name cannot be blank."
            }

            return AppConfig(name)
        }
    }
}

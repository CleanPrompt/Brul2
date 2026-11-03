// src/nl/brul/api/features/report/ai/AiTextClient.kt
package nl.brul.api.features.report.ai

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 * Laag: ai (de koppeling met een AI-dienst).
 *
 * Het kleinste stukje dat echt met internet praat: stuur een vraag (prompt), krijg tekst terug.
 * `fun interface`: een interface met precies één functie. In tests schrijf je een nep-versie als
 * lambda, bijvoorbeeld `AiTextClient { """{"type":"NUISANCE"}""" }`.
 * Geeft `null` als er geen antwoord is.
 */
fun interface AiTextClient {
    fun complete(prompt: String): String?
}

/**
 * Praat met de Claude API van Anthropic (POST /v1/messages).
 *
 * - De API-sleutel komt uit .env (ANTHROPIC_API_KEY) en staat dus niet in Git.
 * - Korte timeouts: de app wacht nooit lang op de AI. Lukt het niet op tijd, dan volgt een
 *   exception en valt de service terug op de vaste regels.
 * - Gebruikt alleen de HttpClient van Java zelf en de JSON-bibliotheek die het project al heeft,
 *   dus er is geen nieuwe dependency nodig.
 *
 * Privacy: alleen de titel en beschrijving van een melding gaan naar de AI, geen namen,
 * e-mailadressen, locaties of foto's.
 */
class ClaudeTextClient(
    private val apiKey: String,
    private val model: String = DEFAULT_MODEL,
    private val http: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(3))
        .build()
) : AiTextClient {

    init {
        require(apiKey.isNotBlank()) {
            "An API key is required."
        }
    }

    override fun complete(prompt: String): String? {
        val body = buildJsonObject {
            put("model", model)
            put("max_tokens", MAX_TOKENS)
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "user")
                    put("content", prompt)
                }
            }
        }

        val request = HttpRequest.newBuilder(URI.create(API_URL))
            .timeout(Duration.ofSeconds(8))
            .header("x-api-key", apiKey)
            .header("anthropic-version", API_VERSION)
            .header("content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
            .build()

        val response = http.send(request, HttpResponse.BodyHandlers.ofString())

        check(response.statusCode() == 200) {
            "AI request failed with status ${response.statusCode()}."
        }

        return firstText(response.body())
    }

    companion object {
        const val DEFAULT_MODEL = "claude-haiku-5-5"

        private const val API_URL = "https://api.anthropic.com/v1/messages"
        private const val API_VERSION = "2023-06-01"
        private const val MAX_TOKENS = 200

        /** Haalt de tekst uit het antwoord: `{"content": [{"type": "text", "text": "..."}]}`. */
        fun firstText(responseBody: String): String? =
            Json.parseToJsonElement(responseBody)
                .jsonObject["content"]
                ?.jsonArray
                ?.firstOrNull { it.jsonObject["type"]?.jsonPrimitive?.contentOrNull == "text" }
                ?.jsonObject
                ?.get("text")
                ?.jsonPrimitive
                ?.contentOrNull
    }
}

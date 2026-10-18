// test/nl/brul/api/HealthRoutesTest.kt
package nl.brul.api

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class HealthRoutesTest {

    @Test
    fun `health endpoint returns JSON`() = testApplication {
        environment {
            config = io.ktor.server.config.MapApplicationConfig(
                "brul.name" to "brul-api"
            )
        }

        application {
            module()
        }

        val response = client.get("/api/v1/health")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ContentType.Application.Json, response.contentType()?.withoutParameters())

        val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject

        assertEquals("ok", json["status"]?.jsonPrimitive?.content)
        assertEquals("brul-api", json["service"]?.jsonPrimitive?.content)
    }
}

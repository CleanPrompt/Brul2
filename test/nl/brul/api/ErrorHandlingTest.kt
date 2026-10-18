// test/nl/brul/api/ErrorHandlingTest.kt
package nl.brul.api

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import nl.brul.api.common.error.configureErrorHandling
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ErrorHandlingTest {

    @Test
    fun `illegal argument returns bad request`() = testApplication {
        application {
            install(ContentNegotiation) {
                json()
            }

            configureErrorHandling()

            routing {
                get("/test-error") {
                    throw IllegalArgumentException("Invalid value")
                }
            }
        }

        val response = client.get("/test-error")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Invalid request."))
    }

    @Test
    fun `unexpected exception returns internal server error`() = testApplication {
        application {
            install(ContentNegotiation) {
                json()
            }

            configureErrorHandling()

            routing {
                get("/test-error") {
                    throw IllegalStateException("Sensitive details")
                }
            }
        }

        val response = client.get("/test-error")

        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertTrue(response.bodyAsText().contains("Internal server error."))
        assertTrue(!response.bodyAsText().contains("Sensitive details"))
    }
}

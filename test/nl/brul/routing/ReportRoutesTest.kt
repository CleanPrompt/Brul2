package nl.brul.routing

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import org.jetbrains.amper.ktor.module

class ReportRoutesTest {

    @Test
    fun `GET reports returns OK`() = testApplication {
        application {
            module()
        }

        val response = client.get("/reports")

        assertEquals(
            HttpStatusCode.OK,
            response.status
        )
    }
}
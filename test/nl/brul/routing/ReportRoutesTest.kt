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

    @Test
    fun `GET report with unknown id returns Not Found`() = testApplication {
        application {
            module()
        }

        val response = client.get("/reports/does-not-exist")

        assertEquals(
            HttpStatusCode.NotFound,
            response.status
        )
    }
}
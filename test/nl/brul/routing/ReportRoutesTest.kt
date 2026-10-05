package nl.brul.routing

import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json
import nl.brul.domain.ReportStatus
import nl.brul.dto.request.CreateHelpRequestRequest
import nl.brul.dto.request.CreateSafetyReportRequest
import nl.brul.dto.request.UpdateReportRequest
import nl.brul.dto.response.ReportResponse
import org.jetbrains.amper.ktor.module

class ReportRoutesTest {

    private fun ApplicationTestBuilder.createJsonClient() =
        createClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }

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

    @Test
    fun `POST safety report returns Created and correct report`() = testApplication {
        application {
            module()
        }

        val jsonClient = createJsonClient()

        val request = CreateSafetyReportRequest(
            title = "Broken streetlight",
            description = "Streetlight is not working.",
            photo = null,
            latitude = 52.0705,
            longitude = 4.3007,
            urgency = nl.brul.domain.Urgency.MEDIUM
        )

        val response = jsonClient.post("/reports/safety") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(
            HttpStatusCode.Created,
            response.status
        )

        val report = response.body<ReportResponse>()

        assertEquals(
            "SAFETY",
            report.type
        )

        assertEquals(
            "Broken streetlight",
            report.title
        )

        assertEquals(
            "Streetlight is not working.",
            report.description
        )

        assertEquals(
            52.0705,
            report.latitude
        )

        assertEquals(
            4.3007,
            report.longitude
        )

        assertEquals(
            nl.brul.domain.Urgency.MEDIUM,
            report.urgency
        )

        assertEquals(
            ReportStatus.OPEN,
            report.status
        )

        assertEquals(
            "user-1",
            report.createdById
        )

        assertNotNull(report.id)
    }

    @Test
    fun `created safety report can be retrieved by id`() = testApplication {
        application {
            module()
        }

        val jsonClient = createJsonClient()

        val createRequest = CreateSafetyReportRequest(
            title = "Unsafe crossing",
            description = "Cars are driving too fast.",
            photo = null,
            latitude = 52.0705,
            longitude = 4.3007,
            urgency = nl.brul.domain.Urgency.HIGH
        )

        val createResponse = jsonClient.post("/reports/safety") {
            contentType(ContentType.Application.Json)
            setBody(createRequest)
        }

        assertEquals(
            HttpStatusCode.Created,
            createResponse.status
        )

        val createdReport =
            createResponse.body<ReportResponse>()

        val getResponse =
            jsonClient.get("/reports/${createdReport.id}")

        assertEquals(
            HttpStatusCode.OK,
            getResponse.status
        )

        val retrievedReport =
            getResponse.body<ReportResponse>()

        assertEquals(
            createdReport.id,
            retrievedReport.id
        )

        assertEquals(
            "Unsafe crossing",
            retrievedReport.title
        )

        assertEquals(
            "SAFETY",
            retrievedReport.type
        )

        assertEquals(
            nl.brul.domain.Urgency.HIGH,
            retrievedReport.urgency
        )

        assertEquals(
            ReportStatus.OPEN,
            retrievedReport.status
        )
    }

    @Test
    fun `PUT report updates report`() = testApplication {
        application {
            module()
        }

        val jsonClient = createJsonClient()

        val createRequest = CreateSafetyReportRequest(
            title = "Old title",
            description = "Old description",
            photo = null,
            latitude = 52.0705,
            longitude = 4.3007,
            urgency = nl.brul.domain.Urgency.LOW
        )

        val createResponse = jsonClient.post("/reports/safety") {
            contentType(ContentType.Application.Json)
            setBody(createRequest)
        }

        assertEquals(
            HttpStatusCode.Created,
            createResponse.status
        )

        val createdReport =
            createResponse.body<ReportResponse>()

        val updateRequest = UpdateReportRequest(
            title = "Updated title",
            description = "Updated description",
            photo = "updated-photo.jpg",
            latitude = 52.0800,
            longitude = 4.3100
        )

        val updateResponse =
            jsonClient.put("/reports/${createdReport.id}") {
                contentType(ContentType.Application.Json)
                setBody(updateRequest)
            }

        assertEquals(
            HttpStatusCode.OK,
            updateResponse.status
        )

        val updatedReport =
            updateResponse.body<ReportResponse>()

        assertEquals(
            createdReport.id,
            updatedReport.id
        )

        assertEquals(
            "Updated title",
            updatedReport.title
        )

        assertEquals(
            "Updated description",
            updatedReport.description
        )

        assertEquals(
            "updated-photo.jpg",
            updatedReport.photo
        )

        assertEquals(
            52.0800,
            updatedReport.latitude
        )

        assertEquals(
            4.3100,
            updatedReport.longitude
        )

        assertEquals(
            nl.brul.domain.Urgency.LOW,
            updatedReport.urgency
        )
    }

    @Test
    fun `DELETE report removes report`() = testApplication {
        application {
            module()
        }

        val jsonClient = createJsonClient()

        val createRequest = CreateSafetyReportRequest(
            title = "Temporary report",
            description = null,
            photo = null,
            latitude = 52.0705,
            longitude = 4.3007,
            urgency = nl.brul.domain.Urgency.LOW
        )

        val createResponse = jsonClient.post("/reports/safety") {
            contentType(ContentType.Application.Json)
            setBody(createRequest)
        }

        assertEquals(
            HttpStatusCode.Created,
            createResponse.status
        )

        val createdReport =
            createResponse.body<ReportResponse>()

        val deleteResponse =
            jsonClient.delete("/reports/${createdReport.id}")

        assertEquals(
            HttpStatusCode.NoContent,
            deleteResponse.status
        )

        val getResponse =
            jsonClient.get("/reports/${createdReport.id}")

        assertEquals(
            HttpStatusCode.NotFound,
            getResponse.status
        )
    }

    @Test
    fun `resolve report changes status to Resolved`() = testApplication {
        application {
            module()
        }

        val jsonClient = createJsonClient()

        val createRequest = CreateSafetyReportRequest(
            title = "Situation to resolve",
            description = null,
            photo = null,
            latitude = 52.0705,
            longitude = 4.3007,
            urgency = nl.brul.domain.Urgency.MEDIUM
        )

        val createResponse = jsonClient.post("/reports/safety") {
            contentType(ContentType.Application.Json)
            setBody(createRequest)
        }

        assertEquals(
            HttpStatusCode.Created,
            createResponse.status
        )

        val createdReport =
            createResponse.body<ReportResponse>()

        assertEquals(
            ReportStatus.OPEN,
            createdReport.status
        )

        val resolveResponse =
            jsonClient.post(
                "/reports/${createdReport.id}/resolve"
            )

        assertEquals(
            HttpStatusCode.OK,
            resolveResponse.status
        )

        val resolvedReport =
            resolveResponse.body<ReportResponse>()

        assertEquals(
            createdReport.id,
            resolvedReport.id
        )

        assertEquals(
            ReportStatus.RESOLVED,
            resolvedReport.status
        )
    }

    @Test
    fun `POST safety report with blank title fails`() = testApplication {
        application {
            module()
        }

        val jsonClient = createJsonClient()

        val request = CreateSafetyReportRequest(
            title = "",
            description = null,
            photo = null,
            latitude = 52.0705,
            longitude = 4.3007,
            urgency = nl.brul.domain.Urgency.LOW
        )

        val response = jsonClient.post("/reports/safety") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(
            HttpStatusCode.InternalServerError,
            response.status
        )
    }

    @Test
    fun `POST safety report with invalid latitude fails`() = testApplication {
        application {
            module()
        }

        val jsonClient = createJsonClient()

        val request = CreateSafetyReportRequest(
            title = "Invalid location",
            description = null,
            photo = null,
            latitude = 100.0,
            longitude = 4.3007,
            urgency = nl.brul.domain.Urgency.LOW
        )

        val response = jsonClient.post("/reports/safety") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(
            HttpStatusCode.InternalServerError,
            response.status
        )
    }

    @Test
    fun `PUT unknown report returns Not Found`() = testApplication {
        application {
            module()
        }

        val jsonClient = createJsonClient()

        val request = UpdateReportRequest(
            title = "Updated",
            description = null,
            photo = null,
            latitude = 52.0705,
            longitude = 4.3007
        )

        val response =
            jsonClient.put("/reports/does-not-exist") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

        assertEquals(
            HttpStatusCode.NotFound,
            response.status
        )
    }

    @Test
    fun `DELETE unknown report returns Not Found`() = testApplication {
        application {
            module()
        }

        val response =
            client.delete("/reports/does-not-exist")

        assertEquals(
            HttpStatusCode.NotFound,
            response.status
        )
    }

    @Test
    fun `claiming own help request returns Conflict`() = testApplication {
        application {
            module()
        }

        val jsonClient = createJsonClient()

        val createRequest = CreateHelpRequestRequest(
            title = "Need help",
            description = "Can someone help me?",
            photo = null,
            latitude = 52.0705,
            longitude = 4.3007
        )

        val createResponse =
            jsonClient.post("/reports/help") {
                contentType(ContentType.Application.Json)
                setBody(createRequest)
            }

        assertEquals(
            HttpStatusCode.Created,
            createResponse.status
        )

        val helpRequest =
            createResponse.body<ReportResponse>()

        val claimResponse =
            jsonClient.post(
                "/reports/${helpRequest.id}/claim"
            )

        assertEquals(
            HttpStatusCode.Conflict,
            claimResponse.status
        )
    }
}
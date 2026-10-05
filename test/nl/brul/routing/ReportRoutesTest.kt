package nl.brul.routing

import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import nl.brul.domain.NuisanceType
import nl.brul.domain.ReportStatus
import nl.brul.domain.ReportType
import nl.brul.domain.Urgency
import nl.brul.dto.request.CreateHelpRequestRequest
import nl.brul.dto.request.CreateNuisanceReportRequest
import nl.brul.dto.request.CreateSafetyReportRequest
import nl.brul.dto.request.UpdateReportRequest
import nl.brul.dto.response.ErrorResponse
import nl.brul.dto.response.ReportResponse
import org.jetbrains.amper.ktor.module

class ReportRoutesTest {

    @Test
    fun `GET reports returns OK`() = testApplication {
        application {
            module()
        }

        val response =
            client.get("/reports")

        assertEquals(
            HttpStatusCode.OK,
            response.status
        )
    }

    @Test
    fun `GET reports returns empty list when no reports exist`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        val response =
            client.get("/reports")

        assertEquals(
            HttpStatusCode.OK,
            response.status
        )

        val reports =
            response.body<List<ReportResponse>>()

        assertTrue(
            reports.isEmpty()
        )
    }

    @Test
    fun `GET unknown report returns NotFound`() = testApplication {
        application {
            module()
        }

        val response =
            client.get("/reports/does-not-exist")

        assertEquals(
            HttpStatusCode.NotFound,
            response.status
        )
    }

    @Test
    fun `POST safety creates safety report`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        val response =
            client.post("/reports/safety") {
                contentType(ContentType.Application.Json)

                setBody(
                    CreateSafetyReportRequest(
                        title = "Broken streetlight",
                        description = "The streetlight is broken.",
                        photo = null,
                        latitude = 52.08,
                        longitude = 4.31,
                        urgency = Urgency.HIGH
                    )
                )
            }

        assertEquals(
            HttpStatusCode.Created,
            response.status
        )

        val report =
            response.body<ReportResponse>()

        assertEquals(
            ReportType.SAFETY,
            report.type
        )

        assertEquals(
            "Broken streetlight",
            report.title
        )

        assertEquals(
            Urgency.HIGH,
            report.urgency
        )

        assertEquals(
            ReportStatus.OPEN,
            report.status
        )
    }

    @Test
    fun `POST safety then GET by id returns created report`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        val createResponse =
            client.post("/reports/safety") {
                contentType(ContentType.Application.Json)

                setBody(
                    CreateSafetyReportRequest(
                        title = "Unsafe crossing",
                        description = "Cars are driving too fast.",
                        photo = null,
                        latitude = 52.08,
                        longitude = 4.31,
                        urgency = Urgency.HIGH
                    )
                )
            }

        assertEquals(
            HttpStatusCode.Created,
            createResponse.status
        )

        val createdReport =
            createResponse.body<ReportResponse>()

        val getResponse =
            client.get(
                "/reports/${createdReport.id}"
            )

        assertEquals(
            HttpStatusCode.OK,
            getResponse.status
        )

        val foundReport =
            getResponse.body<ReportResponse>()

        assertEquals(
            createdReport.id,
            foundReport.id
        )

        assertEquals(
            "Unsafe crossing",
            foundReport.title
        )

        assertEquals(
            ReportType.SAFETY,
            foundReport.type
        )
    }

    @Test
    fun `POST nuisance creates nuisance report`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        val response =
            client.post("/reports/nuisance") {
                contentType(ContentType.Application.Json)

                setBody(
                    CreateNuisanceReportRequest(
                        title = "Loud music",
                        description = "Loud music during the night.",
                        photo = null,
                        latitude = 52.08,
                        longitude = 4.31,
                        nuisanceType = NuisanceType.NOISE
                    )
                )
            }

        assertEquals(
            HttpStatusCode.Created,
            response.status
        )

        val report =
            response.body<ReportResponse>()

        assertEquals(
            ReportType.NUISANCE,
            report.type
        )

        assertEquals(
            NuisanceType.NOISE,
            report.nuisanceType
        )
    }

    @Test
    fun `POST help creates help request`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        val response =
            client.post("/reports/help") {
                contentType(ContentType.Application.Json)

                setBody(
                    CreateHelpRequestRequest(
                        title = "Help moving table",
                        description = "I need help moving a table.",
                        photo = null,
                        latitude = 52.08,
                        longitude = 4.31
                    )
                )
            }

        assertEquals(
            HttpStatusCode.Created,
            response.status
        )

        val report =
            response.body<ReportResponse>()

        assertEquals(
            ReportType.HELP,
            report.type
        )

        assertEquals(
            ReportStatus.OPEN,
            report.status
        )
    }

    @Test
    fun `PUT report updates report`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        val createResponse =
            client.post("/reports/safety") {
                contentType(ContentType.Application.Json)

                setBody(
                    CreateSafetyReportRequest(
                        title = "Old title",
                        description = "Old description",
                        photo = null,
                        latitude = 52.08,
                        longitude = 4.31,
                        urgency = Urgency.MEDIUM
                    )
                )
            }

        val createdReport =
            createResponse.body<ReportResponse>()

        val updateResponse =
            client.put(
                "/reports/${createdReport.id}"
            ) {
                contentType(ContentType.Application.Json)

                setBody(
                    UpdateReportRequest(
                        title = "New title",
                        description = "New description",
                        photo = null,
                        latitude = 52.09,
                        longitude = 4.32
                    )
                )
            }

        assertEquals(
            HttpStatusCode.OK,
            updateResponse.status
        )

        val updatedReport =
            updateResponse.body<ReportResponse>()

        assertEquals(
            "New title",
            updatedReport.title
        )

        assertEquals(
            "New description",
            updatedReport.description
        )

        assertEquals(
            52.09,
            updatedReport.latitude
        )

        assertEquals(
            4.32,
            updatedReport.longitude
        )
    }

    @Test
    fun `PUT unknown report returns NotFound`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        val response =
            client.put("/reports/does-not-exist") {
                contentType(ContentType.Application.Json)

                setBody(
                    UpdateReportRequest(
                        title = "New title",
                        description = null,
                        photo = null,
                        latitude = 52.08,
                        longitude = 4.31
                    )
                )
            }

        assertEquals(
            HttpStatusCode.NotFound,
            response.status
        )
    }

    @Test
    fun `DELETE report removes report`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        val createResponse =
            client.post("/reports/help") {
                contentType(ContentType.Application.Json)

                setBody(
                    CreateHelpRequestRequest(
                        title = "Temporary help request",
                        description = null,
                        photo = null,
                        latitude = 52.08,
                        longitude = 4.31
                    )
                )
            }

        val createdReport =
            createResponse.body<ReportResponse>()

        val deleteResponse =
            client.delete(
                "/reports/${createdReport.id}"
            )

        assertEquals(
            HttpStatusCode.NoContent,
            deleteResponse.status
        )

        val getResponse =
            client.get(
                "/reports/${createdReport.id}"
            )

        assertEquals(
            HttpStatusCode.NotFound,
            getResponse.status
        )
    }

    @Test
    fun `DELETE unknown report returns NotFound`() = testApplication {
        application {
            module()
        }

        val response =
            client.delete(
                "/reports/does-not-exist"
            )

        assertEquals(
            HttpStatusCode.NotFound,
            response.status
        )
    }

    @Test
    fun `POST resolve changes report status to RESOLVED`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        val createResponse =
            client.post("/reports/safety") {
                contentType(ContentType.Application.Json)

                setBody(
                    CreateSafetyReportRequest(
                        title = "Broken light",
                        description = null,
                        photo = null,
                        latitude = 52.08,
                        longitude = 4.31,
                        urgency = Urgency.LOW
                    )
                )
            }

        val createdReport =
            createResponse.body<ReportResponse>()

        assertEquals(
            ReportStatus.OPEN,
            createdReport.status
        )

        val resolveResponse =
            client.post(
                "/reports/${createdReport.id}/resolve"
            )

        assertEquals(
            HttpStatusCode.OK,
            resolveResponse.status
        )

        val resolvedReport =
            resolveResponse.body<ReportResponse>()

        assertEquals(
            ReportStatus.RESOLVED,
            resolvedReport.status
        )
    }

    @Test
    fun `claiming own help request returns Conflict`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        val createResponse =
            client.post("/reports/help") {
                contentType(ContentType.Application.Json)

                setBody(
                    CreateHelpRequestRequest(
                        title = "Need help",
                        description = "Please help me.",
                        photo = null,
                        latitude = 52.08,
                        longitude = 4.31
                    )
                )
            }

        val createdReport =
            createResponse.body<ReportResponse>()

        val claimResponse =
            client.post(
                "/reports/${createdReport.id}/claim"
            )

        assertEquals(
            HttpStatusCode.Conflict,
            claimResponse.status
        )
    }

    @Test
    fun `POST safety with blank title returns BadRequest`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        val response =
            client.post("/reports/safety") {
                contentType(ContentType.Application.Json)

                setBody(
                    CreateSafetyReportRequest(
                        title = "",
                        description = null,
                        photo = null,
                        latitude = 52.08,
                        longitude = 4.31,
                        urgency = Urgency.HIGH
                    )
                )
            }

        assertEquals(
            HttpStatusCode.BadRequest,
            response.status
        )
    }

    @Test
    fun `POST safety with invalid latitude returns BadRequest`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        val response =
            client.post("/reports/safety") {
                contentType(ContentType.Application.Json)

                setBody(
                    CreateSafetyReportRequest(
                        title = "Invalid location",
                        description = null,
                        photo = null,
                        latitude = 100.0,
                        longitude = 4.31,
                        urgency = Urgency.HIGH
                    )
                )
            }

        assertEquals(
            HttpStatusCode.BadRequest,
            response.status
        )
    }

    @Test
    fun `GET reports searches title case insensitive`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        createSafetyReport(
            client = client,
            title = "Broken Streetlight",
            urgency = Urgency.HIGH
        )

        createSafetyReport(
            client = client,
            title = "Unsafe crossing",
            urgency = Urgency.MEDIUM
        )

        val response =
            client.get(
                "/reports?search=streetlight"
            )

        assertEquals(
            HttpStatusCode.OK,
            response.status
        )

        val reports =
            response.body<List<ReportResponse>>()

        assertEquals(
            1,
            reports.size
        )

        assertEquals(
            "Broken Streetlight",
            reports.first().title
        )
    }

    @Test
    fun `GET reports searches description case insensitive`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        client.post("/reports/help") {
            contentType(ContentType.Application.Json)

            setBody(
                CreateHelpRequestRequest(
                    title = "Need assistance",
                    description = "Please help with my GARDEN",
                    photo = null,
                    latitude = 52.08,
                    longitude = 4.31
                )
            )
        }

        val response =
            client.get(
                "/reports?search=garden"
            )

        assertEquals(
            HttpStatusCode.OK,
            response.status
        )

        val reports =
            response.body<List<ReportResponse>>()

        assertEquals(
            1,
            reports.size
        )

        assertEquals(
            ReportType.HELP,
            reports.first().type
        )
    }

    @Test
    fun `GET reports filters by type`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        createSafetyReport(
            client = client,
            title = "Safety report",
            urgency = Urgency.HIGH
        )

        client.post("/reports/help") {
            contentType(ContentType.Application.Json)

            setBody(
                CreateHelpRequestRequest(
                    title = "Help request",
                    description = null,
                    photo = null,
                    latitude = 52.08,
                    longitude = 4.31
                )
            )
        }

        val response =
            client.get(
                "/reports?type=SAFETY"
            )

        assertEquals(
            HttpStatusCode.OK,
            response.status
        )

        val reports =
            response.body<List<ReportResponse>>()

        assertEquals(
            1,
            reports.size
        )

        assertEquals(
            ReportType.SAFETY,
            reports.first().type
        )
    }

    @Test
    fun `GET reports filters type case insensitive`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        createSafetyReport(
            client = client,
            title = "Safety report",
            urgency = Urgency.LOW
        )

        val response =
            client.get(
                "/reports?type=safety"
            )

        assertEquals(
            HttpStatusCode.OK,
            response.status
        )

        val reports =
            response.body<List<ReportResponse>>()

        assertEquals(
            1,
            reports.size
        )

        assertEquals(
            ReportType.SAFETY,
            reports.first().type
        )
    }

    @Test
    fun `GET reports filters by status`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        val openReport =
            createSafetyReport(
                client = client,
                title = "Open report",
                urgency = Urgency.LOW
            )

        val resolvedReport =
            createSafetyReport(
                client = client,
                title = "Resolved report",
                urgency = Urgency.HIGH
            )

        client.post(
            "/reports/${resolvedReport.id}/resolve"
        )

        val response =
            client.get(
                "/reports?status=OPEN"
            )

        assertEquals(
            HttpStatusCode.OK,
            response.status
        )

        val reports =
            response.body<List<ReportResponse>>()

        assertEquals(
            1,
            reports.size
        )

        assertEquals(
            openReport.id,
            reports.first().id
        )

        assertEquals(
            ReportStatus.OPEN,
            reports.first().status
        )
    }

    @Test
    fun `GET reports filters by urgency`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        createSafetyReport(
            client = client,
            title = "High urgency",
            urgency = Urgency.HIGH
        )

        createSafetyReport(
            client = client,
            title = "Low urgency",
            urgency = Urgency.LOW
        )

        val response =
            client.get(
                "/reports?urgency=HIGH"
            )

        assertEquals(
            HttpStatusCode.OK,
            response.status
        )

        val reports =
            response.body<List<ReportResponse>>()

        assertEquals(
            1,
            reports.size
        )

        assertEquals(
            "High urgency",
            reports.first().title
        )

        assertEquals(
            Urgency.HIGH,
            reports.first().urgency
        )
    }

    @Test
    fun `GET reports filters by nuisance type`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        client.post("/reports/nuisance") {
            contentType(ContentType.Application.Json)

            setBody(
                CreateNuisanceReportRequest(
                    title = "Noise complaint",
                    description = null,
                    photo = null,
                    latitude = 52.08,
                    longitude = 4.31,
                    nuisanceType = NuisanceType.NOISE
                )
            )
        }

        client.post("/reports/nuisance") {
            contentType(ContentType.Application.Json)

            setBody(
                CreateNuisanceReportRequest(
                    title = "Waste complaint",
                    description = null,
                    photo = null,
                    latitude = 52.08,
                    longitude = 4.31,
                    nuisanceType = NuisanceType.WASTE
                )
            )
        }

        val response =
            client.get(
                "/reports?nuisanceType=NOISE"
            )

        assertEquals(
            HttpStatusCode.OK,
            response.status
        )

        val reports =
            response.body<List<ReportResponse>>()

        assertEquals(
            1,
            reports.size
        )

        assertEquals(
            "Noise complaint",
            reports.first().title
        )

        assertEquals(
            NuisanceType.NOISE,
            reports.first().nuisanceType
        )
    }

    @Test
    fun `GET reports combines filters`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        createSafetyReport(
            client = client,
            title = "Broken streetlight",
            urgency = Urgency.HIGH
        )

        createSafetyReport(
            client = client,
            title = "Broken fence",
            urgency = Urgency.LOW
        )

        createSafetyReport(
            client = client,
            title = "Unsafe street",
            urgency = Urgency.HIGH
        )

        val response =
            client.get(
                "/reports" +
                        "?type=SAFETY" +
                        "&urgency=HIGH" +
                        "&search=street"
            )

        assertEquals(
            HttpStatusCode.OK,
            response.status
        )

        val reports =
            response.body<List<ReportResponse>>()

        assertEquals(
            2,
            reports.size
        )

        assertTrue(
            reports.all { report ->
                report.type == ReportType.SAFETY
            }
        )

        assertTrue(
            reports.all { report ->
                report.urgency == Urgency.HIGH
            }
        )
    }

    @Test
    fun `GET reports with invalid type returns BadRequest`() = testApplication {
        application {
            module()
        }

        val response =
            client.get(
                "/reports?type=BANANA"
            )

        assertEquals(
            HttpStatusCode.BadRequest,
            response.status
        )
    }

    @Test
    fun `GET reports with invalid status returns BadRequest`() = testApplication {
        application {
            module()
        }

        val response =
            client.get(
                "/reports?status=BANANA"
            )

        assertEquals(
            HttpStatusCode.BadRequest,
            response.status
        )
    }

    @Test
    fun `GET reports with invalid urgency returns BadRequest`() = testApplication {
        application {
            module()
        }

        val response =
            client.get(
                "/reports?urgency=BANANA"
            )

        assertEquals(
            HttpStatusCode.BadRequest,
            response.status
        )
    }

    @Test
    fun `GET reports with invalid nuisance type returns BadRequest`() = testApplication {
        application {
            module()
        }

        val response =
            client.get(
                "/reports?nuisanceType=BANANA"
            )

        assertEquals(
            HttpStatusCode.BadRequest,
            response.status
        )
    }

    @Test
    fun `resolving report twice returns Conflict`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        val createdReport =
            createSafetyReport(
                client = client,
                title = "Broken streetlight",
                urgency = Urgency.HIGH
            )

        val firstResolveResponse =
            client.post(
                "/reports/${createdReport.id}/resolve"
            )

        assertEquals(
            HttpStatusCode.OK,
            firstResolveResponse.status
        )

        val secondResolveResponse =
            client.post(
                "/reports/${createdReport.id}/resolve"
            )

        assertEquals(
            HttpStatusCode.Conflict,
            secondResolveResponse.status
        )
    }

    @Test
    fun `claiming safety report returns Conflict`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        val createdReport =
            createSafetyReport(
                client = client,
                title = "Unsafe crossing",
                urgency = Urgency.HIGH
            )

        val response =
            client.post(
                "/reports/${createdReport.id}/claim"
            )

        assertEquals(
            HttpStatusCode.Conflict,
            response.status
        )
    }

    @Test
    fun `invalid type returns ErrorResponse`() = testApplication {
        application {
            module()
        }

        val client =
            createJsonClient()

        val response =
            client.get(
                "/reports?type=BANANA"
            )

        assertEquals(
            HttpStatusCode.BadRequest,
            response.status
        )

        val errorResponse =
            response.body<ErrorResponse>()

        assertEquals(
            400,
            errorResponse.status
        )

        assertEquals(
            HttpStatusCode.BadRequest.description,
            errorResponse.error
        )

        assertTrue(
            errorResponse.message.isNotBlank()
        )

        assertTrue(
            errorResponse.message.contains(
                "type"
            )
        )

        assertTrue(
            errorResponse.message.contains(
                "BANANA"
            )
        )
    }

    private fun ApplicationTestBuilder.createJsonClient() =
        createClient {
            install(ContentNegotiation) {
                json()
            }
        }

    private suspend fun createSafetyReport(
        client: io.ktor.client.HttpClient,
        title: String,
        urgency: Urgency
    ): ReportResponse {

        val response =
            client.post("/reports/safety") {
                contentType(ContentType.Application.Json)

                setBody(
                    CreateSafetyReportRequest(
                        title = title,
                        description = null,
                        photo = null,
                        latitude = 52.08,
                        longitude = 4.31,
                        urgency = urgency
                    )
                )
            }

        assertEquals(
            HttpStatusCode.Created,
            response.status
        )

        return response.body()
    }
}
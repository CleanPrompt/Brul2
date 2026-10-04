package nl.brul.dto.request

import kotlinx.serialization.Serializable
import nl.brul.domain.Urgency

@Serializable
data class CreateSafetyReportRequest(
    val title: String,
    val description: String? = null,
    val photo: String? = null,
    val latitude: Double,
    val longitude: Double,
    val urgency: Urgency
)
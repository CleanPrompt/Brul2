package nl.brul.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateReportRequest(
    val title: String,
    val description: String? = null,
    val photo: String? = null,
    val latitude: Double,
    val longitude: Double
)
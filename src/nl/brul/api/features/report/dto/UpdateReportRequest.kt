// src/nl/brul/api/features/report/dto/UpdateReportRequest.kt
package nl.brul.api.features.report.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateReportRequest(
    val type: String,
    val title: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val urgent: Boolean? = null,
    val volunteersNeeded: Int? = null
)
// src/nl/brul/api/features/report/dto/ReportResponse.kt
package nl.brul.api.features.report.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReportResponse(
    val id: Long,
    val type: String,
    val title: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val urgent: Boolean? = null,
    val volunteersNeeded: Int? = null,
    val createdAt: Long,
    val viewCount: Int,
    val ownerId: Long? = null
)
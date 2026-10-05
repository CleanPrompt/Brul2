package nl.brul.dto.response

import kotlinx.serialization.Serializable
import nl.brul.domain.NuisanceType
import nl.brul.domain.ReportStatus
import nl.brul.domain.ReportType
import nl.brul.domain.Urgency

@Serializable
data class ReportResponse(
    val id: String,
    val type: ReportType,
    val title: String,
    val description: String?,
    val photo: String?,
    val latitude: Double,
    val longitude: Double,
    val createdById: String,
    val createdByUsername: String,
    val createdAt: String,
    val status: ReportStatus,
    val urgency: Urgency? = null,
    val nuisanceType: NuisanceType? = null,
    val claimedById: String? = null,
    val claimedByUsername: String? = null
)
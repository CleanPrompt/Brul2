package nl.brul.dto.request

import kotlinx.serialization.Serializable
import nl.brul.domain.NuisanceType

@Serializable
data class CreateNuisanceReportRequest(
    val title: String,
    val description: String? = null,
    val photo: String? = null,
    val latitude: Double,
    val longitude: Double,
    val nuisanceType: NuisanceType
)
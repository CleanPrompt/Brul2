// src/nl/brul/api/features/report/dto/ReportResponse.kt
package nl.brul.api.features.report.dto

import kotlinx.serialization.Serializable

/**
 * Laag: dto (de vertaler tussen JSON en Kotlin).
 *
 * De JSON die de API terugstuurt voor één melding: alle velden, plus `status`, `createdAt`,
 * `viewCount` en `ownerId`.
 * `urgent` is alleen gevuld bij een SafetyReport en `volunteersNeeded` alleen bij een HelpRequest,
 * anders `null`.
 * Wordt gemaakt met `Report.toResponse()` in ReportMapper.kt.
 *
 * Waarom een apart DTO? Zo bepaal je zelf wat naar buiten gaat, en blijft het domeinmodel los van
 * de JSON.
 */
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
    val ownerId: Long? = null,
    // Help requests only (ISO-8601, UTC).
    val startsAt: String? = null,
    val endsAt: String? = null,
    // Only filled when a single report is requested.
    val photoCount: Int? = null
)

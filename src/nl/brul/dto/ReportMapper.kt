package nl.brul.dto

import nl.brul.domain.HelpRequest
import nl.brul.domain.NuisanceReport
import nl.brul.domain.Report
import nl.brul.domain.ReportType
import nl.brul.domain.SafetyReport
import nl.brul.dto.response.ReportResponse

fun Report.toResponse(): ReportResponse {
    return ReportResponse(
        id = id,
        type = when (this) {
            is SafetyReport -> ReportType.SAFETY
            is NuisanceReport -> ReportType.NUISANCE
            is HelpRequest -> ReportType.HELP
        },
        title = title,
        description = description,
        photo = photo,
        latitude = location.latitude,
        longitude = location.longitude,
        createdById = createdBy.id,
        createdByUsername = createdBy.username,
        createdAt = createdAt.toString(),
        status = status,
        urgency = if (this is SafetyReport) {
            urgency
        } else {
            null
        },
        nuisanceType = if (this is NuisanceReport) {
            nuisanceType
        } else {
            null
        },
        claimedById = if (this is HelpRequest) {
            claimedBy?.id
        } else {
            null
        },
        claimedByUsername = if (this is HelpRequest) {
            claimedBy?.username
        } else {
            null
        }
    )
}
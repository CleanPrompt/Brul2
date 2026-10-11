package nl.brul.dto

import nl.brul.domain.Claimable
import nl.brul.domain.NuisanceReport
import nl.brul.domain.Report
import nl.brul.domain.SafetyReport
import nl.brul.dto.response.ReportResponse

fun Report.toResponse(): ReportResponse {

    val claimedBy =
        (this as? Claimable)?.claimedBy

    return ReportResponse(
        id = id,
        type = type,
        title = title,
        description = description,
        photo = photo,
        latitude = location.latitude,
        longitude = location.longitude,
        createdById = createdBy.id,
        createdByUsername = createdBy.username,
        createdAt = createdAt.toString(),
        status = status,
        urgency = (this as? SafetyReport)?.urgency,
        nuisanceType = (this as? NuisanceReport)?.nuisanceType,
        claimedById = claimedBy?.id,
        claimedByUsername = claimedBy?.username
    )
}

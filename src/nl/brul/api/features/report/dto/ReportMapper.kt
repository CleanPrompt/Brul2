// src/nl/brul/api/features/report/dto/ReportMapper.kt
package nl.brul.api.features.report.dto

import nl.brul.api.features.report.model.HelpRequest
import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.NuisanceReport
import nl.brul.api.features.report.model.Report
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportStatus
import nl.brul.api.features.report.model.ReportType
import nl.brul.api.features.report.model.SafetyReport
import nl.brul.api.features.report.model.NewReport

private inline fun <reified T : Enum<T>> parseEnum(
    value: String,
    fieldName: String
): T {
    return enumValues<T>().firstOrNull {
        it.name == value
    } ?: throw IllegalArgumentException(
        "Invalid $fieldName: $value."
    )
}

fun CreateReportRequest.toNewReport(): NewReport {
    return NewReport(
        type = parseEnum<ReportType>(type, "report type"),
        title = title,
        description = description,
        category = parseEnum<ReportCategory>(category, "report category"),
        location = Location(latitude, longitude),
        urgent = urgent,
        volunteersNeeded = volunteersNeeded
    )
}

fun UpdateReportRequest.toReport(existing: Report): Report {
    val requestedType = parseEnum<ReportType>(type, "report type")

    require(requestedType == existing.type) {
        "Report type cannot be changed."
    }

    val newReport = NewReport(
        type = requestedType,
        title = title,
        description = description,
        category = parseEnum<ReportCategory>(category, "report category"),
        location = Location(latitude, longitude),
        urgent = urgent,
        volunteersNeeded = volunteersNeeded
    )

    val requestedStatus = parseEnum<ReportStatus>(
        status,
        "report status"
    )

    return when (newReport.type) {
        ReportType.SAFETY -> SafetyReport(
            id = existing.id,
            title = newReport.title,
            description = newReport.description,
            category = newReport.category,
            location = newReport.location,
            urgent = requireNotNull(newReport.urgent),
            status = requestedStatus,
            createdAt = existing.createdAt,
            viewCount = existing.viewCount,
            ownerId = existing.ownerId
        )

        ReportType.NUISANCE -> NuisanceReport(
            id = existing.id,
            title = newReport.title,
            description = newReport.description,
            category = newReport.category,
            location = newReport.location,
            status = requestedStatus,
            createdAt = existing.createdAt,
            viewCount = existing.viewCount,
            ownerId = existing.ownerId
        )

        ReportType.HELP_REQUEST -> HelpRequest(
            id = existing.id,
            title = newReport.title,
            description = newReport.description,
            category = newReport.category,
            location = newReport.location,
            volunteersNeeded = requireNotNull(
                newReport.volunteersNeeded
            ),
            status = requestedStatus,
            createdAt = existing.createdAt,
            viewCount = existing.viewCount,
            ownerId = existing.ownerId
        )
    }
}

fun Report.toResponse(): ReportResponse {
    return ReportResponse(
        id = id,
        type = type.name,
        title = title,
        description = description,
        category = category.name,
        latitude = location.latitude,
        longitude = location.longitude,
        status = status.name,
        urgent = (this as? SafetyReport)?.urgent,
        volunteersNeeded = (this as? HelpRequest)?.volunteersNeeded,
        createdAt = createdAt,
        viewCount = viewCount,
        ownerId = ownerId
    )
}
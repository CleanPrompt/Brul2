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
import nl.brul.api.features.report.model.TimeSlot
import java.time.Instant
import java.time.format.DateTimeParseException

/**
 * Hulpfunctie: zet tekst zoals "SAFETY" om naar de bijbehorende enum-waarde.
 * Bestaat de waarde niet, dan volgt een IllegalArgumentException; ErrorHandling maakt daar 400 Bad
 * Request van.
 * Door `reified` werkt deze ene functie voor elk enum-type.
 */
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

private fun parseInstant(value: String, fieldName: String): Instant =
    try {
        Instant.parse(value)
    } catch (exception: DateTimeParseException) {
        throw IllegalArgumentException(
            "Invalid $fieldName: use ISO-8601 in UTC, e.g. 2026-11-20T10:00:00Z.",
            exception
        )
    }

/**
 * Builds a time slot from the optional request fields.
 * Both fields must be given together, or both left out.
 */
private fun parseSlot(startsAt: String?, endsAt: String?): TimeSlot? {
    if (startsAt == null && endsAt == null) {
        return null
    }

    require(startsAt != null && endsAt != null) {
        "startsAt and endsAt must be given together."
    }

    return TimeSlot(
        start = parseInstant(startsAt, "startsAt"),
        end = parseInstant(endsAt, "endsAt")
    )
}

/**
 * Laag: dto (mapper).
 *
 * Extension function: zet een CreateReportRequest om naar een NewReport.
 * Tekst wordt enums (type en categorie), de coördinaten worden een Location en
 * `startsAt` en `endsAt` worden samen een TimeSlot (alleen bij een hulpvraag).
 * Daarna controleert NewReport in zijn `init` zelf of alles klopt.
 * De ownerId wordt later in de route gezet met `.copy(ownerId = userId)`.
 */
fun CreateReportRequest.toNewReport(): NewReport {
    return NewReport(
        type = parseEnum<ReportType>(type, "report type"),
        title = title,
        description = description,
        category = parseEnum<ReportCategory>(category, "report category"),
        location = Location(latitude, longitude),
        urgent = urgent,
        volunteersNeeded = volunteersNeeded,
        slot = parseSlot(startsAt, endsAt)
    )
}

/**
 * Zet een UpdateReportRequest om naar het juiste soort Report: SafetyReport, NuisanceReport of
 * HelpRequest.
 *
 * - Het type mag niet veranderen.
 * - id, createdAt, viewCount en ownerId komen van de bestaande melding, zodat een gebruiker die
 *   niet kan vervalsen.
 * - Eerst wordt een NewReport gemaakt, zodat dezelfde controles gelden als bij het aanmaken.
 */
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
        volunteersNeeded = volunteersNeeded,
        // A help request keeps its slot when the client does not send one.
        slot = parseSlot(startsAt, endsAt) ?: (existing as? HelpRequest)?.slot
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
            slot = requireNotNull(newReport.slot),
            status = requestedStatus,
            createdAt = existing.createdAt,
            viewCount = existing.viewCount,
            ownerId = existing.ownerId
        )
    }
}

/**
 * Zet een Report om naar een ReportResponse: de JSON die de API terugstuurt.
 * Met `as?` (veilige cast) worden `urgent`, `volunteersNeeded` en het tijdslot (`startsAt` en
 * `endsAt`) alleen ingevuld als ze bij dit soort melding horen.
 * `photoCount` wordt alleen meegegeven bij het ophalen van één melding (GET /reports/{id}).
 */
fun Report.toResponse(photoCount: Int? = null): ReportResponse {
    val slot = (this as? HelpRequest)?.slot

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
        ownerId = ownerId,
        startsAt = slot?.start?.toString(),
        endsAt = slot?.end?.toString(),
        photoCount = photoCount
    )
}
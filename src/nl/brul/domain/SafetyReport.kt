package nl.brul.domain

import kotlin.time.Clock
import kotlin.time.Instant

/**
 * A report about an unsafe situation, with an [Urgency].
 */
class SafetyReport(
    title: String,
    description: String?,
    photo: String?,
    location: Location,
    createdBy: User,
    val urgency: Urgency,
    createdAt: Instant = Clock.System.now()
) : Report(
    title = title,
    description = description,
    photo = photo,
    location = location,
    createdBy = createdBy,
    createdAt = createdAt
) {

    override val type: ReportType = ReportType.SAFETY
}
